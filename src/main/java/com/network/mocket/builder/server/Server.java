package com.network.mocket.builder.server;

import com.network.mocket.MocketException;
import com.network.mocket.helper.Pair;

import java.io.IOException;
import java.net.SocketAddress;

public interface Server <T> {

  /**
   * Read object of type T from the Client connected at @{@link SocketAddress}
   * @return @{@link Pair} of @{@link SocketAddress} and data object
   * @throws InterruptedException if read is interrupted
   */
  Pair<SocketAddress, T> read() throws InterruptedException;

  /**
   * writed data to the given address
   * @param data to send to addredd
   * @param address of the client connected
   * @throws IOException if error occurs while writing to the socket
   * @throws InterruptedException if write is interrupted
   */
  void write(T data, SocketAddress address) throws IOException, InterruptedException;

  /**
   * Initialize the server Mocket
   * @param port of the localhost to be bind to.
   * @throws MocketException if initialization fails.
   */
  void init(int port) throws MocketException;
}
