package com.network.mocket.allocator.buffer;

import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;

public class ByteBufferAllocatorTest {

  private ByteBufferAllocator byteBufferAllocator;
  private static final int BUFFER_SIZE = 2;
  private static final int BUFFER_COUNT = 2;

  @Before
  public void setUp() throws Exception {
    ByteBufferPool byteBufferPool = ByteBufferPool.newInstance(BUFFER_SIZE, BUFFER_COUNT);
    byteBufferAllocator = byteBufferPool.newAllocator();
  }

  @Test
  public void testSingleAllocateRelease() throws Exception {
    ByteBuffer byteBuffer = byteBufferAllocator.allocate();
    assertByteBuffer(byteBuffer);
    byteBufferAllocator.release(byteBuffer);
  }

  private void assertByteBuffer(ByteBuffer byteBuffer) {
    assertEquals(BUFFER_SIZE, byteBuffer.capacity());
    assertEquals(BUFFER_SIZE, byteBuffer.limit());
    assertEquals(0, byteBuffer.position());
  }


  @Test
  public void testMultithreadAllocateRelease() throws Exception {
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    final LinkedBlockingQueue<ByteBuffer> bufferQueue = new LinkedBlockingQueue<>();

    for (int i = 0; i < 100; i++) {
      executorService.submit(new AllocateRunnable(byteBufferAllocator, bufferQueue));
      executorService.submit(new ReleaseRunnable(byteBufferAllocator, bufferQueue));
    }

    executorService.awaitTermination(1, TimeUnit.SECONDS);
  }

  private class AllocateRunnable implements Runnable {
    private final ByteBufferAllocator allocator;
    private final LinkedBlockingQueue<ByteBuffer> buffers;

    AllocateRunnable(ByteBufferAllocator allocator, LinkedBlockingQueue<ByteBuffer> buffers) {
      this.allocator = allocator;
      this.buffers = buffers;
    }

    @Override
    public void run() {
      try {
        ByteBuffer  byteBuffer = allocator.allocate();
        assertByteBuffer(byteBuffer);
        buffers.offer(byteBuffer);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private class ReleaseRunnable implements Runnable {
    private final ByteBufferAllocator allocator;
    private final LinkedBlockingQueue<ByteBuffer> buffers;

    ReleaseRunnable(ByteBufferAllocator allocator, LinkedBlockingQueue<ByteBuffer> buffers) {
      this.allocator = allocator;
      this.buffers = buffers;
    }

    @Override
    public void run() {
      try {
        allocator.release(buffers.take());
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

}