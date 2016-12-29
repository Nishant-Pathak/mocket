package com.network.mocket.packet;

import com.network.mocket.Constants;
import com.network.mocket.channel.manager.IChannelManager;
import com.network.mocket.helper.Pair;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * packet responsible for selective nack
 */
public class NackPacket extends Packet {
  private static final int MAX_NACK_SIZE = 100;
  private static final Logger LOGGER = Logger.getLogger(NackPacket.class.getSimpleName());

  public NackPacket(ByteBuffer byteBuffer, int offset) {
    super(byteBuffer, offset);
    this.header = new NackHeader();
  }

  @Override
  public void process(IChannelManager channelManager) {
    int ack = ((NackHeader) header).getAckFromNack();
    channelManager.getPacketManager().setDeliveredSuccessfully(ack);
  }

  @Override
  public void postProcess(
      final IChannelManager channelManager,
      final ExecutorService executorService
  ) {
    byte[] voidByteArray = readPayload();
    int voidCount = ((NackHeader) header).getNackCount();
    ByteBuffer byteBuffer = ByteBuffer.wrap(voidByteArray);
    final List<Integer> voids = new ArrayList<>(voidCount);
    for (int i = 0; i < voidCount; i++) {
      voids.add(byteBuffer.getInt());
    }
    executorService.submit(new Runnable() {
      @Override public void run() {
        for (Integer v : voids) {
          channelManager.reSendPacketWithIndex(v);
        }
      }
    });
    executorService.execute(new Runnable() {
      @Override public void run() {
        channelManager.reactOnAcknowledge();
      }
    });
  }

  public class NackHeader extends Header {
    // 2 bytes type and 2 bytes nack sequence number count
    public static final int HEADER_LENGTH = 4;

    public static final int NACK_COUNT_OFFSET = 2;

    public int getNackCount() {
      return byteBuffer.getShort(NACK_COUNT_OFFSET);
    }
    public int getAckFromNack() {
      if (getNackCount() == 0) {
        LOGGER.log(Level.FINE, "cant get ack from nack");
        return -1;
      }
      return byteBuffer.getInt(HEADER_LENGTH) - 1;
    }

    @Override
    public int getPayloadSize() {
      return getNackCount() * 4;
    }

    @Override
    protected int getTotalSize() {
      return getPayloadSize() + HEADER_LENGTH;
    }

    @Override
    public int getHeaderLength() {
      return HEADER_LENGTH;
    }

    @Override
    public String toString() {
      return "(PT: NACK) =" + getNackCount() + ")";
    }
  }

  public static class Parser implements Packet.Parser {

    @Override
    public Pair<Integer, IPacket> wrapBytesToPacket(IChannelManager channelManager, byte[] data) throws InterruptedException {

      List<Integer> voids = channelManager.getAckManager().getVoids(MAX_NACK_SIZE);
      byte[] header = NackPacket.nackHeaderToBytes(voids);
      IPacket nackPacket = new NackPacket(ByteBuffer.wrap(header), 0);
      nackPacket.put(header);
      return Pair.create(Constants.IGNORE_SEQUENCE, nackPacket);
    }

    @Override
    public Packet apply(Pair<ByteBuffer, Integer> byteBufferIntegerPair) {
      return new NackPacket(byteBufferIntegerPair.getFirst(), byteBufferIntegerPair.getSecond());
    }
  }

  private static byte[] nackHeaderToBytes(List<Integer> voids) {
    byte [] nackPacket = new byte[NackHeader.HEADER_LENGTH + voids.size() * 4];
    ByteBuffer byteBuffer = ByteBuffer.wrap(nackPacket);
    byteBuffer.putShort(PacketType.NACK.type);
    byteBuffer.putShort((short) voids.size());
    for (Integer v: voids) {
      byteBuffer.putInt(v);
    }
    return nackPacket;
  }
}
