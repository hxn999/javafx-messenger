package com.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Receiver extends Thread {

    private Socket socket;
    private String type;
    private BufferedReader receive;
    private String data;
    public static Receiver receiver;

    public Receiver(String name, Socket socket) {
        super(name);
        this.socket = socket;
        try {
            this.receive = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        } catch (IOException e) {
            e.printStackTrace();
        }

        this.receiver = this;
    }

    private  void receive() {
        synchronized (this) {

            while (true) {
//                try {
                    System.out.println(
                            "waiting for data to send"
                    );
                    System.out.println(Thread.currentThread().getState());
                try {
                    data = receive.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (data != null) {

                        System.out.println(data);
                    }
                    System.out.println("not waiting for data to send");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

            }
        }
    }
//
//    public synchronized void sendMessage(String receiver, String message) {
//        String type="MSG";
//        this.request = "MSG\n" +
//                receiver+"\n" +
//                message+"\n";
//        notify();
//
//    }
//    public synchronized void login(String phone, String password) {
//        String type="LOGIN";
//        request = "LOGIN\n" +
//                phone+"\n" +
//                password+"\n";
//        notify();
//
//    }

    @Override
    public void run() {
        receive();
    }
}
