package com.network.mocket.utils;

import com.network.mocket.packet.IPacket;
import com.network.mocket.packet.PacketType;
import com.network.mocket.parser.ByteBufferToPackets;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class TestUtils {
  private static final String PACKET_FILE_NAME = "samplePacket.txt";

  public static IPacket getRandomPacket(PacketType packetType) throws IOException {
    List<IPacket> packets = readAllPackets();
    packets = packets.stream()
        .filter(packet -> packet.getHeader().getPacketType() == packetType)
        .collect(Collectors.toList());
    return packets.get(new Random().nextInt(packets.size()));
  }

  public static List<IPacket> readAllPackets() throws IOException {
    List<IPacket> packets = new LinkedList<>();
    // Read packet from file and populate packets
    String line;
    BufferedReader bufferedReader = new BufferedReader(new FileReader(PACKET_FILE_NAME));
    while ((line = bufferedReader.readLine()) != null) {

      // ignore comments
      if (line.startsWith("#")) continue;

      line = line.replace(" ", "");

      // blank line
      if (line.length() == 0) continue;


      byte[] packetPayload = com.network.mocket.helper.Utils.hexStringToByteArray(line);
      packets.addAll(ByteBufferToPackets.parse(ByteBuffer.wrap(packetPayload)));
    }
    return packets;
  }
}
