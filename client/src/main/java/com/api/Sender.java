package com.api;

import java.io.*;
import java.net.Socket;

import com.db.Message;
import com.db.SignedUser;
import com.db.User;
import com.server.Response;

public class Sender extends Thread {

    private Socket socket;
    private String type;
    public static ObjectInputStream receive;
    private ObjectOutputStream send;
    public static Sender sender;
    private Response request;

    public Sender(String name, Socket socket) {
        super(name);
        this.socket = socket;
        try {
            this.receive = new ObjectInputStream(socket.getInputStream());
            this.send = new ObjectOutputStream(socket.getOutputStream());
            System.out.println(send);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.sender = this;
    }

    private void request() {
        synchronized (this) {
            while (true) {
                try {
                    System.out.println("waiting");
                    wait();

                    System.out.println("sending request");


                    // Create a Response object with the request as the body

                    send.writeObject(request);
                    send.flush();

                    System.out.println("sending request finished");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public synchronized void sendMessage(Message msg) {
        request = new Response(SignedUser.phone,msg,Path.MSG);

        notify();
    }

    public synchronized void login(String phone, String password) {
        Data body = new Data();
        body.phone = phone;
        body.password = password;
        request = new Response(SignedUser.phone,body,Path.LOGIN);
        notify();
    }

    public synchronized void searchUser(String name) {
        Data body = new Data();
        body.recieverName = name;
        request = new Response(SignedUser.phone,body,Path.SEARCH);
        notifyAll();
    }

    public synchronized void block(String phone) {
        Data body = new Data();
        body.receiverPhone = phone;
        request = new Response(SignedUser.phone,body,Path.BLOCK);
        notifyAll();
    }

    public synchronized void unblock(String phone) {
        Data body = new Data();
        body.receiverPhone = phone;
        request = new Response(SignedUser.phone,body,Path.UNBLOCK);
        notifyAll();
    }




    public synchronized void createAccount(String name, String phone, String password,String url) {
        User newUser = new User(name,url,phone,password);

        request = new Response(newUser.getPhone(),newUser,Path.CREATE_ACCOUNT);
        notify();

    }


    public synchronized void fetchChat(int chatId) {
        Data body = new Data();
        body.chatId = chatId;
        request = new Response(SignedUser.phone,body,Path.CHAT);
        notify();
    }



    @Override
    public void run() {
        request();
    }


}
