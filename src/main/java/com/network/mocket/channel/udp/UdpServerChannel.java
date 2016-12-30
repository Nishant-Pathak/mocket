package com.network.mocket.channel.udp;

import com.network.mocket.MocketException;
import com.network.mocket.allocator.buffer.ByteBufferAllocator;
import com.network.mocket.allocator.packet.PacketAllocator;
import com.network.mocket.channel.BaseServerChannel;
import com.network.mocket.packet.DataPacket;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

final public class UdpServerChannel extends BaseServerChannel {
  private static final int MAX_DATA_SIZE = 508 - DataPacket.DataHeader.DATA_HEADER_LENGTH;
  private DatagramChannel datagramChannel;

  public UdpServerChannel(
      ByteBufferAllocator byteBufferAllocator,
      PacketAllocator packetAllocator,
      ExecutorService readExecutorService,
      ScheduledExecutorService executorService) {
    super(MAX_DATA_SIZE, byteBufferAllocator, packetAllocator, readExecutorService,
        executorService, false);
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
  protected SocketAddress receive(ByteBuffer byteBuffer, SelectionKey selectionKey) throws IOException {
    return datagramChannel.receive(byteBuffer);
  }

  @Override
  public void write(SocketAddress address, byte[] data) throws IOException {
    datagramChannel.send(ByteBuffer.wrap(data), address);
  }

  @Override
  public void init(int port) throws MocketException {
    super.init(port);
    try {
      datagramChannel = DatagramChannel.open();
      datagramChannel.configureBlocking(false);
      datagramChannel.setOption(StandardSocketOptions.SO_RCVBUF, 1024 * 1024);
      datagramChannel.setOption(StandardSocketOptions.SO_SNDBUF, 1024 * 1024);
      datagramChannel.bind(new InetSocketAddress(port));
      Executors.newSingleThreadExecutor().execute(new UdpServerChannel.ChannelRead());

    } catch (IOException e) {
      e.printStackTrace();
      throw new MocketException(e);
    }
  }

  private class ChannelRead implements Runnable {
    @Override
    public void run() {
      try {
        selector = Selector.open();
        datagramChannel.register(selector, SelectionKey.OP_READ);
        while (true) {
          try {
            selector.select();
            Iterator selectedKeys = selector.selectedKeys().iterator();
            while (selectedKeys.hasNext()) {
              try {
                SelectionKey key = (SelectionKey) selectedKeys.next();
                selectedKeys.remove();

                if (!key.isValid()) {
                  continue;
                }

                if (key.isReadable()) {
                  read(key);
                }
              } catch (IOException | InterruptedException e) {
                System.err.println("glitch, continuing... " +(e.getMessage()!=null?e.getMessage():""));
                e.printStackTrace();
              }
            }
          } catch (IOException e) {
            System.err.println("glitch, continuing... " +(e.getMessage()!=null?e.getMessage():""));
          }
        }
      } catch (IOException e) {
        System.err.println("network error: " + (e.getMessage()!=null?e.getMessage():""));
      }
    }
  }
}
