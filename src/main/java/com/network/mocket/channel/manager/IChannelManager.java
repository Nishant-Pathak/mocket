package com.network.mocket.channel.manager;

import com.network.mocket.helper.AckManager;
import com.network.mocket.packet.IPacket;
import com.network.mocket.packet.PacketManager;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public interface IChannelManager {
  void setChannelId(UUID uuid);

  void putInFlightPacket(Integer seq, IPacket packet);

  IPacket removeFromIncomingPackets(Integer seq);
  IPacket getIncomingPackets(Integer seq);

  AtomicInteger getIncomingPacketConsumedTill();

  void addIncomingPacket(int sequenceNumber, IPacket packet);

  void ackSeen(int ack);

  boolean reSendPacketWithIndex(int seq);

  PacketManager getPacketManager();

  void cleanUpInFlightMessages();

  boolean canIgnore(IPacket packet);

  void reactOnAcknowledge();

  void setLastSendTime(long lastSendTime);

  void setLastReceiveTime(long lastReceiveTime);

  void registerChannel() throws InterruptedException, IOException;

  void reSendPacket(int start, int end);

   AckManager getAckManager();
}
