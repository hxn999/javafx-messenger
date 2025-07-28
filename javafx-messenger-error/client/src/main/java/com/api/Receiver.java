package com.api;

import com.client.chat.ChatController;
import com.client.util.ReceiverPhone;
import com.db.Chat;
import com.db.Message;
import com.db.SignedUser;
import com.server.Response;
import javafx.application.Platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.List;

public class Receiver extends Thread {

    private final Socket socket;
    private final ChatController chatController;
    private ObjectInputStream input;

    public Receiver(String name, Socket socket, ChatController chatController) {
        super(name);
        this.socket = socket;
        this.chatController = chatController;

        try {
            this.input = new ObjectInputStream(this.socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize ObjectInputStream", e);
        }
    }

    @Override
    public void run() {
        try {
            listenLoop();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void listenLoop() throws IOException, ClassNotFoundException {
        while (true) {
            System.out.println("Waiting to receive...");
            Object o = input.readObject();

            if (o instanceof Response) {
                Response resp = (Response) o;
                System.out.println("Received Response: requestId=" + resp.getRequestId());

                if (!resp.isIsMessage()) {
                    // Handle RPC-style response
                    ResponseManager.complete(resp.getRequestId(), resp);
                } else {
                    // Handle real-time message event
                    handleRealtimeMessage(resp);
                }
            } else {
                System.err.println("Unexpected object type: " + o.getClass().getName());
            }
        }
    }

    private void handleRealtimeMessage(Response resp) {
        Platform.runLater(() -> {
            if (chatController == null) {
                System.err.println("ChatController not initialized for real-time messages.");
                return;
            }

            Chat chat = resp.getChat();
            List<Message> messages = chat.getMessages();

            // 1) Add each new Message to the existing chat
            for (Message msg : messages) {
                if (ChatController.allChats.containsKey(msg.getChatId())) {
                    ChatController.allChats.get(msg.getChatId()).addMessage(msg);
                }
                if (ChatController.currentChatId == msg.getChatId()) {
                    chatController.addMessageBubble(msg.getMessage(), false);
                }
            }

            // 2) Update or insert the Chat object itself
            ChatController.allChats.put(chat.getChatId(), chat);
            ChatController.receiverMap.put(ReceiverPhone.get(chat), chat.getChatId());
            SignedUser.chatList.add(chat.getChatId());

            // 3) Refresh the UI
            chatController.populateChatList();
        });
    }
}
