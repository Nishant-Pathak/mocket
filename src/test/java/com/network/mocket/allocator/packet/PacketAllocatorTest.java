package com.network.mocket.allocator.packet;

import com.network.mocket.allocator.buffer.ByteBufferAllocator;
import com.network.mocket.allocator.buffer.ByteBufferPool;
import com.network.mocket.packet.IPacket;
import com.network.mocket.packet.PacketType;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

public class PacketAllocatorTest {
  private PacketAllocator packetAllocator;

  @Before
  public void setUp() throws Exception {
    ByteBufferPool byteBufferPool = ByteBufferPool.newInstance(1024, 10);
    PacketPool packetPool = PacketPool.newInstance(byteBufferPool.newAllocator());
    packetAllocator = packetPool.newAllocator();
  }

  @Test
  public void allocate() throws Exception {
    IPacket packet = packetAllocator.allocate(18, PacketType.DATA);
    assertNotNull(packet);
  }

  @Test
  public void release() throws Exception {
    IPacket packet = packetAllocator.allocate(18, PacketType.DATA);
    assertNotNull(packet);
    packetAllocator.release(packet);
  }

}