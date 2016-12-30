package com.network.mocket.builder.client;

import com.network.mocket.MocketException;
import com.network.mocket.allocator.buffer.ByteBufferAllocator;
import com.network.mocket.allocator.buffer.ByteBufferPool;
import com.network.mocket.allocator.packet.PacketAllocator;
import com.network.mocket.allocator.packet.PacketPool;
import com.network.mocket.builder.Builder;
import com.network.mocket.builder.server.ServerBuilder;
import com.network.mocket.channel.ClientChannel;
import com.network.mocket.channel.udp.UdpChannel;
import com.network.mocket.handler.MocketStreamHandler;
import com.network.mocket.handler.StreamProcessor;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.*;

/**
 * builds a client object as per configure
 */
public class ClientBuilder<T> implements Builder {
  private static final Logger LOGGER = Logger.getLogger(ClientBuilder.class.getName());

  private ChannelType channelType = ChannelType.UDP;

  private String host;

  private int port;

  private List<MocketStreamHandler> handlers;

  private boolean ensureDelivery = false;

  public ClientBuilder() {
    setLogLevel(Level.OFF);
    this.handlers = new LinkedList<>();
  }

  /**
   * build client with given configuration
   * @return new concrete {@link Client}
   * @throws MocketException if it fails to create Client
   */
  public <T> Client<T> build() throws MocketException {
    ClientChannel clientChannel;
    UUID uuid = UUID.randomUUID();

    LOGGER.log(Level.FINEST, "Using channel id: {0}", uuid.toString());

    ByteBufferPool byteBufferPool = ByteBufferPool.newInstance(BYTE_BUFFER_SIZE, BYTE_BUFFER_POOL_COUNT);
    ByteBufferAllocator byteBufferAllocator = byteBufferPool.newAllocator();

    PacketPool packetPool = PacketPool.newInstance(byteBufferAllocator);
    PacketAllocator packetAllocator = packetPool.newAllocator();

    ExecutorService executorService = Executors.newSingleThreadExecutor();
    ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    StreamProcessor streamProcessor = new StreamProcessor(handlers);

    switch (channelType) {
      case TCP:
        throw new MocketException("coming soon");
      case TWO_TCP:
        throw new MocketException("coming soon");
      case UDP:
        clientChannel = new UdpChannel(
            uuid,
            byteBufferAllocator,
            packetAllocator,
            executorService,
            scheduledExecutorService,
            ensureDelivery
        );
        break;
      default:
        throw new MocketException("channel type is not defined");
    }
    Client<T> client = new ClientImpl<>(clientChannel, streamProcessor);
    client.init(host, port);
    return client;
  }

  public ClientBuilder<T> channelType(ChannelType type) {
    this.channelType = type;
    return this;
  }

  public ClientBuilder<T> host(String host, int port) {
    this.host = host;
    this.port = port;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ClientBuilder<T> addHandler(MocketStreamHandler handler) {
    Objects.nonNull(handler);
    handlers.add(handler);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ClientBuilder<T> addLogHandler(Handler handler) {
    Logger log = LogManager.getLogManager().getLogger("");
    handler.setFormatter(new SimpleFormatter());
    log.addHandler(handler);
    return this;
  }

  public enum ChannelType {
    UDP,
    TCP,
    TWO_TCP
  }


  public ClientBuilder<T> ensureDelivery(boolean ensure) {
    ensureDelivery = ensure;
    return this;
  }
}
