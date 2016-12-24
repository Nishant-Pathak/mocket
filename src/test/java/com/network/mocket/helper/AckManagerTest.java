package com.network.mocket.helper;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class AckManagerTest {

  private AckManager ackManager;

  @Before
  public void setUp() throws Exception {
    ackManager = new AckManagerImpl();

  }

  @Test
  public void getLastSeenAck() throws Exception {
    assertEquals(-1, ackManager.getLastSeenAck());
    ackManager.ackSeen(0);
    ackManager.ackSeen(1);
    ackManager.ackSeen(2);
    assertEquals(2, ackManager.getLastSeenAck());
    ackManager.ackSeen(4);
    assertEquals(2, ackManager.getLastSeenAck());
    ackManager.ackSeen(3);
    assertEquals(4, ackManager.getLastSeenAck());
  }
}