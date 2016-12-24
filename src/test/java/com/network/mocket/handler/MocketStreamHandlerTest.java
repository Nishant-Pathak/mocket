package com.network.mocket.handler;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MocketStreamHandlerTest {

  private MocketStreamHandler<Object> streamHandler;

  @Before
  public void setUp() throws Exception {
    streamHandler = new DefaultHandler();
  }

  @Test
  public void encode() throws Exception {
    byte[] obj = new byte[] {1, 4};
    assertSame(obj, streamHandler.encode(obj));
  }

  @Test
  public void decode() throws Exception {
    byte[] obj = new byte[] {1, 4};
    assertSame(obj, streamHandler.decode(obj));
  }

}