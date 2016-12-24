package com.network.mocket.packet;

import com.network.mocket.Constants;
import com.network.mocket.channel.manager.IChannelManager;
import com.network.mocket.helper.Pair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.function.Function;

public abstract class Packet implements IPacket {
  protected final ByteBuffer byteBuffer;
  private final long createdTime;
  final int offset;
  Header header;

  public Packet(ByteBuffer byteBuffer, int offset) {
    this.byteBuffer = byteBuffer;
    this.offset = offset;
    this.createdTime = System.nanoTime();
  }

  @Override
  public Header getHeader() {
    return header;
  }

  @Override
  public int getSize() {
    return header.getTotalSize();
  }

  @Override
  public Packet put(byte[] data) {
    synchronized (byteBuffer) {
      byteBuffer.put(data, offset, data.length);
    }
    return this;
  }

  @Override
  public byte[] readPayload() {
    return read(header.getHeaderLength(), header.getPayloadSize());
  }

  public byte[] read(int startIndex, int length) {
    byte[] data = new byte[length];
    try {
      synchronized (byteBuffer) {
        byteBuffer.rewind();
        byteBuffer.position(offset + startIndex);
        byteBuffer.get(data, 0, length);
      }
    } catch (BufferUnderflowException ex) {
      ex.printStackTrace();
      throw ex;
    }
    return data;
  }

  @Override
  public int compareTo(Object o) {
    return (int) (this.createdTime - ((Packet) o).getCreatedTime());
  }

  @Override
  public long getCreatedTime() {
    return createdTime;
  }

  @Override
  public byte[] read() {
    return read(0, getSize());
  }

  public void process(IChannelManager channelManager) {
    channelManager.ackSeen(getHeader().getSequenceNumber());
    channelManager.getPacketManager().setDeliveredSuccessfully(getHeader().getAck());
    channelManager.addIncomingPacket(header.getSequenceNumber(), this);
    channelManager.cleanUpInFlightMessages();
  }


  public abstract class Header {
    /**
     * 2 bytes packet type
     * 4 bytes seq number
     */
    public static final int LENGTH = 10;
    public static final int SEQUENCE = 2;
    public static final int ACK = 6;

    public PacketType getPacketType() {
      return PacketType.values()[byteBuffer.getShort(offset)];
    }

    public void setPacketType(PacketType type) {
      byteBuffer.putShort(offset, type.type);
    }


    public int getSequenceNumber() {
      return byteBuffer.getInt(offset + SEQUENCE);
    }

    public void setSequenceNumber(int seq) {
      byteBuffer.putInt(offset + SEQUENCE, seq);
    }

    public int getAck() {
      return byteBuffer.getInt(offset + ACK);
    }

    public void setAck(int ack) {
      byteBuffer.putInt(offset + ACK, ack);
    }


    public abstract int getPayloadSize();

    protected abstract int getTotalSize();

    public abstract int getHeaderLength();

    @Override
    public String toString() {
      return "(PT: Seq, Ack, size) = (" + getPacketType() + ":" + getSequenceNumber() + ", " + getAck() + ", " + getSize() +  ") ";
    }

    public Pair<Integer, Integer> getAckAsRange() {
      if (Packet.this instanceof AckPacket) {
        AckPacket.AckHeader ackHeader = (AckPacket.AckHeader) Packet.Header.this;
        return Pair.create(getAck(), ackHeader.getUpperRange());
      }
      return Pair.create(getAck(), getAck());
    }
  }

  public interface Parser extends Function<Pair<ByteBuffer, Integer>, Packet> {
    Pair<Integer, IPacket> wrapBytesToPacket(final IChannelManager channelManager, final byte[] data) throws InterruptedException;
  }
  @Override
  public String toString() {
    return "Packet{" +
        "byteBuffer=" + byteBuffer +
        ", createdTime=" + createdTime +
        ", offset=" + offset +
        ", header=" + header +
        '}';
  }

  public static String dumpPacket(byte [] data) {
    int byteCount = 0;
    ByteBuffer output = ByteBuffer.wrap(data);
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    while (output.hasRemaining()) {
      // this loop is for one line
      try {
        stream.write(String.format("%04X", byteCount).getBytes());
        stream.write(" ".getBytes());
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 16; i++) {
          if(!output.hasRemaining()){
            stream.write(Constants.SPACE.getBytes());
            stream.write(Constants.SPACE.getBytes());
            stream.write(Constants.SPACE.getBytes());
          }//break;
          else {
            byte b = output.get();
            stream.write(String.format("%02X", b).getBytes());
            stringBuilder.append(String.format("%c", b & 0xFF));
            stream.write(Constants.SPACE.getBytes());
          }
          if (i == 7) stream.write(Constants.SPACE.getBytes());
        }
        stream.write(Constants.TAB.getBytes());
        stream.write(stringBuilder.toString().getBytes());
        stream.write(Constants.NEW_LINE.getBytes());
        stringBuilder.setLength(0);
      } catch (IOException e) {
        e.printStackTrace();
      }
      byteCount += 16;
    }
    return stream.toString();

  }

  public String dumpPacket() {
    return dumpPacket(read());
  }
}
