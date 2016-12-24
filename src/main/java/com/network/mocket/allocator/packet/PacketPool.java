package com.network.mocket.allocator.packet;

import com.network.mocket.allocator.buffer.ByteBufferAllocator;
import com.network.mocket.helper.Pair;
import com.network.mocket.packet.IPacket;
import com.network.mocket.packet.PacketType;
import com.network.mocket.parser.ByteBufferToPackets;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PacketPool {

  private static final Logger LOGGER = Logger.getLogger( PacketPool.class.getName() );

  private final ByteBufferAllocator byteBufferAllocator;
  private final ConcurrentSkipListMap<IPacket, Pair<ByteBuffer, BufferReferenceCount>> packetBufferMap;

  private PacketPool(ByteBufferAllocator byteBufferAllocator) {
    this.byteBufferAllocator = byteBufferAllocator;
    this.packetBufferMap = new ConcurrentSkipListMap<>();
  }

  /**
   * create new instance of packet pool
   * @param byteBufferAllocator byte buffer use to map packet
   * @return new Instance of @{@link PacketPool}
   */
  public static PacketPool newInstance(ByteBufferAllocator byteBufferAllocator) {
    return new PacketPool(byteBufferAllocator);
  }

  public PacketAllocator newAllocator() {
    return new PacketAllocatorImpl();
  }

  private class PacketAllocatorImpl implements PacketAllocator {

    private PacketAllocatorImpl() {}

    /**
     * {@inheritDoc}
     */
    @Override
    synchronized public IPacket allocate(int size, PacketType type) throws InterruptedException {
      Pair<ByteBuffer, BufferReferenceCount> lastPacket = null;
      ByteBuffer byteBuffer;
      BufferReferenceCount bufferReferenceCount;
      if (packetBufferMap.size() > 0) {
        lastPacket = packetBufferMap.lastEntry().getValue();
      }
      if (lastPacket == null || lastPacket.getSecond().getFreeSize() < size) {
        byteBuffer = byteBufferAllocator.allocate();
        bufferReferenceCount = new BufferReferenceCount(0, byteBuffer.capacity());
        lastPacket = Pair.create(byteBuffer, bufferReferenceCount);
      } else {
        byteBuffer = lastPacket.getFirst();
        bufferReferenceCount = lastPacket.getSecond();
      }

      // pre audit
      ByteBuffer duplicate = byteBuffer.slice();
      duplicate.limit(size);
      byteBuffer.position(byteBuffer.position() + size);

      // audit
      bufferReferenceCount.markNewBuffer(size);

      // create
      IPacket packet = ByteBufferToPackets.wrapByteBuffer(type, duplicate);
      packetBufferMap.put(packet, lastPacket);

      // log
      LOGGER.log(Level.FINEST, "byte buffer: {0} , duplicate {1}, size requested: {2}, packet type: {3}",
          new Object[]{byteBuffer, duplicate, size, type});
      LOGGER.log(Level.FINEST, "reference count: {0}", bufferReferenceCount);
      LOGGER.log(Level.FINER, "PacketPool after allocating 1 packet: {0}", packetBufferMap.size());

      return packet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    synchronized public void release(IPacket packet) {
      LOGGER.log(Level.FINER, "releasing packet: {0}", packet);
      Pair<ByteBuffer, BufferReferenceCount> lastPacket = packetBufferMap.get(packet);
      if (lastPacket == null) {
        return;
      }
      lastPacket.getSecond().decrement();
      int refCount = lastPacket.getSecond().getReference();
      if (0 == refCount) {
        byteBufferAllocator.release(lastPacket.getFirst());
        LOGGER.log(Level.FINE, "PacketPool releasing byte buffer: {0}", packetBufferMap.size());
      }
      packetBufferMap.remove(packet);
      LOGGER.log(Level.FINER, "releasing 1 packet: {0}", packetBufferMap.size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String dump() {
      return PacketPool.this.toString();
    }

  }

  @Override
  public String toString() {
    return "PacketPool{" +
        "packetBufferMap=" + packetBufferMap.size() +
        ", byteBufferAllocator=" + byteBufferAllocator +
        '}';
  }
}
