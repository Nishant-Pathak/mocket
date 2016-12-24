package com.network.mocket.allocator.buffer;

import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A pool of byte buffers that can be shared by multiple concurrent
 * clients.
 */
public class ByteBufferPool {

  /**
   * The shared collection of byte buffers.
   */
  private final BlockingQueue<ByteBuffer> byteBuffers;

  /**
   * The size of each byte buffer.
   */
  private final int bufferSize;

  /**
   * Count of max bytebuffer in this pool
   */
  private final int bufferCount;


  /**
   * Creates a new pool.
   */
  private ByteBufferPool(int bufferSize, int bufferCount) {
    this.bufferSize = bufferSize;
    this.bufferCount = bufferCount;
    byteBuffers = new ArrayBlockingQueue<>(bufferCount);
    for (int i = 0; i < bufferCount; i++) {
      try {
        byteBuffers.put(ByteBuffer.allocate(bufferSize));
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }


  /**
   * Creates a new pool.
   */
  public static ByteBufferPool newInstance(int bufferSize, int bufferCount) {
    return new ByteBufferPool(bufferSize, bufferCount);
  }

  /**
   * Creates a new allocator associated with this pool.
   * The allocator will allow its client to allocate and release
   * buffers.
   */
  public ByteBufferAllocator newAllocator() {
    return new ByteBufferAllocatorImpl();
  }

  private static final Logger LOGGER = Logger.getLogger( ByteBufferPool.class.getName() );

  /**
   * The allocator implementation.
   */
  private final class ByteBufferAllocatorImpl implements ByteBufferAllocator {

    /**
     * Creates a new allocator.
     */
    private ByteBufferAllocatorImpl() {
    }


    /**
     * {@inheritDoc}
     */
    @Override
    synchronized public ByteBuffer allocate() throws InterruptedException {
      LOGGER.log(Level.FINER, "ByteBufferPool has  {0} byteBuffers available", byteBuffers.size());
      ByteBuffer byteBuffer = byteBuffers.poll();
      if (byteBuffer == null) {
        if (byteBuffers.size() < bufferCount) {
          byteBuffer = ByteBuffer.allocateDirect(bufferSize);
        } else {
          byteBuffer = byteBuffers.take();

        }
      }
      LOGGER.log(Level.FINER, "ByteBufferPool after allocating, {0} readily available in pool", byteBuffers.size());
      return byteBuffer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    synchronized public void release(ByteBuffer byteBuffer) {
      byteBuffer.clear();
      byteBuffers.add(byteBuffer);
      LOGGER.log(Level.FINER, "ByteBufferPool after releasing {0} available", byteBuffers.size());
    }
  }
}

