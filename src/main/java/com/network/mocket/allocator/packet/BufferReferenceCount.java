package com.network.mocket.allocator.packet;

/**
 * Buffer meta data object used to map packet to the buffer.
 */
class BufferReferenceCount {

  /**
   * packets referring the buffer.
   */
  private int reference;

  /**
   * free offset of the buffer
   */
  private int freeFrom;

  /**
   * free size of the buffer
   */
  private int freeSize;

  BufferReferenceCount(int freeFrom, int freeSize) {
    this.reference = 0;
    this.freeFrom = freeFrom;
    this.freeSize = freeSize;
  }

  synchronized void decrement() {
    reference--;
  }

  int getReference() {
    return reference;
  }

  int getFreeFrom() {
    return freeFrom;
  }

  int getFreeSize() {
    return freeSize;
  }

  @Override
  public String toString() {
    return "BufferReferenceCount{" +
        "reference=" + reference +
        ", freeFrom=" + freeFrom +
        ", freeSize=" + freeSize +
        '}';
  }

  synchronized void markNewBuffer(int size) {
    if (this.freeSize - size < 0) {
      throw new RuntimeException();
    }
    this.reference++;
    this.freeFrom += size;
    this.freeSize-= size;
  }
}
