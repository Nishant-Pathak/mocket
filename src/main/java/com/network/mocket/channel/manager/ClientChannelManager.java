package com.network.mocket.channel.manager;

import com.network.mocket.Constants;
import com.network.mocket.channel.ChannelCommons;
import com.network.mocket.helper.Pair;
import com.network.mocket.helper.Utils;
import com.network.mocket.packet.IPacket;
import com.network.mocket.packet.PacketManager;
import com.network.mocket.packet.PacketType;
import com.network.mocket.parser.ByteBufferToPackets;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.ScheduledExecutorService;

final public class ClientChannelManager extends ChannelManager {
  private long lastAckTime = Utils.now();
  private int exponentialBackOff = 1;
  public ClientChannelManager(
      final PacketManager packetManager,
      final ChannelCommons channel,
      final SocketAddress address,
      final ScheduledExecutorService executorService,
      final boolean appReliabilityEnabled
  ) {
    super(packetManager, channel, address, executorService, appReliabilityEnabled);
  }

  /**
   * initiate ack on the client side,
   * note: server will only reply to the ack received and never initiate ack
   */
  @Override
  void localHouseKeeping() {
    // reset exponential back off if some activity happened on wire
    if (lastSendTime.get() > lastAckTime || lastReceiveTime.get() > lastAckTime) {
      exponentialBackOff = 1;
    }

    if (isTimeToAck()) {
      executorService.execute(new SeqUpdateRunnable());
      lastAckTime = Utils.now();
    }

  }

  /**
   * @return true if channel is idle and it is time to ack
   */
  private boolean isTimeToAck() {
    long now = Utils.now();
    if (lastSendTime.get() + exponentialBackOff * Constants.ONE_SEC < now &&
        lastReceiveTime.get() + exponentialBackOff * Constants.ONE_SEC < now &&
        lastAckTime + exponentialBackOff * Constants.ONE_SEC < now) {
      exponentialBackOff *= 2;
      return true;
    }
    return false;
  }

  @Override
  public void registerChannel() throws InterruptedException, IOException {
    Pair<Integer, IPacket> channelInitPacket =
        ByteBufferToPackets.wrapBytesToPackets(this, PacketType.REQUEST, null);
    write(channelInitPacket.getSecond(), channelInitPacket.getFirst());
  }

  public long getLastAckTime() {
    return lastAckTime;
  }
}
