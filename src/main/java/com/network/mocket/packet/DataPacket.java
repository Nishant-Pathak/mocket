package com.network.mocket.packet;

import com.network.mocket.channel.manager.IChannelManager;
import com.network.mocket.helper.Pair;
import com.network.mocket.parser.HeaderToBytes;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;

final public class DataPacket extends Packet {

  /**
   * Created Packet with Data header
   * @param byteBuffer allocated @{@link ByteBuffer}
   * @param offset offset in buffer
   */
  public DataPacket(ByteBuffer byteBuffer, int offset) {
    super(byteBuffer, offset);
    header = new DataHeader();
  }

  @Override
  public void process(IChannelManager channelManager) {
    super.process(channelManager);
  }

  @Override
  public void postProcess(IChannelManager channelManager, ExecutorService executorService) {
  }

  /**
   * Header all 4 bytes
   * ____________________________
   * |          |        |       |
   * |  offset  |  size  |  ack  |
   * |__________|________|_______|
   *
   * seq is atomicInteger number stamped to this packet
   *     if seq id == 0 data has 16 bytes starting channel id (uuid
   * offset is the start index of the payload in the data stream
   * size is the payload size ( except header)
   * ack is the ack seen by the sender of this packet
   *
   */
  public class DataHeader extends Header {

    /**
     * indexes
     */
    public static final int OFFSET = Header.LENGTH;
    public static final int SIZE = 14;

    public static final int DATA_HEADER_LENGTH = 8 + Header.LENGTH;

    private DataHeader() {
    }

    public int getOffset() {
      return byteBuffer.getInt(OFFSET + offset);
    }

    public void setOffset(int os) {
      byteBuffer.putInt(OFFSET + offset, os);
    }

    public int getSize() {
      return byteBuffer.getInt(SIZE + offset);
    }

    public void setSize(int size) {
      byteBuffer.putInt(SIZE + offset, size);
    }

    @Override
    public int getPayloadSize() {
      return getSize() - DATA_HEADER_LENGTH;
    }

    @Override
    protected int getTotalSize() {
      return getSize();
    }

    @Override
    public int getHeaderLength() {
      return DATA_HEADER_LENGTH;
    }

    @Override
    public String toString() {
      return super.toString() + "(Offset) = (" + getOffset() + ")";
    }
  }

  public static byte [] dataHeaderToBytes(int seq, int offset, int size, int ack) {
    byte [] dataHeader = new byte[DataPacket.DataHeader.DATA_HEADER_LENGTH];
    ByteBuffer byteBuffer = ByteBuffer.wrap(dataHeader);

    byte [] header = HeaderToBytes.HEADER_TO_BYTES.apply(PacketType.DATA, Pair.create(seq, ack));
    byteBuffer.put(header);

    byteBuffer.putInt(DataPacket.DataHeader.SEQUENCE, seq);
    byteBuffer.putInt(DataPacket.DataHeader.OFFSET, offset);
    byteBuffer.putInt(DataPacket.DataHeader.SIZE, size + DataHeader.DATA_HEADER_LENGTH);
    return dataHeader;
  }

  public static class Parser implements Packet.Parser {
    @Override
    public DataPacket apply(Pair<ByteBuffer, Integer> byteBufferOffset) {
      return new DataPacket(byteBufferOffset.getFirst(), byteBufferOffset.getSecond());
    }

    @Override
    public Pair<Integer, IPacket> wrapBytesToPacket(final IChannelManager channelManager, final byte[] data) throws InterruptedException {
      int seqNumber = channelManager.getPacketManager().getSeqGeneratorClient().getNextPacketSequence();
      int currentOffset = channelManager.getPacketManager().getOffset().getAndAdd(data.length);
      int ackSeen = channelManager.getAckManager().getLastSeenAck();
      byte[] header = DataPacket.dataHeaderToBytes(seqNumber, currentOffset, data.length, ackSeen);
      IPacket dataPacket = channelManager.getPacketManager().allocator.allocate(header.length + data.length, PacketType.DATA);
      dataPacket.put(header);
      dataPacket.put(data);
      return Pair.create(seqNumber, dataPacket);

    }
  }

}
