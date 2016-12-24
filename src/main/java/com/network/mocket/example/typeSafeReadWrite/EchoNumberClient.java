package com.network.mocket.example.typeSafeReadWrite;

import com.network.mocket.MocketException;
import com.network.mocket.builder.client.Client;
import com.network.mocket.builder.client.ClientBuilder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class EchoNumberClient {

  public static void main(String... args) throws MocketException, IOException, InterruptedException {
    ClientBuilder<Integer> builder = new ClientBuilder<Integer>()
        .host("127.0.0.1", 8080)
        .addHandler(new NumberHandler())
        .channelType(ClientBuilder.ChannelType.UDP);
//    builder.setLogLevel(Level.FINEST);
    Client<Integer> client = builder.build();

    ReadableByteChannel readableByteChannel = Channels.newChannel(System.in);
    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

    new Thread(() -> {
      while (true) {
        try {
          readableByteChannel.read(byteBuffer);
          byteBuffer.flip();

          byte [] data = new byte[byteBuffer.remaining()];
          byteBuffer.get(data);
          client.write(Integer.parseInt(new String(data).trim()));
          byteBuffer.flip();
          byteBuffer.clear();
        } catch (Exception e) {
          e.printStackTrace();
          client.shutDown();
          break;
        }
      }
    }).start();

    while (true) {
      try {
        Integer data = client.read();
        if (data != null) {
          System.out.println("Reply from server: " + data);
        }
      } catch (IOException | InterruptedException ex) {
        ex.printStackTrace();
      }
    }
  }
}
