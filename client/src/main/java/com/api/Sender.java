package com.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Sender extends Thread {

    private Socket socket;
    private String type;
    private PrintWriter send;
    private String request;
    public static Sender sender;

    public Sender(String name, Socket socket) {
        super(name);
        this.socket = socket;
        try {
            this.send = new PrintWriter(socket.getOutputStream(), true);
            System.out.println(send);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.sender = this;
    }

    private synchronized void request() {
        while (true) {
            try {
                System.out.println("waiting");
                wait();
                System.out.println("waiting finished");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("sending request");
            System.out.println(request);
            send.println(request);
            System.out.println("sending request finished");
        }
    }

    public synchronized void sendMessage(String receiver, String message) {
        String type="MSG";
      this.request = "MSG\n" +
                receiver+"\n" +
                message+"\n";
        notify();

    }
    public synchronized void login(String phone, String password) {
        String type="LOGIN";
        request = "LOGIN\n" +
                phone+"\n" +
                password+"\n";
        notify();

    }

    @Override
    public void run() {
        request();
    }
}
