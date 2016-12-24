package com.network.mocket.handler;

import java.text.ParseException;

public interface MocketStreamHandler<P> {
  byte [] encode(P in) throws ParseException;
  P decode(byte [] out) throws ParseException;
}
