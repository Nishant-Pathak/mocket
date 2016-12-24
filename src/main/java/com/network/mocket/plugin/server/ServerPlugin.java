package com.network.mocket.plugin.server;

import com.network.mocket.plugin.Plugin;

import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.Map;

public abstract class ServerPlugin implements Plugin {
  protected ServerSocketChannel channel;

  public ServerPlugin(ServerSocketChannel channel) {
    this.channel = channel;
  }

  abstract void write(SocketAddress address, byte[] data);
  abstract Map<SocketAddress, byte[]> read();
}
