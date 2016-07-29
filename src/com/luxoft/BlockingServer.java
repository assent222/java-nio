package com.luxoft;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by yuliya on 29.07.16.
 */
public class BlockingServer {

    public static void main(String[] args) throws IOException {


        ServerSocket server = new ServerSocket(8080);

        while (true) {

            Socket s = server.accept(); // blocking call

            handle(s);
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
