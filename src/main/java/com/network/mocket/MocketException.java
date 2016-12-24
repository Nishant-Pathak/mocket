package com.network.mocket;

public class MocketException extends Exception {
  public MocketException(Exception e) {
    super(e);
  }

  public MocketException(String cause) {
    super(cause);
  }
}
