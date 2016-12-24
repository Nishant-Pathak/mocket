package com.network.mocket.example.typeSafeReadWrite;

import com.network.mocket.handler.MocketStreamHandler;

import java.nio.ByteBuffer;
import java.text.ParseException;

public class NumberHandler implements MocketStreamHandler<Integer> {
  @Override
  public byte[] encode(Integer in) throws ParseException {
    if (in == null) {
      return null;
    }
    byte[] data = new byte[4];
    ByteBuffer.wrap(data).putInt(in);
    return data;
  }

  @Override
  public Integer decode(byte[] out) throws ParseException {
    return out == null || out.length == 0? null: ByteBuffer.wrap(out).getInt();
  }
}
