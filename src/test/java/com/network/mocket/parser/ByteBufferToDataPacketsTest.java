/*
package com.network.mocket.parser;

import com.network.mocket.packet.DataPacket;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.Assert.*;

public class ByteBufferToDataPacketsTest {

  private byte[] data;

  @Before
  public void setUp() throws Exception {
    data = new byte[]{0x1, 0x3 , 0x5 , 0x6};
  }

  @Test
  public void parseByteBufferToDataPacket() throws Exception {
    ByteBuffer byteBuffer = createPacket();
    DataPacket dataPacket = new ByteBufferToDataPackets().apply(byteBuffer).get(0);
    assertEquals(dataPacket.getDataHeader().getSequenceNumber(), 10);
    assertEquals(dataPacket.getDataHeader().getOffset(), 2012);
    assertEquals(dataPacket.getDataHeader().getAck(), 99);
    assertArrayEquals(data, dataPacket.readPayload());
  }

  @Test
  public void parseByteBufferTo2DataPacket() throws Exception {
    ByteBuffer byteBuffer = create2Packet();
    List<DataPacket> dataPackets = new ByteBufferToDataPackets().apply(byteBuffer);
    assertDataPacket(dataPackets.get(0));
    flag = true;
    assertDataPacket(dataPackets.get(1));
  }
  boolean flag = false;

  private void assertDataPacket(DataPacket dataPacket) {
    assertEquals(dataPacket.getDataHeader().getSequenceNumber(), flag?300: 10);
    assertEquals(dataPacket.getDataHeader().getOffset(), 2012);
    assertEquals(dataPacket.getDataHeader().getAck(), 99);
    assertArrayEquals(data, dataPacket.readPayload());

  }


  private void createPacketInternal(ByteBuffer byteBuffer, int offSet) {
    // 16 bytes header + 4 bytes data
    byteBuffer.putInt(offSet + 0, offSet == 20? 300: 10); // seq no
    byteBuffer.putInt(offSet + 4, 2012); // offset
    byteBuffer.putInt(offSet + 8, 20); // size
    byteBuffer.putInt(offSet + 12, 99); // ack
    byteBuffer.position(offSet + 16);
    byteBuffer.put(data);

  }

  private ByteBuffer createPacket() {
    ByteBuffer byteBuffer = ByteBuffer.allocate(20);
    createPacketInternal(byteBuffer, 0);
    return byteBuffer;
  }

  private ByteBuffer create2Packet() {
    ByteBuffer byteBuffer = ByteBuffer.allocate(40);
    createPacketInternal(byteBuffer, 0);
    createPacketInternal(byteBuffer, 20);
    return byteBuffer;
  }
}*/
