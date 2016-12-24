package com.network.mocket.packet;

import com.network.mocket.utils.AckPacketTest;
import com.network.mocket.utils.DataPacketTest;
import com.network.mocket.utils.RequestPacketTest;
import com.network.mocket.utils.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PacketTest {

  private List<IPacket> packets;
  private static EnumMap<PacketType, Function> packetTest = new EnumMap<>(PacketType.class);

  static  {
    packetTest.put(PacketType.DATA, new DataPacketTest());
    packetTest.put(PacketType.ACK, new AckPacketTest());
    packetTest.put(PacketType.REQUEST, new RequestPacketTest());
  }

  @Before
  public void setUp() throws Exception {
    packets = TestUtils.readAllPackets();
  }

  @Test
  public void getPacket() throws Exception {

    for(IPacket packet: packets) {
      assertTrue(packet.getHeader().getPacketType().isValidType());
      assertNotNull(packetTest.get(packet.getHeader().getPacketType()));
      packetTest.get(packet.getHeader().getPacketType()).apply(packet);
    }
  }
}