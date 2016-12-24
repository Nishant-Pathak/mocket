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
  static void logPacket(SocketAddress address, IPacket packet, boolean outgoing) {
    String stringBuilder = (outgoing ? Constants.OUT_GOING : Constants.IN_COMING) +
        address.toString() + " " +
        packet.getHeader() + '\n';
    LOGGER.log(Level.FINER, stringBuilder);
    LOGGER.log(Level.FINEST, Constants.NEW_LINE + packet.dumpPacket());
  }

  void shutDown();
}
