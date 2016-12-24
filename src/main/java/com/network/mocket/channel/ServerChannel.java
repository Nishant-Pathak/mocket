package com.network.mocket.channel;

import com.network.mocket.MocketException;
import com.network.mocket.helper.Pair;

import java.io.IOException;
import java.net.SocketAddress;

public interface ServerChannel extends MocketChannel {
  void processWrite(byte [] data, SocketAddress address) throws IOException, InterruptedException;
  Pair<SocketAddress, Pair<byte[], Integer>> processRead() throws InterruptedException;
  void release(SocketAddress address, Integer seq);
  void init(int port) throws MocketException;
}
