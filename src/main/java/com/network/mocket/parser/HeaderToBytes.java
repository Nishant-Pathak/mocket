package com.network.mocket.parser;

import com.network.mocket.helper.Pair;
import com.network.mocket.packet.Packet;
import com.network.mocket.packet.PacketType;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.BiFunction;

public class HeaderToBytes implements BiFunction<PacketType, Pair<Integer, Integer>, byte[]> {

  public static final HeaderToBytes HEADER_TO_BYTES = new HeaderToBytes();

  @Override
  public byte[] apply(PacketType packetType, Pair<Integer, Integer> seqAck) {
    Objects.requireNonNull(packetType);
    Objects.requireNonNull(seqAck);
    byte [] header = new byte[Packet.Header.LENGTH];
    ByteBuffer byteBuffer = ByteBuffer.wrap(header);
    byteBuffer.putShort(packetType.type);
    byteBuffer.putInt(Packet.Header.SEQUENCE, seqAck.getFirst());
    byteBuffer.putInt(Packet.Header.ACK, seqAck.getSecond());
    return header;

  }
}
