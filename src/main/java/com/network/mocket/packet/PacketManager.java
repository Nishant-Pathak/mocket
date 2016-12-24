package com.network.mocket.packet;

import com.network.mocket.allocator.packet.PacketAllocator;
import com.network.mocket.helper.AckManager;
import com.network.mocket.helper.AckManagerImpl;
import com.network.mocket.helper.SeqGeneratorClient;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class PacketManager {
  private final SeqGeneratorClient seqGeneratorClient;
  private final AtomicInteger offset;
  public final PacketAllocator allocator;
  private UUID channelId;
  private final AtomicInteger deliveredSuccessfully;

  public PacketManager(PacketAllocator allocator) {
    seqGeneratorClient = new SeqGeneratorClient();
    offset = new AtomicInteger(0);
    this.allocator = allocator;
    deliveredSuccessfully = new AtomicInteger(-1);
  }

  public void setDeliveredSuccessfully(int i) {
    if (i > deliveredSuccessfully.get()) {
      deliveredSuccessfully.set(i);
    }
  }

  public int getDeliveredSuccessfully() {
    return deliveredSuccessfully.get();
  }

  public UUID getChannelId() {
    return channelId;
  }

  public void setChannelId(UUID channelId) {
    this.channelId = channelId;
  }

  public SeqGeneratorClient getSeqGeneratorClient() {
    return seqGeneratorClient;
  }

  public AtomicInteger getOffset() {
    return offset;
  }

  public void releasePacket(IPacket packet) {
    allocator.release(packet);
  }

  @Override
  public String toString() {
    return "PacketManager{" +
        "seqGeneratorClient=" + seqGeneratorClient +
        ", offset=" + offset +
        ", allocator=" + allocator +
        ", channelId=" + channelId +
        ", deliveredSuccessfully=" + deliveredSuccessfully +
        '}';
  }

  public String dump() {
    return  "\n" + "---------------" +
        "\n" + "Current Sequence in use: " + seqGeneratorClient +
        "\n" + "Offset: " + offset +
        "\n" + "Delivered successfully: " + deliveredSuccessfully +
        "\n" + "---------------";
  }
}
