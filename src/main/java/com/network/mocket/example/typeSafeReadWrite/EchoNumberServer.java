package com.network.mocket.example.typeSafeReadWrite;

import com.network.mocket.MocketException;
import com.network.mocket.builder.server.Server;
import com.network.mocket.builder.server.ServerBuilder;
import com.network.mocket.helper.Pair;

import java.io.IOException;
import java.net.SocketAddress;

public class EchoNumberServer {
  public static void main(String... args) throws IOException, MocketException, InterruptedException {
    ServerBuilder<Integer> serverBuilder = new ServerBuilder<Integer>()
                                   .port(8080)
                                   .addHandler(new NumberHandler())
                                   .channelType(ServerBuilder.ChannelType.UDP);
//    serverBuilder.setLogLevel(Level.FINEST);
    Server <Integer> server = serverBuilder.build();

    while (true) {
      try {
        Pair<SocketAddress, Integer> read = server.read();
        if (read.getSecond() != null) {
          server.write(read.getSecond(), read.getFirst());
          System.out.println("got data from" + read.getFirst() + " : " + read.getSecond());

        }
      } catch (IOException | InterruptedException ex) {
        ex.printStackTrace();
        break;
      }
    }
  }
}
