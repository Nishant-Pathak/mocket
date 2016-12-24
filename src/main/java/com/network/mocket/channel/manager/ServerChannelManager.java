package com.network.mocket.channel.manager;

import com.network.mocket.channel.ChannelCommons;
import com.network.mocket.packet.PacketManager;

import java.net.SocketAddress;
import java.util.concurrent.ScheduledExecutorService;

public class ServerChannelManager extends ChannelManager {

  public ServerChannelManager(
      PacketManager packetManager,
      ChannelCommons channel,
      SocketAddress address,
      ScheduledExecutorService executorService,
      boolean appReliabilityEnabled) {
    super(packetManager, channel, address, executorService, appReliabilityEnabled);
  }

  @Override
  public void reactOnAcknowledge() {
    executorService.execute(new SeqUpdateRunnable());
  }

  @Override
  void localHouseKeeping() {

  }
}
