package com.api;

//import com.db.ClientChat;

import com.client.chat.ChatController;
import com.client.util.ReceiverPhone;
import com.db.Chat;
import com.db.Message;
import com.db.SignedUser;
import com.server.Response;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;

public class Receiver extends Thread {


    public static ObjectInputStream receive;
    private Socket socket;
    private ChatController chatController;

    public Receiver(String name, Socket socket, ChatController chatController) {
        super(name);
        this.socket = socket;
        this.chatController = chatController;

        try {
            receive = new ObjectInputStream(this.socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private synchronized void receive() {

        while (true) {
            Object obj = null;
            System.out.println("waiting to receive");
            try {
                obj = receive.readObject();
            System.out.println(" receive ses");
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            if (obj instanceof Response response) {
                // Check if it's a response to a previous request
                if (response.getRequestId() != null) {
                    System.out.println("response: " + response.getRequestId());
                    ResponseManager.complete(response.getRequestId(), response);
                } else {
                    // It’s a real-time event (e.g., chat message)

                    handleRealtimeMessage(obj);
                }
            }
        }

    }


    private synchronized void handleRealtimeMessage(Object o) {
        Platform.runLater(() -> {
            if (chatController == null) {
                System.err.println("ChatController is not initialized for real-time message handling.");
                return;
            }

            if (o instanceof Message) {
                Message msg = (Message) o;
                ChatController.allChats.get(msg.getChatId()).addMessage(msg);
                chatController.populateChatList();
                if (ChatController.currentChatId == msg.getChatId()) {
                    chatController.addMessageBubble(msg.getMessage(), false);
                }
            }

            if (o instanceof Chat) {
                Chat chat = (Chat) o;
                ChatController.allChats.put(chat.getChatId(), chat);
                ChatController.receiverMap.put(ReceiverPhone.get(chat), chat.getChatId());
                SignedUser.chatList.add(chat.getChatId());
                chatController.populateChatList();
                if (ChatController.currentChatId == chat.getChatId()) {
                    chatController.addMessageBubble(chat.getMessages().get(0).getMessage(), false);
                }
            }
        });
    }


    @Override
    public void run() {

        receive();


    }
}
