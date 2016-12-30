package com.network.mocket.example;

import com.network.mocket.MocketException;
import com.network.mocket.builder.server.Server;
import com.network.mocket.builder.server.ServerBuilder;
import com.network.mocket.helper.Pair;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

public class EchoServer {

  private static final Map<SocketAddress, Pair<Long, Integer> /* pair of time vs bytes */> clientStat = new HashMap<>();
  public static void main(String... args) throws IOException, MocketException, InterruptedException {
    Server<byte[]> server = new ServerBuilder<>().port(8080).build();

    while (true) {
      Pair<SocketAddress, byte[]> read = server.read();
      if (read.getSecond() != null) {
        if (clientStat.containsKey(read.getFirst())) {
          Pair<Long, Integer> stats = clientStat.get(read.getFirst());
          Pair<Long, Integer> newStats = Pair.create(stats.getFirst(), stats.getSecond() + read.getSecond().length);
          clientStat.put(read.getFirst(), newStats);
          System.out.println("Stats of" + read.getFirst() + " as: " + newStats.getSecond()
              + " bytes, in: " + (System.currentTimeMillis() - newStats.getFirst()) + " ms");
        } else {
          Pair<Long, Integer> stat = Pair.create(System.currentTimeMillis(), read.getSecond().length);
          clientStat.put(read.getFirst(), stat);
        }
        server.write(read.getSecond(), read.getFirst());
      }
    }
  }
}
