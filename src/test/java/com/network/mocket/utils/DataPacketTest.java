package com.network.mocket.utils;

import com.network.mocket.packet.DataPacket;
import com.network.mocket.packet.PacketType;

import java.util.function.Function;

import static org.junit.Assert.*;

public class DataPacketTest implements Function<DataPacket, Object> {
  @Override
  public Object apply(DataPacket dataPacket) {
    assertTrue(dataPacket.getSize() >= DataPacket.DataHeader.DATA_HEADER_LENGTH);
    assertEquals(dataPacket.getHeader().getPacketType(), PacketType.DATA);
    assertArrayEquals("hello india\n".getBytes(), dataPacket.readPayload());
    return null;
  }
}
