package com.network.mocket.helper;

import org.junit.Before;
import org.junit.Test;
import sun.awt.Mutex;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import static org.junit.Assert.*;

public class AckManagerImplTest {

  private AckManager ackManager;
  private static int MAX_NACK_SIZE = 2;

  @Before
  public void setUp() throws Exception {
    ackManager = new AckManagerImpl();
  }

  @Test
  public void getLastSeenAck() throws Exception {
    assertNotEquals(0, ackManager.getLastSeenAck());
    ackManager.ackSeen(0);
    assertEquals(0, ackManager.getLastSeenAck());
    ackManager.ackSeen(1);
    ackManager.ackSeen(2);
    ackManager.ackSeen(3);
    ackManager.ackSeen(5);
    assertEquals(3, ackManager.getLastSeenAck());
    ackManager.ackSeen(4);
    assertEquals(5, ackManager.getLastSeenAck());
  }

  @Test
  public void getVoids1() throws Exception {
    ackManager.ackSeen(0);
    ackManager.ackSeen(2);
    ackManager.ackSeen(3);
    ackManager.ackSeen(5);
    ackManager.ackSeen(4);
    assertArrayEquals(Collections.singletonList(1).toArray(), ackManager.getVoids(MAX_NACK_SIZE).toArray());
  }

  @Test
  public void getVoids2() throws Exception {
    ackManager.ackSeen(1);
    ackManager.ackSeen(2);
    ackManager.ackSeen(4);
    ackManager.ackSeen(6);
    assertArrayEquals(Arrays.asList(0, 3).toArray(), ackManager.getVoids(MAX_NACK_SIZE).toArray());
  }

  @Test
  public void getVoids3() throws Exception {
    ackManager.ackSeen(1);
    ackManager.ackSeen(2);
    ackManager.ackSeen(4);
    ackManager.ackSeen(6);
    assertArrayEquals(Arrays.asList(0, 3, 5).toArray(), ackManager.getVoids(3).toArray());
  }

  @Test
  public void getVoids4() throws Exception {
    ackManager.ackSeen(-1);
    ackManager.ackSeen(1);
    ackManager.ackSeen(2);
    ackManager.ackSeen(3);
    assertArrayEquals(Arrays.asList(0).toArray(), ackManager.getVoids(3).toArray());
  }


  @Test
  public void recentMissingAckRange1() throws Exception {
    ackManager.ackSeen(5);
    assertEquals(Pair.create(-1, 5), ackManager.recentMissingAckRange());
  }

  @Test
  public void recentMissingAckRange2() throws Exception {
    ackManager.ackSeen(0);
    ackManager.ackSeen(4);
    assertEquals(Pair.create(0, 4), ackManager.recentMissingAckRange());
  }

  @Test
  public void recentMissingAckRange3() throws Exception {
    ackManager.ackSeen(1);
    ackManager.ackSeen(4);
    assertEquals(Pair.create(-1, 1), ackManager.recentMissingAckRange());
  }

  @Test
  public void recentMissingAckRange4() throws Exception {
    ackManager.ackSeen(0);
    ackManager.ackSeen(1);
    ackManager.ackSeen(4);
    assertEquals(Pair.create(1, 4), ackManager.recentMissingAckRange());
  }


  @Test
  public void recentMissingAckRange5() throws Exception {
    ackManager.ackSeen(0);
    ackManager.ackSeen(1);
    ackManager.ackSeen(5);
    ackManager.ackSeen(3);
    ackManager.ackSeen(100);
    assertEquals(Pair.create(1, 3), ackManager.recentMissingAckRange());

  }
}