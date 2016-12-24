package com.network.mocket.allocator.buffer;

import java.nio.ByteBuffer;

/**
 * An object that can allocate and release byte buffers.
 */
public interface ByteBufferAllocator {

  /**
   * Allocates a byte buffer. or waits if buffer pool is exausted
   * @return newly created Byte Buffer
   * @throws InterruptedException if gets interrupted
   */
  ByteBuffer allocate() throws InterruptedException;

  /**
   * Releases a byte buffer to the buffer pool
   * @param byteBuffer to be released
   */
  void release(ByteBuffer byteBuffer);
}
