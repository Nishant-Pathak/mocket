package com.network.mocket.utils;

import com.network.mocket.packet.AckPacket;
import com.network.mocket.packet.ChannelRequestPacket;
import com.network.mocket.packet.Packet;

import java.util.function.Function;

import static org.junit.Assert.*;

public class AckPacketTest implements Function<Packet, Object> {
  @Override
  public Object apply(Packet packet) {
    assertEquals(AckPacket.AckHeader.LENGTH, packet.getSize());

    return null;
  }
}
