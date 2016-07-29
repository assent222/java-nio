package com.luxoft;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yuliya on 29.07.16.
 */
public class BlockingServerSelectorMTh {

    private static final Map<SocketChannel, ByteBuffer> sockets = new ConcurrentHashMap<>();

    public static final ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws IOException {


        ServerSocketChannel server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(8080));

        server.configureBlocking(false);

        Selector selector = Selector.open();
        server.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {

            selector.select(); // blocking, not null

            for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext();) {


                SelectionKey key = it.next();
                it.remove();

                try {
                    if (key.isValid()) {

                        if (key.isAcceptable()) {
                            accept(key);
                        } else if (key.isReadable()) {
                            read(key);
                        } else if (key.isWritable()) {
                            write(key);
                        }
                    }
                } catch (Exception e) {

                    System.err.println(e);
                }

                sockets.keySet().removeIf(socketChannel -> !socketChannel.isOpen());
            }
        }
    }

    private static void write(SelectionKey key) throws IOException {

        SocketChannel channel = (SocketChannel) key.channel();

        ByteBuffer buffer = sockets.get(channel);

        channel.write(buffer);

        if (!buffer.hasRemaining()) {

            buffer.compact();
            key.interestOps(SelectionKey.OP_READ);
        }

    }

    private static void read(SelectionKey key) throws IOException {

        SocketChannel channel = (SocketChannel) key.channel();

        ByteBuffer buffer = sockets.get(channel);

        int data = channel.read(buffer);

        if (data == -1) {

            channel.close();
            sockets.remove(channel);
        }

        executor.submit(() -> {

            buffer.flip();
            transform(buffer);

            key.interestOps(SelectionKey.OP_WRITE);
            key.selector().wakeup();
        });

    }

    private static void accept(SelectionKey key) throws IOException {

        ServerSocketChannel server = (ServerSocketChannel) key.channel();

        SocketChannel channel = server.accept();

        System.out.println(channel);
        channel.configureBlocking(false);

        channel.register(key.selector(), SelectionKey.OP_READ);

        sockets.put(channel, ByteBuffer.allocateDirect(80));
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
