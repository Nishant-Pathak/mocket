package com.network.mocket.builder.client;

import com.network.mocket.MocketException;

import java.io.IOException;

public interface Client<T> {

  /**
   * read any incoming data
   * @return T generic type object which was mentioned while creating the client
   * @throws InterruptedException
   * @throws IOException
   */
  T read() throws InterruptedException, IOException;

  /**
   * write object of type T on the wire
   * it used @{@link com.network.mocket.handler.MocketStreamHandler} to parse the object
   * @param data object to send on wire to the server.
   * @throws IOException
   * @throws InterruptedException
   */
  void write(T data) throws IOException, InterruptedException;

  /**
   * Initialize the client
   * @param host host name of the server
   * @param port post exposed by the server
   * @throws MocketException if not able to connect or something goes wrong
   */
  void init(String host, int port) throws MocketException;

  /**
   * disconnect from the server and release buffers
   */
  void shutDown();
}
