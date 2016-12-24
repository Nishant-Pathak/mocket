package com.network.mocket.helper;

import java.util.concurrent.atomic.AtomicInteger;

public class SeqGeneratorClient {
  private AtomicInteger sequence;

  public SeqGeneratorClient() {
    sequence = new AtomicInteger(0);
  }

  public int getNextPacketSequence() {
    return sequence.getAndIncrement();
  }

  @Override
  public String toString() {
    return "SeqGeneratorClient{" +
        "sequence=" + sequence +
        '}';
  }
}
