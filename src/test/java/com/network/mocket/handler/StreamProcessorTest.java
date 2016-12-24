package com.network.mocket.handler;

import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;

import static org.junit.Assert.*;

public class StreamProcessorTest {

  private StreamProcessor streamProcessor;

  @Before
  public void setUp() throws Exception {
    streamProcessor = new StreamProcessor(new LinkedList<>());
  }

  @Test
  public void decode() throws Exception {
    Object data = new byte []{};
    assertEquals(data, streamProcessor.decode(data));
  }

  @Test
  public void encode() throws Exception {
    Object data = new byte []{};
    assertEquals(data, streamProcessor.encode(data));
  }

}