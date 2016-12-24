package com.network.mocket.builder.client;

import com.network.mocket.MocketException;
import com.network.mocket.channel.ClientChannel;
import com.network.mocket.handler.StreamProcessor;
import com.network.mocket.helper.Pair;

import java.io.IOException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

class ClientImpl<T> implements Client<T> {
  private ClientChannel clientChannel;
  private StreamProcessor streamProcessor;
  private static final Logger LOGGER = Logger.getLogger(ClientImpl.class.getSimpleName());

  ClientImpl(ClientChannel clientChannel, StreamProcessor streamProcessor) {
    this.clientChannel = clientChannel;
    this.streamProcessor = streamProcessor;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public T read() throws InterruptedException, IOException {
    Pair<byte[], Integer> incoming = clientChannel.processRead();
    Object data = incoming.getFirst();

    try {
      data = streamProcessor.decode(incoming.getFirst());
      LOGGER.log(Level.FINEST, "releasing packet {0}", incoming.getSecond());
      clientChannel.release(incoming.getSecond());

    } catch (ParseException e) {
      // expect some more data, try next time.
    }
    return (T) data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void write(T data) throws IOException, InterruptedException {
    try {
      Object output = streamProcessor.encode(data);
      clientChannel.processWrite((byte[]) output);

    } catch (ParseException e) {
      // expect some more data, try next time.
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void init(String host, int port) throws MocketException {
    clientChannel.init(host, port);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void shutDown() {
    clientChannel.shutDown();
  }
}
