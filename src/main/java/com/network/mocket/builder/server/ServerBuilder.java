package com.network.mocket.builder.server;

import com.network.mocket.MocketException;
import com.network.mocket.allocator.buffer.ByteBufferAllocator;
import com.network.mocket.allocator.buffer.ByteBufferPool;
import com.network.mocket.allocator.packet.PacketAllocator;
import com.network.mocket.allocator.packet.PacketPool;
import com.network.mocket.builder.Builder;
import com.network.mocket.channel.ServerChannel;
import com.network.mocket.channel.udp.UdpServerChannel;
import com.network.mocket.handler.MocketStreamHandler;
import com.network.mocket.handler.StreamProcessor;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.*;

public class ServerBuilder<T> implements Builder {
  private int port = 8080;

  private ChannelType channelType = ChannelType.UDP;

  private List<MocketStreamHandler> handlers;

  public ServerBuilder() {
    setLogLevel(Level.OFF);
    handlers = new LinkedList<>();
  }

  /**
   * Builds a server which is capable of reading and writing objects
   * of T to the clients
   * @param <T> type of the object to be read and write
   * @return implementation of @{@link Server}
   * @throws MocketException if something wents wrong
   */
  public <T> Server<T> build() throws MocketException {
    ServerChannel serverChannel;
    ByteBufferPool byteBufferPool = ByteBufferPool.newInstance(SERVER_BYTE_BUFFER_SIZE, SERVER_BYTE_BUFFER_POOL_COUNT);
    PacketPool packetPool = PacketPool.newInstance(byteBufferPool.newAllocator());
    ExecutorService readExecutorService = Executors.newSingleThreadExecutor();
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    PacketAllocator packetAllocator = packetPool.newAllocator();
    ByteBufferAllocator byteBufferAllocator = byteBufferPool.newAllocator();
    StreamProcessor streamProcessor = new StreamProcessor(handlers);

    switch (channelType) {
      case UDP:
        serverChannel = new UdpServerChannel(
            byteBufferAllocator,
            packetAllocator,
            readExecutorService,
            executorService
        );
        break;
      case TCP:
        throw new MocketException("coming soon");
      default:
        throw new MocketException("serverChannel is not defined");
    }
    Server<T> server = new ServerImpl<>(serverChannel, streamProcessor);
    server.init(port);
    return server;
  }

  public ServerBuilder<T> channelType(ChannelType serverChannel) {
    this.channelType = serverChannel;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ServerBuilder<T> addHandler(MocketStreamHandler handler) {
    Objects.nonNull(handler);
    handlers.add(handler);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ServerBuilder<T> addLogHandler(Handler handler) {
    Logger log = LogManager.getLogManager().getLogger("");
    handler.setFormatter(new SimpleFormatter());
    log.addHandler(handler);
    return this;
  }

  public ServerBuilder<T> port(int port) {
    this.port = port;
    return this;
  }

  public enum ChannelType {
    UDP,
    TCP
  }
}
