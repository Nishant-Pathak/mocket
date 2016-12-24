package com.network.mocket.channel;

import com.network.mocket.allocator.buffer.ByteBufferAllocator;
import com.network.mocket.allocator.packet.PacketAllocator;
import com.network.mocket.channel.manager.IChannelManager;
import com.network.mocket.helper.Pair;
import com.network.mocket.helper.Utils;
import com.network.mocket.packet.IPacket;
import com.network.mocket.packet.PacketType;
import com.network.mocket.parser.ByteBufferToPackets;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * only to be used for client channel
 */
public abstract class Channel extends ChannelCommons implements ClientChannel {
  protected static final Logger LOGGER = Logger.getLogger(Channel.class.getName());
  private final UUID channelId;
  protected final PacketAllocator packetAllocator;
  protected final ScheduledExecutorService scheduledExecutorService;
  private final ExecutorService readExecutor;
  private final Object lock;
  private final int maxDataSize;
  protected IChannelManager channelManager;
  protected SocketAddress serverAddress;

  public Channel(
      int maxDataSize,
      UUID channelId,
      ByteBufferAllocator bufferAllocator,
      PacketAllocator packetAllocator,
      ExecutorService readExecutor,
      ScheduledExecutorService scheduledExecutorService
  ) {
    this.maxDataSize = maxDataSize;
    this.channelId = channelId;
    this.packetAllocator = packetAllocator;
    this.lock = new Object();
    this.readExecutor = readExecutor;
    this.scheduledExecutorService = scheduledExecutorService;
  }

  abstract protected void readInBuffer(ByteBuffer byteBuffer) throws IOException;

  @Override
  public void processWrite(byte[] data) throws IOException, InterruptedException {
    LOGGER.log(Level.INFO, "writing data of length: {0}", data.length);
    for(int i =0; i< data.length; i+= maxDataSize) {
      Pair<Integer, IPacket> dataPacket =
          ByteBufferToPackets.wrapBytesToPackets(
              channelManager, PacketType.DATA,
              Arrays.copyOfRange(data, i, i + maxDataSize > data.length ? data.length: i + maxDataSize)
          );
      write(channelManager, serverAddress, dataPacket.getSecond(), dataPacket.getFirst());
    }
  }

  @Override
  public Pair<byte[], Integer> processRead() throws InterruptedException, IOException {
    synchronized (lock) {
       lock.wait(1000);
      return read(channelManager);
    }
  }

  public void release(Integer consumedSeq) {
    releasePacket(channelManager, packetAllocator, channelManager.getIncomingPacketConsumedTill().get(), consumedSeq);
  }

  abstract protected void channelShutDown();

  @Override
  public void shutDown() {
    LOGGER.log(Level.INFO, "shutting down client");
    scheduledExecutorService.shutdown();
    readExecutor.shutdown();
    channelShutDown();
  }


  public class ChannelRead implements Runnable {
    private ByteBuffer readBuffer = ByteBuffer.allocate(1024 * 1024);

    public ChannelRead() throws InterruptedException {
    }

    @Override
    public void run() {
      while (true) {
        try {
          readInBuffer(readBuffer);
          if (!readBuffer.hasRemaining()) continue;
          readBuffer.flip();

          List<IPacket> packets = ByteBufferToPackets.parse(readBuffer);

          List<IPacket> incomingPacket = new ArrayList<>(packets.size());

          for(IPacket packet: packets) {
            MocketChannel.logPacket(serverAddress, packet, false);
            IPacket packet1 = packetAllocator.allocate(packet.getSize(), packet.getHeader().getPacketType());
            packet1.put(packet.read());
            incomingPacket.add(packet1);
          }

          readBuffer.clear();

          boolean gotSomeData = false;
          for (IPacket packet: incomingPacket) {
            if (packet.getHeader().getPacketType().isTimeAccounted()) {
              channelManager.setLastReceiveTime(Utils.now());
            }
            if(channelManager.canIgnore(packet)) {
              packetAllocator.release(packet);
              continue;
            }

            packet.process(channelManager);
            packet.postProcess(channelManager, scheduledExecutorService);

            if (PacketType.DATA.equals(packet.getHeader().getPacketType())) {
              gotSomeData = true;
            }
          }
          if (gotSomeData) {
            synchronized (lock) {
              lock.notify();
            }
          }
        } catch (InterruptedException | IOException ex) {
          LOGGER.finest(ex.toString());
          break;
        }
      }
      Channel.this.shutDown();
    }
  }
}
