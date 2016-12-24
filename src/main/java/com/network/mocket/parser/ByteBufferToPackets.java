package com.network.mocket.parser;

import com.network.mocket.channel.manager.IChannelManager;
import com.network.mocket.helper.Pair;
import com.network.mocket.packet.*;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ByteBufferToPackets implements Function<ByteBuffer, List<IPacket>> {
  private static final Logger LOGGER = Logger.getLogger(ByteBufferToPackets.class.getSimpleName());
  private static final ByteBufferToPackets BYTE_BUFFER_TO_PACKETS = new ByteBufferToPackets();
  private static Map<PacketType, Packet.Parser> typePacketParser;

  static {
    typePacketParser = new EnumMap<>(PacketType.class);
    typePacketParser.put(PacketType.DATA, new DataPacket.Parser());
    typePacketParser.put(PacketType.REQUEST, new ChannelRequestPacket.Parser());
    typePacketParser.put(PacketType.ACK, new AckPacket.Parser());
    typePacketParser.put(PacketType.NACK, new NackPacket.Parser());
    typePacketParser = Collections.unmodifiableMap(typePacketParser);
  }

  public static Pair<Integer, IPacket> wrapBytesToPackets(IChannelManager channelManager, PacketType type, byte[] data)
      throws InterruptedException {
    return typePacketParser.get(type).wrapBytesToPacket(channelManager, data);
  }

  public static IPacket wrapByteBuffer(PacketType packetType, ByteBuffer byteBuffer) {
    return typePacketParser.get(packetType).apply(Pair.create(byteBuffer, 0));
  }


  public static List<IPacket> parse(ByteBuffer byteBuffer) {
    return BYTE_BUFFER_TO_PACKETS.apply(byteBuffer);
  }

  @Override
  public List<IPacket> apply(ByteBuffer byteBuffer) {
    List<IPacket> packets = new LinkedList<>();
    int offset = 0;
    while (offset + 2 < byteBuffer.remaining()) {
      PacketType packetType = PacketType.values()[byteBuffer.getShort(offset)];
      IPacket packet = typePacketParser.get(packetType).apply(Pair.create(byteBuffer.asReadOnlyBuffer(), offset));
      if (packet == null) break;
      packets.add(packet);
      LOGGER.log(Level.FINEST, "got packet {0} of {1} size",
          new Object[]{packet.getHeader().getPacketType(), packet.getSize()});
      offset += packet.getSize();
      if (offset > byteBuffer.remaining()) {
        break;
      }
    }
    LOGGER.log(Level.FINE, "parsed #{0} packets of {1} size", new Object[]{packets.size(), offset});
    LOGGER.log(Level.FINEST, "packets are {0}", Arrays.toString(packets.toArray()));
    return packets;
  }
}
