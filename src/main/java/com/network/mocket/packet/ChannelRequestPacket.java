package com.network.mocket.packet;

import com.network.mocket.channel.manager.IChannelManager;
import com.network.mocket.helper.Pair;
import com.network.mocket.parser.HeaderToBytes;

import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChannelRequestPacket extends Packet {
  private static final Logger LOGGER = Logger.getLogger( ChannelRequestPacket.class.getName() );


  ChannelRequestPacket(ByteBuffer byteBuffer, Integer offset) {
    super(byteBuffer, offset);
    header = new ChannelRequestHeader();
  }

  @Override
  public void process(IChannelManager channelManager) {
    super.process(channelManager);
    LOGGER.log(Level.FINEST, "got uuid for channel {0}", ((ChannelRequestHeader) header).getChannelId());
    channelManager.setChannelId(((ChannelRequestHeader) header).getChannelId());
  }

  @Override
  public void postProcess(IChannelManager channelManager, ExecutorService executorService) {
//    channelManager.getIncomingPacketConsumedTill().incrementAndGet();
  }

  public class ChannelRequestHeader extends Header {

    public static final int CHANNEL_REQ_HEADER_LEN = 16 + Header.LENGTH;

    public static final int MSB = Header.LENGTH;
    public static final int LSB = Header.LENGTH + 8;

    public UUID getChannelId() {
      return new UUID(byteBuffer.getLong(offset + MSB), byteBuffer.getLong(offset + LSB));
    }

/*
    public void setUUID(UUID uuid) {
      byteBuffer.putLong(offset + MSB, uuid.getMostSignificantBits());
      byteBuffer.putLong(offset + LSB, uuid.getLeastSignificantBits());
    }
*/

    public long getMostSignificantBits() {
      return byteBuffer.getLong(offset + MSB);
    }

    public long getLeastSignificantBits() {
      return byteBuffer.getLong(offset + LSB);
    }


    @Override
    public int getTotalSize() {
      return CHANNEL_REQ_HEADER_LEN;
    }

    @Override
    public int getHeaderLength() {
      return CHANNEL_REQ_HEADER_LEN;
    }

    @Override
    public int getPayloadSize() {
      return 0;
    }


    @Override
    public String toString() {
      return super.toString() + "(MSB, LSB) = (" + getMostSignificantBits() + ", " + getLeastSignificantBits() + ")";
    }
  }

  public static byte [] requestHeaderToBytes(int seq, int ack, UUID uuid) {
    byte[] rawHeader =  HeaderToBytes.HEADER_TO_BYTES.apply(PacketType.REQUEST, Pair.create(seq, ack));
    byte [] requestHeader = new byte[ChannelRequestHeader.CHANNEL_REQ_HEADER_LEN];
    ByteBuffer byteBuffer = ByteBuffer.wrap(requestHeader);

    byteBuffer.put(rawHeader);

    byteBuffer.putLong(ChannelRequestHeader.MSB, uuid.getMostSignificantBits());
    byteBuffer.putLong(ChannelRequestHeader.LSB, uuid.getLeastSignificantBits());
    return requestHeader;
  }

  public static class Parser implements Packet.Parser {

    @Override
    public Packet apply(Pair<ByteBuffer, Integer> byteBufferOffset) {
      return new ChannelRequestPacket(
        byteBufferOffset.getFirst(),
        byteBufferOffset.getSecond()
      );
    }

    @Override
    public Pair<Integer, IPacket> wrapBytesToPacket(IChannelManager channelManager, byte[] data) throws InterruptedException {
      int seqNumber = channelManager.getPacketManager().getSeqGeneratorClient().getNextPacketSequence();
      int ackNumber = channelManager.getAckManager().getLastSeenAck();
      byte [] header = ChannelRequestPacket.requestHeaderToBytes(seqNumber, ackNumber, channelManager.getPacketManager().getChannelId());
      IPacket dataPacket = channelManager.getPacketManager().allocator.allocate(header.length, PacketType.REQUEST);
      dataPacket.put(header);
      return Pair.create(seqNumber, dataPacket);
    }
  }

}
