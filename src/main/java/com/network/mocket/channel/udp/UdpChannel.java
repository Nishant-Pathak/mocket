package com.network.mocket.channel.udp;

import com.network.mocket.MocketException;
import com.network.mocket.allocator.buffer.ByteBufferAllocator;
import com.network.mocket.allocator.packet.PacketAllocator;
import com.network.mocket.channel.Channel;
import com.network.mocket.channel.manager.ClientChannelManager;
import com.network.mocket.packet.DataPacket;
import com.network.mocket.packet.PacketManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

final public class UdpChannel extends Channel {
  // see below link for why 508
  // http://stackoverflow.com/questions/1098897/what-is-the-largest-safe-udp-packet-size-on-the-internet#answer-35697810
  private static final int MAX_DATA_SIZE = 508 - DataPacket.DataHeader.DATA_HEADER_LENGTH;
  private static final Logger LOGGER = Logger.getLogger(UdpChannel.class.getName());
  private final UUID channelId;
  private DatagramChannel datagramChannel;
  private boolean ensureDelivery;

  public UdpChannel(
      UUID channelId,
      ByteBufferAllocator byteBufferAllocator,
      PacketAllocator packetAllocator,
      ExecutorService readExecutor,
      ScheduledExecutorService executorService,
      boolean ensureDelivery) {
    super(MAX_DATA_SIZE, channelId, byteBufferAllocator, packetAllocator, readExecutor, executorService);
    this.channelId = channelId;
    this.ensureDelivery = ensureDelivery;
  }

  @Override
  public void write(SocketAddress address, byte[] data) throws IOException {
    datagramChannel.send(ByteBuffer.wrap(data), address);
  }

  @Override
  protected void readInBuffer(ByteBuffer byteBuffer) throws IOException {
    datagramChannel.read(byteBuffer);
  }

  @Override
  protected void channelShutDown() {
    try {
      datagramChannel.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void init(String host, int port) throws MocketException {
    try {
      serverAddress = new InetSocketAddress(InetAddress.getByName(host), port);
      datagramChannel = DatagramChannel.open();
      datagramChannel.setOption(StandardSocketOptions.SO_RCVBUF, 1024 * 1024);
      datagramChannel.setOption(StandardSocketOptions.SO_SNDBUF, 1024 * 1024);
      datagramChannel.connect(serverAddress);

      channelManager =
          new ClientChannelManager(
              new PacketManager(packetAllocator),
              this,
              serverAddress,
              scheduledExecutorService,
              ensureDelivery
          );
      channelManager.setChannelId(channelId);
      channelManager.registerChannel();

      Executors.newSingleThreadExecutor().execute(new UdpChannel.ChannelRead());
    } catch (InterruptedException | IOException ex) {
      throw new MocketException(ex);
    }
  }
}
