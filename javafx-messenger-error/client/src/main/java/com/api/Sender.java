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

    private ObjectOutputStream send;
    public static Sender sender;
    private Response request;

    public Sender(String name, Socket socket) {
        super(name);
        this.socket = socket;
        try {

            this.send = new ObjectOutputStream(socket.getOutputStream());
            System.out.println(send);
            this.send.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Sender.sender = this;
    }

    public synchronized void sendRequest(Response req) {
        try {
            this.send.writeObject(req);
            this.send.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public synchronized void sendMessage(Message msg, String requestId) {
        request = new Response(SignedUser.phone,msg,Path.MSG,requestId);

        notifyAll();
    }

    public synchronized void login(String phone, String password, String requestId) {
        System.out.println(requestId);
        Data body = new Data();
        body.phone = phone;
        body.password = password;
        request = new Response(SignedUser.phone,body,Path.LOGIN,requestId);
        notifyAll();
    }

    public synchronized void searchUser(String name, String requestId) {
        Data body = new Data();
        body.recieverName = name;
        request = new Response(SignedUser.phone,body,Path.SEARCH,requestId);
        notifyAll();
    }

    public synchronized void block(String phone, String requestId) {
        Data body = new Data();
        body.receiverPhone = phone;
        request = new Response(SignedUser.phone,body,Path.BLOCK,requestId);
        notifyAll();
    }

    public synchronized void unblock(String phone, String requestId) {
        Data body = new Data();
        body.receiverPhone = phone;
        request = new Response(SignedUser.phone,body,Path.UNBLOCK,requestId);
        notifyAll();
    }




    public synchronized void createAccount(String name, String phone, String password, String url, String requestId) {
        User newUser = new User(name,url,phone,password);

        request = new Response(newUser.getPhone(),newUser,Path.CREATE_ACCOUNT,requestId);
        notifyAll();

    }


    public synchronized void fetchChat(int chatId, String requestId) {
        Data body = new Data();
        body.chatId = chatId;
        request = new Response(SignedUser.phone,body,Path.CHAT,requestId);
        notifyAll();
    }

    public synchronized void searchSingle(String phone, String requestId) {

        Data body = new Data();
        body.receiverPhone = phone;
        request = new Response(SignedUser.phone,body,Path.SINGLE_SEARCH,requestId);
        notifyAll();
    }



    @Override
    public void run() {
        request();
    }


}
