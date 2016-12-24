package com.network.mocket.builder.server;

import com.network.mocket.MocketException;
import com.network.mocket.channel.ServerChannel;
import com.network.mocket.handler.MocketStreamHandler;
import com.network.mocket.handler.StreamProcessor;
import com.network.mocket.helper.Pair;

import java.io.IOException;
import java.net.SocketAddress;
import java.text.ParseException;
import java.util.List;

class ServerImpl<T> implements Server<T> {
  private ServerChannel serverChannel;
  private StreamProcessor streamProcessor;

  ServerImpl(ServerChannel serverChannel, StreamProcessor streamProcessor) {
    this.serverChannel = serverChannel;
    this.streamProcessor = streamProcessor;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Pair<SocketAddress, T> read() throws InterruptedException {
    Pair<SocketAddress, Pair<byte[], Integer>> incoming = serverChannel.processRead();
    Object data = incoming.getSecond().getFirst();

    try {
      data = streamProcessor.decode(data);
      serverChannel.release(incoming.getFirst(), incoming.getSecond().getSecond());
    } catch (ParseException e) {
      // expect some more data, try next time.
    }
    return Pair.create(incoming.getFirst(), (T) data);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void write(T data, SocketAddress address) throws IOException, InterruptedException {
    try {
      Object output = streamProcessor.encode(data);
      serverChannel.processWrite((byte[])output, address);
    } catch (ParseException e) {
      // expect some more data, try next time.
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void init(int port) throws MocketException {
    serverChannel.init(port);
  }
}
