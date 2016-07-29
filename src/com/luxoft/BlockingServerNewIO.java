package com.luxoft;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by yuliya on 29.07.16.
 */
public class BlockingServerNewIO {

    public static void main(String[] args) throws IOException {


        ServerSocketChannel server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(8080));

        while (true) {

            SocketChannel s = server.accept(); // blocking call

            handle(s);
        }

    }

    private static void handle(SocketChannel s) throws IOException {

        ByteBuffer buffer = ByteBuffer.allocateDirect(80);

        int data;

        while ((data = s.read(buffer)) != -1) {

            buffer.flip();
            transform(buffer);

            while (buffer.hasRemaining()) {
                s.write(buffer);

            }
            buffer.compact();
        }
    }

    private static void transform(ByteBuffer data) {

        for (int i = 0; i < data.limit(); i++) {

            data.put(i, (byte) transform(data.get(i)));
        }

    }

    private static void handle(Socket s) {

        try (InputStream in = s.getInputStream(); OutputStream out = s.getOutputStream()) {


            int data;
            while ((data = in.read()) != -1) {

                out.write(transform(data));
            }

        } catch (IOException e) {

            throw new UncheckedIOException(e);
        }
    }

    private static int transform(int data) {

        return Character.isLetter(data) ? data ^ 32 : data;
    }
}
