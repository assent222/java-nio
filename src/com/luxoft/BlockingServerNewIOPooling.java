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
import java.rmi.UnexpectedException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Created by yuliya on 29.07.16.
 */
public class BlockingServerNewIOPooling {

    public static void main(String[] args) throws IOException {


        ServerSocketChannel server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(8080));

        server.configureBlocking(false);

        Map<SocketChannel, ByteBuffer> sockets = new ConcurrentHashMap<>();

        while (true) {

            SocketChannel s = server.accept(); // non blocking, almost always null

            if (s != null) {

                System.out.println(s);
                s.configureBlocking(false);

                sockets.put(s, ByteBuffer.allocateDirect(80));
            }

            sockets.keySet().removeIf(socketChannel -> !socketChannel.isOpen());

            sockets.forEach(BlockingServerNewIOPooling::handle);
        }

    }

    private static void handle(SocketChannel s, ByteBuffer buffer) {

        try {

            int data;

            if ((data = s.read(buffer)) == -1) {

                close(s);
            }

            buffer.flip();
            transform(buffer);

            while (buffer.hasRemaining()) {
                s.write(buffer);

            }
            buffer.compact();

        } catch (IOException e) {

            close(s);
            throw new UncheckedIOException(e);
        }

    }

    private static void close(SocketChannel s) {
        try {
            s.close();

        } catch (IOException ignore) {};
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
