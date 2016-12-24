package com.network.mocket.handler;

import java.text.ParseException;

public class DefaultHandler implements MocketStreamHandler<Object> {
  @Override
  public byte[] encode(Object in) throws ParseException {
    return (byte[])in;
  }

  @Override
  public Object decode(byte[] out) throws ParseException {
    return out;
  }
}
