package com.network.mocket.packet;

import com.network.mocket.Constants;
import com.network.mocket.channel.manager.ChannelManager;
import com.network.mocket.channel.manager.IChannelManager;
import com.network.mocket.helper.Pair;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

public class AckPacket extends Packet {
  private static final Logger LOGGER = Logger.getLogger( ChannelManager.class.getName() );

  public AckPacket(ByteBuffer byteBuffer, int offset) {
    super(byteBuffer, offset);
    this.header = new AckHeader();
  }

  @Override
  public void process(IChannelManager channelManager) {
    channelManager.getPacketManager().setDeliveredSuccessfully(getHeader().getAckAsRange().getFirst());
    channelManager.cleanUpInFlightMessages();
  }

  @Override
  public void postProcess(final IChannelManager channelManager, ExecutorService executorService) {
    executorService.execute(
        new Runnable() {
          @Override public void run() {
            channelManager.reSendPacket(getHeader().getAckAsRange().getFirst(), getHeader()
                .getAckAsRange().getSecond());
          }
        });
    executorService.execute(new Runnable() {
      @Override public void run() {
        channelManager.reactOnAcknowledge();
      }
    });
  }

  public class AckHeader extends Header {
    public static final int LENGTH = 10;
    public static final int ACK_OFFSET_START = 2;
    public static final int ACK_OFFSET_END = 6;

    @Override
    public int getPayloadSize() {
      return 0;
    }

    @Override
    protected int getTotalSize() {
      return AckHeader.LENGTH;
    }

    @Override
    public int getHeaderLength() {
      return AckHeader.LENGTH;
    }

    @Override
    public int getAck() {
      return byteBuffer.getInt(offset + ACK_OFFSET_START);
    }

    public int getUpperRange() {return byteBuffer.getInt(offset + ACK_OFFSET_END);}

    @Override
    public String toString() {
      return "(PT: ACK) = (" + getPacketType() + ": " + getAck() +  ") ";
    }

  }

  public static class Parser implements Packet.Parser {
    @Override
    public Pair<Integer, IPacket> wrapBytesToPacket(IChannelManager channelManager, byte[] data) throws InterruptedException {
      Pair<Integer, Integer> ackRange = channelManager.getAckManager().recentMissingAckRange();
      byte [] header = new byte[AckHeader.LENGTH];
      ByteBuffer byteBuffer = ByteBuffer.wrap(header);
      byteBuffer.putShort(PacketType.ACK.type);
      byteBuffer.putInt(AckHeader.ACK_OFFSET_START, ackRange.getFirst());
      byteBuffer.putInt(AckHeader.ACK_OFFSET_END, ackRange.getSecond());

      IPacket ackPacket = new AckPacket(ByteBuffer.wrap(header), 0);

      ackPacket.put(header);
      // seq is ignored as ack packet is not on direct buffer
      return Pair.create(Constants.IGNORE_SEQUENCE, ackPacket);

    }

    @Override
    public Packet apply(Pair<ByteBuffer, Integer> byteBufferIntegerPair) {
      return new AckPacket(byteBufferIntegerPair.getFirst(), byteBufferIntegerPair.getSecond());
    }
  }
}
