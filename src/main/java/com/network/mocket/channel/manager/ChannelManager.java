package com.network.mocket.channel.manager;

import com.network.mocket.Constants;
import com.network.mocket.channel.ChannelCommons;
import com.network.mocket.helper.AckManager;
import com.network.mocket.helper.AckManagerImpl;
import com.network.mocket.helper.Pair;
import com.network.mocket.helper.Utils;
import com.network.mocket.packet.IPacket;
import com.network.mocket.packet.PacketManager;
import com.network.mocket.packet.PacketType;
import com.network.mocket.parser.ByteBufferToPackets;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ChannelManager implements IChannelManager {
  private static final Logger LOGGER = Logger.getLogger(ChannelManager.class.getName());
  protected final ScheduledExecutorService executorService;
  private final ChannelCommons channel;
  private final boolean appReliabilityEnabled;
  private final PacketManager packetManager;
  private final ConcurrentHashMap<Integer, IPacket> inFlightPackets;
  private final ConcurrentHashMap<Integer, IPacket> inComingPackets;
  protected final SocketAddress socketAddress;
  private final AtomicInteger incomingPacketConsumedTill;
  private final AckManager ackManager;
  AtomicLong lastSendTime;
  AtomicLong lastReceiveTime;

  private final AtomicInteger inFlightSize; // in flight size in bytes
  private final AtomicInteger inComingSize; // in coming size in bytes


  ChannelManager(
      PacketManager packetManager,
      ChannelCommons channel,
      SocketAddress address,
      ScheduledExecutorService executorService,
      boolean appReliabilityEnabled
  ) {
    this.packetManager = packetManager;
    this.channel = channel;
    this.socketAddress = address;
    this.executorService = executorService;
    this.appReliabilityEnabled = appReliabilityEnabled;

    inFlightPackets = new ConcurrentHashMap<>();
    inComingPackets = new ConcurrentHashMap<>();
    incomingPacketConsumedTill = new AtomicInteger(-1);
    ackManager = new AckManagerImpl();


    // initialize to old values
    lastSendTime = new AtomicLong(Utils.now());
    lastReceiveTime = new AtomicLong(Utils.now());

    executorService.scheduleWithFixedDelay(new HouseKeeping(), 5, 1, TimeUnit.SECONDS);
    executorService.scheduleWithFixedDelay(new ChannelManagerLog(), 1, 15, TimeUnit.SECONDS);
    inFlightSize = new AtomicInteger(0);
    inComingSize = new AtomicInteger(0);
  }

  public void cleanUpInFlightMessages() {
    for (Map.Entry<Integer, IPacket> seqPacket : inFlightPackets.entrySet()) {
      if (seqPacket.getKey() <= packetManager.getDeliveredSuccessfully()) {
        LOGGER.log(
            Level.FINE,
            "successfully delivered packet #{0}, removing it",
            seqPacket.getKey()
        );
        inFlightSize.addAndGet(-1 * seqPacket.getValue().getSize());
        packetManager.releasePacket(seqPacket.getValue());
        inFlightPackets.remove(seqPacket.getKey());
      }
    }
  }

  public AckManager getAckManager() {
    return ackManager;
  }

  @Override
  public boolean reSendPacketWithIndex(int seq) {
    if (inFlightPackets.containsKey(seq)) {
      IPacket packet = inFlightPackets.get(seq);
      packet.getHeader().setAck(ackManager.getLastSeenAck());
      LOGGER.log(Level.WARNING, "Sending #{0} again to {1}", new Object[]{packet, socketAddress});
      long now = System.nanoTime();
      if (now - packet.getCreatedTime() > Constants.ONE_SEC) {
        try {
          write(packet, seq);
          return true;
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return false;

  }

  protected void write(IPacket packet, Integer seq) throws IOException {
    channel.write(ChannelManager.this, socketAddress, packet, seq);
  }

  @Override
  public void reSendPacket(int start, int end) {
    if (start == end) {
      // optimization to create voids :)
      reSendLastPacket(start + 1);
    } else {
      int totalPacketResend = 0;
      for(int i = start + 1; i< end; i++) {
        if(reSendPacketWithIndex(i)) totalPacketResend++;
      }
      LOGGER.log(Level.FINE, "#{0} packet resend", totalPacketResend);
    }
  }

  private void reSendLastPacket(int currentIndex) {
    int last = currentIndex;
    for (Integer nLast: inFlightPackets.keySet()) {
      if(nLast > last){
        last = nLast;
      }
    }
    reSendPacketWithIndex(last);
  }

  abstract void localHouseKeeping();

  public PacketManager getPacketManager() {
    return packetManager;
  }

  public AtomicInteger getIncomingPacketConsumedTill() {
    return incomingPacketConsumedTill;
  }

  @Override
  public void addIncomingPacket(int sequenceNumber, IPacket packet) {
    inComingPackets.put(sequenceNumber, packet);
    inComingSize.addAndGet(packet.getSize());
  }

  @Override
  public void ackSeen(int ack) {
    LOGGER.log(Level.FINEST, "Sequence number got {0}", ack);
    ackManager.ackSeen(ack);
  }

  public void setChannelId(UUID channelId) {
    packetManager.setChannelId(channelId);
  }

  @Override
  public void putInFlightPacket(Integer seq, IPacket packet) {
    if (!inFlightPackets.containsKey(seq)) {
      LOGGER.log(Level.FINEST, "Adding seq {0} to inflight", seq);
      inFlightPackets.put(seq, packet);
      inFlightSize.addAndGet(packet.getSize());
    }
  }

  @Override
  public IPacket removeFromIncomingPackets(Integer seq){
    IPacket packet = inComingPackets.remove(seq);
    inComingSize.getAndAdd(-1 * packet.getSize());
    return packet;
  }

  @Override
  public IPacket getIncomingPackets(Integer seq) {
    return inComingPackets.get(seq);
  }

  @Override
  public boolean canIgnore(IPacket packet) {
    boolean canIgnored =
        !PacketType.ACK.equals(packet.getHeader().getPacketType()) &&
            !PacketType.NACK.equals(packet.getHeader().getPacketType()) &&
            (inComingPackets.containsKey(packet.getHeader().getSequenceNumber()) ||
                packet.getHeader().getSequenceNumber() <= incomingPacketConsumedTill.get());
    if (canIgnored) {
      LOGGER.log(Level.WARNING, "Dropping packet as it can be ignored {0}", packet);
    }
    return canIgnored;
  }

  public void setLastSendTime(long lastSendTime) {
    this.lastSendTime.set(lastSendTime);
  }

  public void setLastReceiveTime(long lastReceiveTime) {
    this.lastReceiveTime.set(lastReceiveTime);
  }

  @Override
  public String toString() {
    return "ChannelManager{" +
        "packetManager=" + packetManager +
        ", ackManager=" + ackManager +
        ", inFlightPackets=" + inFlightPackets +
        ", inFlightSize=" + inFlightSize +
        ", inComingPackets=" + inComingPackets +
        ", inComingSize=" + inComingSize +
        ", socketAddress=" + socketAddress +
        ", channel=" + channel +
        ", incomingPacketConsumedTill=" + incomingPacketConsumedTill +
        ", lastSendTime=" + lastSendTime +
        ", lastReceiveTime=" + lastReceiveTime +
        '}';
  }

  private String dump() {
    return "*****************" +
        "\n" + "PacketManager: " + packetManager.dump() +
        "\n" + "Ack Manager: " + ackManager.dump() +
        "\n" + "In flight packets: " + inFlightPackets.size() +
        "\n" + "In coming packets: " + inComingPackets.size() +
        "\n" + "Remote Socket: " + socketAddress +
        "\n" + "InComing packet consumed till: " + incomingPacketConsumedTill +
        "\n" + "*****************";
  }

  private class ChannelManagerLog implements Runnable {
    @Override
    public void run() {
      LOGGER.log(Level.FINEST, ChannelManager.this.dump());
    }
  }

  class SeqUpdateRunnable implements Runnable {
    @Override
    public void run() {
      Pair<Integer, IPacket> packetPair;
      try {
        if (ackManager.preferNackVsAck()) {
          packetPair = ByteBufferToPackets.wrapBytesToPackets(ChannelManager.this, PacketType.NACK, null);

        } else {
          packetPair = ByteBufferToPackets.wrapBytesToPackets(ChannelManager.this, PacketType.ACK, null);
        }
        write(packetPair.getSecond(), packetPair.getFirst());
      } catch (IOException | InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private class HouseKeeping implements Runnable {

    @Override
    public void run() {
      if (appReliabilityEnabled) {
        localHouseKeeping();
      }
      // check for pending messages and resend them
      cleanUpInFlightMessages();
    }
  }
}
