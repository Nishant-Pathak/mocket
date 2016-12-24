package com.network.mocket.helper;

import org.junit.Test;

import static org.junit.Assert.*;

public class PairTest {
  @Test
  public void create() throws Exception {
    Pair<Integer, Integer> myPair = Pair.create(1, 2);
    assertNotNull(myPair);
  }

  @Test
  public void equals() throws Exception {
    Pair<Integer, Integer> myPair1 = Pair.create(1, 2);
    Pair<Integer, Integer> myPair2 = Pair.create(1, 2);
    assertEquals(myPair1, myPair2);
  }

  @Test
  public void getFirst() throws Exception {
    Pair<Integer, Integer> myPair = Pair.create(1, 2);
    assertEquals(Integer.valueOf(1), myPair.getFirst());
  }

  @Test
  public void getSecond() throws Exception {
    Pair<Integer, Integer> myPair = Pair.create(1, 2);
    assertEquals(Integer.valueOf(2), myPair.getSecond());
  }
}