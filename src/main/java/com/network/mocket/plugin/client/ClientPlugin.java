package com.network.mocket.plugin.client;

import com.network.mocket.plugin.Plugin;

import java.nio.channels.SocketChannel;

public abstract class ClientPlugin implements Plugin {
  protected final SocketChannel socketChannel;

  public ClientPlugin(SocketChannel socketChannel) {
    this.socketChannel = socketChannel;
  }

  public abstract byte[] read();
  public abstract void write(byte [] data);
}
