package com.network.mocket.builder;

import com.network.mocket.handler.MocketStreamHandler;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * common interface used by @{@link com.network.mocket.builder.server.ServerBuilder}
 * and @{@link com.network.mocket.builder.client.ClientBuilder}
 */
public interface Builder {
  // This can be increased by increasing java heap memory accordingly.
  int BYTE_BUFFER_POOL_COUNT = 1024; // pool size = count * size 1MB
  int BYTE_BUFFER_SIZE = 1024 * 10; // 10 KB

  int SERVER_BYTE_BUFFER_POOL_COUNT = 1024 * 10; // pool size = count * size = 10MB
  int SERVER_BYTE_BUFFER_SIZE = 1024 * 10; // 10 KB

  /**
   * Add handler for converting objects to bytes and bytes to objects
   *
   * @param handler implementation of @{@link MocketStreamHandler}
   * @return @{@link Builder}
   */
  Builder addHandler(MocketStreamHandler handler);

  Builder addLogHandler(Handler handler);
}
