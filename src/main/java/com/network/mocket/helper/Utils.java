package com.network.mocket.helper;

public class Utils {
  public static byte[] concat(byte[] a, byte[] b) {
    if (a == null) return b;
    int aLen = a.length;
    int bLen = b.length;
    byte[] c= new byte[aLen+bLen];
    System.arraycopy(a, 0, c, 0, aLen);
    System.arraycopy(b, 0, c, aLen, bLen);
    return c;
  }

  public static byte [] rotation () {
    byte[] rotation = new byte[95*2];
    for (byte i = ' '; i <= '~'; i++) {
      rotation[i-' '] = i;
      rotation[i+95-' '] = i;
    }
    return rotation;
  }

  public static byte[] hexStringToByteArray(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
          + Character.digit(s.charAt(i+1), 16));
    }
    return data;
  }

  public static long now() {
    return System.nanoTime();
  }

}
