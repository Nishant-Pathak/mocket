package com.network.mocket.channel;

import com.network.mocket.Constants;
import com.network.mocket.MocketException;
import com.network.mocket.allocator.buffer.ByteBufferAllocator;
import com.network.mocket.allocator.packet.PacketAllocator;
import com.network.mocket.channel.manager.ChannelManager;
import com.network.mocket.channel.manager.IChannelManager;
import com.network.mocket.channel.manager.ServerChannelManager;
import com.network.mocket.helper.Pair;
import com.network.mocket.helper.Utils;
import com.network.mocket.packet.*;
import com.network.mocket.parser.ByteBufferToPackets;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseServerChannel extends ChannelCommons implements ServerChannel {
  protected static final Logger LOGGER = Logger.getLogger(BaseServerChannel.class.getName());
  protected final ExecutorService readExecutorService;
  protected final PacketAllocator packetAllocator;
  private final LinkedBlockingQueue<SocketAddress> incomingSocketAddresses;
  private final ConcurrentHashMap<SocketAddress, ChannelManager> socketAddressChannelManager;

  private final ConcurrentHashMap<UUID, SocketAddress> uuidSocketAddress;
  private final int maxDataSize;
  private final ScheduledExecutorService scheduledExecutorService;
  private final boolean appReliabilityEnabled;
  protected Selector selector;
  private ByteBuffer readBuffer;

  //  private final ConcurrentHashMap<SocketAddress, ByteBuffer> pendingBuffers;
  public BaseServerChannel(
      int maxDataSize,
      ByteBufferAllocator bufferAllocator,
      PacketAllocator packetAllocator,
      ExecutorService readExecutorService,
      ScheduledExecutorService scheduledExecutorService,
      boolean appReliabilityEnabled
  ) {
    this.maxDataSize = maxDataSize;
    this.packetAllocator = packetAllocator;
    this.readExecutorService = readExecutorService;
    this.scheduledExecutorService = scheduledExecutorService;
    this.appReliabilityEnabled = appReliabilityEnabled;

    incomingSocketAddresses = new LinkedBlockingQueue<>();
    socketAddressChannelManager = new ConcurrentHashMap<>();
    uuidSocketAddress = new ConcurrentHashMap<>();
  }

  protected void read(SelectionKey key) throws IOException, InterruptedException{
    SocketAddress sender = receive(readBuffer, key);
    readBuffer.flip();

    if (!readBuffer.hasRemaining()) {
      return;
    }

    LOGGER.log(Level.INFO, "Got {0} bytes from {1}", new Object[]{readBuffer.remaining(), sender});

    List<IPacket> packets = ByteBufferToPackets.parse(readBuffer);

    List<IPacket> incomingPacket = new ArrayList<>(packets.size());

    LOGGER.log(Level.INFO, "total packets got: {0}", packets.size());

    for(IPacket packet: packets) {
      logPacket(sender, packet, false);
      IPacket packet1 = packetAllocator.allocate(packet.getSize(), packet.getHeader().getPacketType());
      packet1.put(packet.read());
      incomingPacket.add(packet1);
    }

    readBuffer.clear();

    ChannelManager channelManager;

    if ((channelManager = socketAddressChannelManager.get(sender)) == null) {
      channelManager = new ServerChannelManager(
          new PacketManager(packetAllocator),
          this,
          sender,
          scheduledExecutorService,
          appReliabilityEnabled
      );
      socketAddressChannelManager.put(sender, channelManager);
    }

    for (IPacket packet : incomingPacket) {
      if (packet.getHeader().getPacketType().isTimeAccounted()) {
        channelManager.setLastReceiveTime(Utils.now());
      }
      if (channelManager.canIgnore(packet)) {
        packetAllocator.release(packet);
        continue;
      }

      if (PacketType.REQUEST.equals(packet.getHeader().getPacketType())) {
        UUID channelId = ((ChannelRequestPacket.ChannelRequestHeader) packet.getHeader()).getChannelId();
        if (uuidSocketAddress.containsKey(channelId)) {
          // new channel for existing id
          socketAddressChannelManager.put(sender, socketAddressChannelManager.get(uuidSocketAddress.get(channelId)));
        }
        uuidSocketAddress.put(channelId, sender);
      }
      packet.process(channelManager);
      packet.postProcess(channelManager, scheduledExecutorService);

      if (PacketType.ACK.equals(packet.getHeader().getPacketType()) ||
          PacketType.NACK.equals(packet.getHeader().getPacketType())) {
        packetAllocator.release(packet);
        continue;
      }

      if (PacketType.DATA.equals(packet.getHeader().getPacketType())) {
        incomingSocketAddresses.put(sender);
      }
    }
  }

  abstract protected void channelShutDown();

  @Override
  public void shutDown() {
    LOGGER.log(Level.INFO, "shutting down server");
    channelShutDown();
    readExecutorService.shutdown();
    scheduledExecutorService.shutdown();
  }


  @Override
  public Pair<SocketAddress, Pair<byte[], Integer>> processRead() throws InterruptedException {
    SocketAddress senderAddress = incomingSocketAddresses.take();
    LOGGER.log(Level.INFO, "got data from {0}", senderAddress);
    ChannelManager channelManager = socketAddressChannelManager.get(senderAddress);
    Pair<byte[], Integer> result = read(channelManager);
    LOGGER.log(Level.FINE, "got data #{0} as {1}", new Object[] {result.getSecond(), result.getFirst()});
    return Pair.create(senderAddress, result);
  }

  @Override
  public void processWrite(byte[] data, SocketAddress address) throws IOException, InterruptedException {
    ChannelManager channelManager = socketAddressChannelManager.get(address);
    for(int i =0; i< data.length; i+= maxDataSize) {
      Pair<Integer, IPacket> dataPacket =
          ByteBufferToPackets.wrapBytesToPackets(
              channelManager, PacketType.DATA,
              Arrays.copyOfRange(data, i, i + maxDataSize > data.length ? data.length: i+ maxDataSize)
          );
      write(channelManager, address, dataPacket.getSecond(), dataPacket.getFirst());
    }
  }

  protected abstract SocketAddress receive(ByteBuffer byteBuffer, SelectionKey selectionKey) throws IOException;

  @Override
  public void release(SocketAddress address, Integer seq) {
    IChannelManager channelManager = socketAddressChannelManager.get(address);
    releasePacket(channelManager, packetAllocator, channelManager.getIncomingPacketConsumedTill().get(), seq);
  }

  @Override
  public void init(int port) throws MocketException {
    readBuffer = ByteBuffer.allocate(1024 * 1000);
  }
}
