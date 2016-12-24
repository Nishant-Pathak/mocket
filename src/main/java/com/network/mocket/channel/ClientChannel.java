package com.network.mocket.channel;

import com.network.mocket.MocketException;
import com.network.mocket.helper.Pair;

import java.io.IOException;

public interface ClientChannel extends MocketChannel {
  void processWrite(byte [] data) throws IOException, InterruptedException;
  Pair<byte[], Integer> processRead() throws InterruptedException, IOException;
  void init(String host, int port) throws MocketException;

  void release(Integer till);
}
