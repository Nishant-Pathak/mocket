package com.network.mocket.packet;

import com.network.mocket.channel.manager.IChannelManager;

import java.util.concurrent.ExecutorService;

public interface IPacket extends Comparable {
  int getSize();
  IPacket put(byte [] data);
  byte[] readPayload();
  byte[] read();
  long getCreatedTime();
  String dumpPacket();
  Packet.Header getHeader();

  /**
   * Effect of receiving a packet
   * @param channelManager
   */
  void process(IChannelManager channelManager);

  /**
   * Async side effect of receiving a packet
   * @param channelManager
   * @param executorService
   */
  default void postProcess(IChannelManager channelManager, ExecutorService executorService){
    throw new RuntimeException("Packet.postProcess not implemented");
  }
}
