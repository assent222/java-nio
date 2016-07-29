package com.luxoft;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by yuliya on 29.07.16.
 */
public class BadBoy {

    public static void main(String[] args) throws Exception {


        Socket[] sockets = new Socket[2800];

        for (int i = 0; i < sockets.length; i++) {

            sockets[i] = new Socket("localhost", 8080);

            System.out.println(i);
        }

        Thread.sleep(1_000_000_000);
    }
}
