package com.api;

import com.db.Chat;
import com.db.ClientChat;
import com.db.SignedUser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class MessageReceiver extends Thread {


    public static BufferedReader receive;
    private Socket socket;

    public MessageReceiver(String name, Socket socket) {
        super(name);
        this.socket = socket;

        try {
            this.receive = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void receive() {
        String data;
        boolean isChatId = true;
        int chatId = 0;
        String msg = null;
        while (true) {
            try {
                data = receive.readLine();

                if (isChatId) {
                    chatId = Integer.parseInt(data);
                    isChatId = false;
                } else {
                    msg = data;
                    isChatId = true;
                }

                // checking chat exits or not
                if (SignedUser.chatList!=null&&SignedUser.chatList.contains(chatId)) {

                    new ClientChat(chatId).add(msg);
                }else{

                    // TODO create new chatfile and add
                    ClientChat.CreateChat(msg,receive.readLine(),chatId);
                    System.out.println(data);
                    System.out.println("creating chat");
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    public void run() {

        receive();


    }
}
