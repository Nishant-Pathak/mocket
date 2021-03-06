package com.network.mocket.channel;

import com.network.mocket.Constants;
import com.network.mocket.allocator.packet.PacketAllocator;
import com.network.mocket.channel.manager.IChannelManager;
import com.network.mocket.helper.Pair;
import com.network.mocket.helper.Utils;
import com.network.mocket.packet.IPacket;

import java.io.IOException;
import java.net.SocketAddress;

public abstract class ChannelCommons {

  // method to be implemented by UdpChannel and UdpServer Channel
  public abstract void write(SocketAddress address, byte[] data) throws IOException;

  public void write(
      IChannelManager channelManager,
      SocketAddress address,
      IPacket packet,
      Integer packetSeq
  ) throws IOException {
    // Log Packet
    MocketChannel.logPacket(address, packet, true);

    // add to in-flight packets
    if (Constants.IGNORE_SEQUENCE != packetSeq) {
      channelManager.putInFlightPacket(packetSeq, packet);
    }

    // update send time
    if (packet.getHeader().getPacketType().isTimeAccounted()) {
      channelManager.setLastSendTime(Utils.now());
    }

    // Write to wire
    write(address, packet.read());
  }

  void releasePacket(
      IChannelManager channelManager,
      PacketAllocator allocator,
      Integer start,
      Integer end
  ) {
    for (int i = start; i <= end; i++) {
      IPacket packet = channelManager.getIncomingPackets(i);
      if(packet == null) continue;
      channelManager.removeFromIncomingPackets(i);
      allocator.release(packet);
      channelManager.getIncomingPacketConsumedTill().incrementAndGet();
    }
  }

  Pair<byte[], Integer> read(IChannelManager channelManager) {
    byte[] result = null;
    int currentConsumedPacket = channelManager.getIncomingPacketConsumedTill().get();
    while (true) {
      IPacket packet = channelManager.getIncomingPackets(currentConsumedPacket + 1);
      if (packet == null) break;

      // TODO optimize memory allocation
      result = Utils.concat(result, packet.readPayload());
      currentConsumedPacket++;
    }
    return Pair.create(result, currentConsumedPacket);
  }
}
