package com.network.mocket.allocator.packet;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BufferReferenceCountTest {

  private BufferReferenceCount bufferReferenceCount;
  @Before
  public void setUp() throws Exception {
    bufferReferenceCount = new BufferReferenceCount(0, 512);
  }

  @Test
  public void decrement() throws Exception {
    bufferReferenceCount.markNewBuffer(100);
    assertEquals(bufferReferenceCount.getReference(), 1);
    bufferReferenceCount.decrement();
    assertEquals(bufferReferenceCount.getReference(), 0);
  }

  @Test
  public void mark1NewBuffer() throws Exception {
    bufferReferenceCount.markNewBuffer(100);
    assertEquals(bufferReferenceCount.getReference(), 1);
    assertEquals(bufferReferenceCount.getFreeFrom(), 100);
    assertEquals(bufferReferenceCount.getFreeSize(), 412);
  }

  @Test
  public void mark2NewBuffer() throws Exception {
    bufferReferenceCount.markNewBuffer(100);
    bufferReferenceCount.markNewBuffer(200);
    assertEquals(2, bufferReferenceCount.getReference());
    assertEquals(300, bufferReferenceCount.getFreeFrom());
    assertEquals(212, bufferReferenceCount.getFreeSize());
  }

  @Test
  public void markBufferFull() throws Exception {
    bufferReferenceCount.markNewBuffer(512);
    try {
      bufferReferenceCount.markNewBuffer(1);
      fail("should have thrown an exception");
    } catch (RuntimeException re) {
      assertTrue(true);
    }
  }
}