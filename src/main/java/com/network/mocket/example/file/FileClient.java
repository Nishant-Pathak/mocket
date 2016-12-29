package com.network.mocket.example.file;

import com.network.mocket.MocketException;
import com.network.mocket.builder.client.Client;
import com.network.mocket.builder.client.ClientBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class FileClient {

  private static final AtomicInteger numBytesWritten = new AtomicInteger(0);
  private static final AtomicInteger numBytesGotBack = new AtomicInteger(0);


  public static void main(String... args) throws MocketException, IOException, InterruptedException {

    if (args.length != 1) {
      System.err.println("Please specify file name");
      System.exit(1);
    }

    ClientBuilder<byte []> clientBuilder = new ClientBuilder<byte[]>()
        .host("127.0.0.1", 8080)
        .channelType(ClientBuilder.ChannelType.UDP);
    Client<byte []> client = clientBuilder.build();
    final long startTime = System.nanoTime();
    sendFile(client, args[0]);


    OutputStream outputStream = Files.newOutputStream(Paths.get(args[0]), StandardOpenOption.TRUNCATE_EXISTING);
    WritableByteChannel writableByteChannel = Channels.newChannel(outputStream);
    while (true) {
      byte [] data = client.read();
      if (data != null) {
        writableByteChannel.write(ByteBuffer.wrap(data));
        if (numBytesWritten.get() == numBytesGotBack.addAndGet(data.length)) {
          System.out.println("Bytes got back: " + numBytesGotBack + " in: " + String.valueOf((System.nanoTime() - startTime)/1000000));
          break;
        }
      }
    }
    client.shutDown();
  }

  static final ExecutorService threadPoolExecutor = Executors.newSingleThreadExecutor();

  private static void sendFile(final Client<byte []> client, String fileName) throws IOException {
    InputStream fileInputStream = Files.newInputStream(Paths.get(fileName + "_echo"), StandardOpenOption.READ);

    final ReadableByteChannel readableByteChannel = Channels.newChannel(fileInputStream);
    final ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 1024);

    threadPoolExecutor.submit(new Runnable() {
      @Override public void run() {
        try {
          while (true) {
            int read = readableByteChannel.read(byteBuffer);
            if (read == -1) break;
            numBytesWritten.addAndGet(read);
            byteBuffer.flip();
            byte[] data = new byte[byteBuffer.remaining()];
            byteBuffer.get(data);
            client.write(data);
            byteBuffer.flip();
            byteBuffer.clear();
          }
          byteBuffer.clear();
          System.out.println("Bytes written: " + numBytesWritten);
        } catch (IOException | InterruptedException e) {
          e.printStackTrace();
        }
      }
    });
  }
}
