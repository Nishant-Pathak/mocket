package com.network.mocket.allocator.packet;

import com.network.mocket.packet.IPacket;
import com.network.mocket.packet.PacketType;

public interface PacketAllocator {
  /**
   * Creates new packet
   * Check if last byte buffer has space to accumulate this packet, add it there.
   * else allocate new byte buffer.
   * Create packet in that
   * increase reference count
   * return it
   * @param size bytes to be holding in that buffer
   * @param type one of @{@link PacketType}
   * @return new Packet
   * @throws InterruptedException if allocator is exhausted
   */
  IPacket allocate(int size, PacketType type) throws InterruptedException;

  /**
   * Release a packet and the buffer marked with it for the reuse.
   * @param packet the @{@link IPacket} representation of the buffer
   */
  void release(IPacket packet);

  /**
   * to get stats of the packet pool
   * @return String
   */
  String dump();
}
