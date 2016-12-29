package com.network.mocket.channel;

import com.network.mocket.Constants;
import com.network.mocket.packet.IPacket;

import java.net.SocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * common for client and server
 */
interface MocketChannel {
  Logger LOGGER = Logger.getLogger(MocketChannel.class.getSimpleName());

  void shutDown();
}
