package com.network.mocket.handler;

import java.text.ParseException;

public interface MocketStreamHandler<P> {
  /**
   * Used to encode object to byte stream
   * @param in inout object of tye {P}
   * @return byte array representing object
   * @throws ParseException throws if fails to encode or decode
   */
  byte [] encode(P in) throws ParseException;

  /**
   * Used to decode bytestream to object
   * @param out inout object of tye {P}
   * @return byte array representing object
   * @throws ParseException throws if fails to encode or decode
   */
  P decode(byte [] out) throws ParseException;
}
