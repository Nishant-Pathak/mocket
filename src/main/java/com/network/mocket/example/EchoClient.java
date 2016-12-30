package com.network.mocket.example;

import com.network.mocket.MocketException;
import com.network.mocket.builder.client.Client;
import com.network.mocket.builder.client.ClientBuilder;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class EchoClient {

  public static void main(String... args) throws MocketException, IOException, InterruptedException {
    final Client<byte []> client =
        new ClientBuilder<>().ensureDelivery(false).host("127.0.0.1", 8080).build();

    final ReadableByteChannel readableByteChannel = Channels.newChannel(System.in);
    final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

    new Thread(new Runnable() {
      @Override public void run() {

        while (true) {
          try {
            readableByteChannel.read(byteBuffer);
            byteBuffer.flip();
            byte[] data = new byte[byteBuffer.remaining()];
            byteBuffer.get(data);
            client.write(data);
            byteBuffer.flip();
            byteBuffer.clear();
          } catch (Exception e) {
            e.printStackTrace();
            client.shutDown();
            break;
          }
        }
      }
    }).start();

    while (true) {
      try {
        byte[] data = client.read();
        if (data != null) {
          System.out.println("Reply from server: " + new String(data));
        }
      } catch (IOException | InterruptedException ex) {
        ex.printStackTrace();
        break;
      }
    }
  }
}
