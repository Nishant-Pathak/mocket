package com.network.mocket.utils;

import com.network.mocket.packet.ChannelRequestPacket;
import com.network.mocket.packet.Packet;

import java.util.function.Function;

import static org.junit.Assert.*;

public class RequestPacketTest implements Function<Packet, Object> {
  @Override
  public Object apply(Packet packet) {
    assertEquals(ChannelRequestPacket.ChannelRequestHeader.CHANNEL_REQ_HEADER_LEN, packet.getSize());
    return null;
  }
}
