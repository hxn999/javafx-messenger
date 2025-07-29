package com.api;

import com.client.chat.ChatController;
import com.client.util.ReceiverPhone;
import com.db.Chat;
import com.db.Message;
import com.db.SignedUser;
import com.db.User;
import com.server.Response;
import javafx.application.Platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Receiver extends Thread {

    private static ChatController controller;
    private final Socket socket;
    private final ChatController chatController;
    private ObjectInputStream input;
    private volatile boolean running = true;

    // Queue for messages received before ChatController is ready
    private static final ConcurrentLinkedQueue<PendingRealTimeMessage> pendingMessages = new ConcurrentLinkedQueue<>();

    public Receiver(String name, Socket socket, ChatController chatController) {
        super(name);
        this.socket = socket;
        this.chatController = chatController;

        try {
            this.input = new ObjectInputStream(this.socket.getInputStream());
            System.out.println("✅ Receiver initialized successfully for: " + name);
        } catch (IOException e) {
            System.err.println("❌ Failed to initialize ObjectInputStream: " + e.getMessage());
            throw new RuntimeException("Failed to initialize ObjectInputStream", e);
        }
    }

    @Override
    public void run() {
        try {
            listenLoop();
        } catch (IOException e) {
            if (running) {
                System.err.println("❌ Receiver connection error: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Receiver class not found error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    private void listenLoop() throws IOException, ClassNotFoundException {
        while (running && !socket.isClosed()) {
            try {
                System.out.println("👂 Waiting to receive message...");
                Object receivedObject = input.readObject();

                if (receivedObject == null) {
                    System.err.println("⚠️ Received null object");
                    continue;
                }

                if (receivedObject instanceof Response) {
                    Response response = (Response) receivedObject;

                    System.out.println("📨 Received Response:");
                    System.out.println("   - RequestID: " + response.getRequestId());
                    System.out.println("   - StatusCode: " + response.getStatusCode());
                    System.out.println("   - IsMessage: " + response.isIsMessage());
                    System.out.println("   - Sender: " + response.getSender());

                    if (response.isIsMessage()) {
                        System.out.println("🔥 Processing real-time message...");
                        handleRealtimeMessage(response);
                    } else {
                        System.out.println("🔄 Processing RPC response...");
                        ResponseManager.complete(response.getRequestId(), response);
                    }
                } else if (receivedObject instanceof Chat) {
                    System.out.println("💬 Received direct Chat object");
                    Chat chat = (Chat) receivedObject;
                    handleDirectChatUpdate(chat);
                } else {
                    System.err.println("❌ Unexpected object type received: " + receivedObject.getClass().getName());
                }

            } catch (IOException e) {
                if (running) {
                    System.err.println("❌ Error reading from socket: " + e.getMessage());
                    throw e;
                }
                break;
            }
        }
    }

    private void handleDirectChatUpdate(Chat chat) {
        Platform.runLater(() -> {
            if (chat != null) {
                handleChatUpdate(chat, chatController);
            }
        });
    }

    // Replaced handleRealtimeMessage with enhanced version:
    private void handleRealtimeMessage(Response response) {
        if (response == null) {
            System.err.println("❌ Received null response in handleRealtimeMessage");
            return;
        }

        // Handle Chat object (existing logic)
        Chat incomingChat = response.getChat();
        if (incomingChat != null) {
            System.out.println("📱 Processing real-time chat for chat ID: " + incomingChat.getChatId());
            Platform.runLater(() -> {
                try {
                    if (chatController == null) {
                        System.out.println("⏳ ChatController not ready, queuing chat for later processing");
                        pendingMessages.offer(new PendingRealTimeMessage(incomingChat, null, System.currentTimeMillis()));
                        return;
                    }
                    handleChatUpdate(incomingChat, chatController);
                    processPendingMessages();
                } catch (Exception e) {
                    System.err.println("❌ Error in handleRealtimeMessage (Chat): " + e.getMessage());
                    e.printStackTrace();
                }
            });
            return; // Exit early if we handled a Chat
        }

        // Handle Message object directly
        Message incomingMessage = response.getMessage(); // Assuming Response has a getMessage() method
        if (incomingMessage != null) {
            System.out.println("📨 Processing real-time message directly: '" + incomingMessage.getMessage() +
                    "' from: " + incomingMessage.getSender() +
                    " to: " + incomingMessage.getReceiver());

            Platform.runLater(() -> {
                try {
                    if (chatController == null) {
                        System.out.println("⏳ ChatController not ready, queuing message for later processing");
                        pendingMessages.offer(new PendingRealTimeMessage(null, incomingMessage, System.currentTimeMillis()));
                        return;
                    }
                    handleDirectMessage(incomingMessage, chatController);
                    processPendingMessages();
                } catch (Exception e) {
                    System.err.println("❌ Error in handleRealtimeMessage (Message): " + e.getMessage());
                    e.printStackTrace();
                }
            });
            return;
        }

        // Handle body object (generic fallback)
        Object bodyObject = response.getBody();
        if (bodyObject instanceof Chat) {
            Chat chat = (Chat) bodyObject;
            System.out.println("📱 Processing real-time chat from body: " + chat.getChatId());
            Platform.runLater(() -> {
                try {
                    if (chatController == null) {
                        pendingMessages.offer(new PendingRealTimeMessage(chat, null, System.currentTimeMillis()));
                        return;
                    }
                    handleChatUpdate(chat, chatController);
                    processPendingMessages();
                } catch (Exception e) {
                    System.err.println("❌ Error processing chat from body: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } else if (bodyObject instanceof Message) {
            Message message = (Message) bodyObject;
            System.out.println("📨 Processing real-time message from body: '" + message.getMessage() + "'");
            Platform.runLater(() -> {
                try {
                    if (chatController == null) {
                        pendingMessages.offer(new PendingRealTimeMessage(null, message, System.currentTimeMillis()));
                        return;
                    }
                    handleDirectMessage(message, chatController);
                    processPendingMessages();
                } catch (Exception e) {
                    System.err.println("❌ Error processing message from body: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } else {
            System.err.println("❌ Real-time response contains no Chat or Message object");
            System.err.println("   - Chat: " + incomingChat);
            System.err.println("   - Body type: " + (bodyObject != null ? bodyObject.getClass().getName() : "null"));
        }
    }

    // ... (all other existing methods: handleChatUpdate, handleDirectMessage, findChatIdByParticipant, etc.)


    /**
     * Enhanced unified method to handle chat updates with immediate UI response
     */
    private void handleChatUpdate(Chat incomingChat, ChatController controller) {
        if (incomingChat == null || controller == null) {
            return;
        }

        List<Message> incomingMessages = incomingChat.getMessages();
        if (incomingMessages == null || incomingMessages.isEmpty()) {
            System.err.println("⚠️ No messages in incoming chat");
            return;
        }

        // Get the existing chat (if any)
        Chat existingChat = ChatController.allChats.get(incomingChat.getChatId());
        int existingMessageCount = (existingChat != null && existingChat.getMessages() != null)
                ? existingChat.getMessages().size()
                : 0;

        System.out.println("📊 Existing messages: " + existingMessageCount +
                ", Incoming messages: " + incomingMessages.size());

        // Only process if there are actually new messages
        if (incomingMessages.size() <= existingMessageCount) {
            System.out.println("ℹ️ No new messages to process");
            return;
        }

        // Update the chat in local storage FIRST
        ChatController.allChats.put(incomingChat.getChatId(), incomingChat);

        // Update receiver map
        String receiverPhone = ReceiverPhone.get(incomingChat);
        if (receiverPhone != null) {
            ChatController.receiverMap.put(receiverPhone, incomingChat.getChatId());
            System.out.println("🔗 Updated receiver map: " + receiverPhone + " -> " + incomingChat.getChatId());
        }

        // Add to signed user's chat list if not already present
        boolean isNewChat = false;
        if (!SignedUser.chatList.contains(incomingChat.getChatId())) {
            SignedUser.chatList.add(0, incomingChat.getChatId()); // Add at the beginning
            SignedUser.saveToFile();
            isNewChat = true;
            System.out.println("➕ Added NEW chat " + incomingChat.getChatId() + " to user's chat list");
        } else {
            // Move existing chat to the top
            SignedUser.chatList.remove(Integer.valueOf(incomingChat.getChatId()));
            SignedUser.chatList.add(0, incomingChat.getChatId());
            SignedUser.saveToFile();
            System.out.println("📌 Moved chat " + incomingChat.getChatId() + " to top of list");
        }

        // Process NEW messages and immediately update UI
        boolean hasNewMessages = false;
        Message latestMessage = null;

        for (int i = existingMessageCount; i < incomingMessages.size(); i++) {
            Message newMsg = incomingMessages.get(i);
            if (newMsg == null) {
                System.err.println("⚠️ Null message at index " + i);
                continue;
            }

            hasNewMessages = true;
            latestMessage = newMsg; // Keep track of the latest message

            System.out.println("📨 NEW real-time message: '" + newMsg.getMessage() +
                    "' from: " + newMsg.getSender() +
                    " to: " + newMsg.getReceiver());

            // IMMEDIATELY update chat list for this new message
            Platform.runLater(() -> {
                try {
                    // Ensure sender user info is available
                    ensureSenderUserInfo(newMsg.getSender(), controller);

                    // Force immediate chat list refresh
                    controller.populateChatList();

                    // Highlight the chat with new message
                    highlightChatWithNewMessage(incomingChat.getChatId(), controller);

                    System.out.println("🔄 IMMEDIATE chat list update completed for: " + newMsg.getSender());
                } catch (Exception e) {
                    System.err.println("❌ Error in immediate chat list update: " + e.getMessage());
                }
            });

            // If viewing this chat, add message bubble immediately
            if (ChatController.currentChatId != null &&
                    ChatController.currentChatId.equals(incomingChat.getChatId())) {

                boolean isIncomingMessage = !newMsg.getSender().equals(SignedUser.phone);
                System.out.println("👀 Adding message bubble - isIncoming: " + isIncomingMessage);

                Platform.runLater(() -> {
                    controller.addMessageBubble(newMsg.getMessage(), !isIncomingMessage);

                    // Scroll to bottom
                    if (controller.messageScrollPane != null) {
                        controller.messageScrollPane.setVvalue(1.0);
                    }
                });
            } else {
                System.out.println("📱 Message received for different chat or no chat open");
                // Show notification for new message in background chat
                showEnhancedNotification(newMsg, incomingChat.getChatId(), receiverPhone, controller);
            }
        }

        // Additional immediate UI updates
        if (hasNewMessages && latestMessage != null) {
            final Message finalLatestMessage = latestMessage;
            Platform.runLater(() -> {
                // Show toast notification for new message
                showNewMessageToast(finalLatestMessage, incomingChat.getChatId(), controller);

                // Update window title if needed (for desktop notifications)
                updateWindowTitleForNewMessage(finalLatestMessage, controller);
            });
        }

        System.out.println("✅ Real-time message processing completed with immediate UI updates");
    }


    private void handleDirectMessage(Message incomingMessage, ChatController controller) {
        if (incomingMessage == null || controller == null) {
            return;
        }

        String sender = incomingMessage.getSender();
        String receiver = incomingMessage.getReceiver();
        String chatPartner = sender.equals(SignedUser.phone) ? receiver : sender;

        System.out.println("🔍 Processing direct message for chat partner: " + chatPartner);

        // 1. Find or create chat
        Integer chatId = ChatController.receiverMap.get(chatPartner);
        Chat targetChat = null;

        if (chatId != null && ChatController.allChats.containsKey(chatId)) {
            targetChat = ChatController.allChats.get(chatId);
            System.out.println("📝 Found existing chat ID: " + chatId);
        }



        // 3. Add & persist
        assert targetChat != null;
        targetChat.getMessages().add(incomingMessage);
        ChatController.allChats.put(chatId, targetChat);
        ChatController.receiverMap.put(chatPartner, chatId);

        // 4. Update user's chat list

            SignedUser.chatList.remove(Integer.valueOf(chatId));
            SignedUser.chatList.add(0, chatId);
            System.out.println("📌 Moved chat to top of user's list");

        SignedUser.saveToFile();

        // 5. Immediate UI updates
        final boolean isOutgoing = sender.equals(SignedUser.phone);
        Platform.runLater(() -> {
            controller.populateChatList();               // refresh list
            if (Objects.equals(ChatController.currentChatId, chatId)) {
                controller.addMessageBubble(incomingMessage.getMessage(), isOutgoing);
                if (controller.messageScrollPane != null) {
                    controller.messageScrollPane.setVvalue(1.0);
                }
            } else {
                showEnhancedNotification(incomingMessage, chatId, chatPartner, controller);
            }
            showNewMessageToast(incomingMessage, chatId, controller);
        });

        System.out.println("✅ Direct message processed for chat ID: " + chatId);
    }


    // Ensure sender user information is available
    private void ensureSenderUserInfo(String senderPhone, ChatController controller) {
        if (senderPhone == null || controller == null) return;

        // Check if we already have this user's info
        boolean userExists = ChatController.allChatUser.stream()
                .anyMatch(user -> user.getPhone().equals(senderPhone));

        if (!userExists) {
            // Fetch user info immediately
            String userRequestId = UUID.randomUUID().toString();
            CompletableFuture<Response> userFuture = new CompletableFuture<>();
            ResponseManager.register(userRequestId, userFuture);

            Sender.sender.searchSingle(senderPhone, userRequestId);

            userFuture.thenAccept(userRes -> {
                if (userRes.getStatusCode() == 200) {
                    User senderUser = (User) userRes.getBody();
                    if (senderUser != null) {
                        ChatController.addOrUpdateUser(senderUser);

                        // Refresh chat list again with complete user info
                        Platform.runLater(controller::populateChatList);
                        System.out.println("✅ Sender user info fetched and chat list refreshed");
                    }
                }
            });
        }
    }

    // Enhanced notification for new messages
    private void showEnhancedNotification(Message message, int chatId, String senderPhone, ChatController controller) {
        Platform.runLater(() -> {
            try {
                // Find sender info
                String senderName = "Unknown User";
                if (ChatController.allChatUser != null) {
                    Optional<User> senderUser = ChatController.allChatUser.stream()
                            .filter(user -> user.getPhone().equals(senderPhone))
                            .findFirst();

                    if (senderUser.isPresent()) {
                        senderName = senderUser.get().getName();
                    }
                }

                System.out.println("🔔 ENHANCED notification - New message from " + senderName + ": " + message.getMessage());

                // You can add system tray notification here if needed
                // showSystemTrayNotification(senderName, message.getMessage());

            } catch (Exception e) {
                System.err.println("Error showing enhanced notification: " + e.getMessage());
            }
        });
    }

    // Show toast notification for new messages
    private void showNewMessageToast(Message message, int chatId, ChatController controller) {
        try {
            // Find sender name
            String senderName = "Unknown";
            String senderPhone = message.getSender();

            if (ChatController.allChatUser != null) {
                Optional<User> senderUser = ChatController.allChatUser.stream()
                        .filter(user -> user.getPhone().equals(senderPhone))
                        .findFirst();

                if (senderUser.isPresent()) {
                    senderName = senderUser.get().getName();
                }
            }

            // Create a simple toast notification
            System.out.println("💬 TOAST: New message from " + senderName);

            // You can implement actual toast UI here
            // For now, just ensure the chat list prominently shows the sender

        } catch (Exception e) {
            System.err.println("Error showing toast: " + e.getMessage());
        }
    }

    // Highlight specific chat in the list
    private void highlightChatWithNewMessage(int chatId, ChatController controller) {
        Platform.runLater(() -> {
            try {
                // This will be handled by the enhanced populateChatList method
                // which checks for recent activity and highlights accordingly
                System.out.println("🎯 Highlighting chat " + chatId + " for new message");
            } catch (Exception e) {
                System.err.println("Error highlighting chat: " + e.getMessage());
            }
        });
    }

    // Update window title for new message indication
    private void updateWindowTitleForNewMessage(Message message, ChatController controller) {
        try {
            // You can update the window title to indicate new messages
            // This would require access to the Stage object
            System.out.println("📝 Window title would be updated for new message");
        } catch (Exception e) {
            System.err.println("Error updating window title: " + e.getMessage());
        }
    }

    /**
     * Show notification for messages received in background chats
     */
    private void showNewMessageNotification(Message message, int chatId, String senderPhone) {
        try {
            // Find sender info
            String senderName = "Unknown User";
            if (ChatController.allChatUser != null) {
                ChatController.allChatUser.stream()
                        .filter(user -> user.getPhone().equals(senderPhone))
                        .findFirst()
                        .ifPresent(user -> System.out.println("🔔 New message from " + user.getName() + ": " + message.getMessage()));
            }

            System.out.println("🔔 New background message in chat " + chatId + ": " + message.getMessage());

        } catch (Exception e) {
            System.err.println("Error showing notification: " + e.getMessage());
        }
    }

    /**
     * Process any messages that were queued while ChatController wasn't ready
     */
    private void processPendingMessages() {
        PendingRealTimeMessage pending;
        int processedCount = 0;

        while ((pending = pendingMessages.poll()) != null) {
            try {
                System.out.println("🔄 Processing queued message from " +
                        (System.currentTimeMillis() - pending.timestamp) + "ms ago");
                handleChatUpdate(pending.chat, chatController);
                processedCount++;
            } catch (Exception e) {
                System.err.println("❌ Error processing queued message: " + e.getMessage());
            }
        }

        if (processedCount > 0) {
            System.out.println("✅ Processed " + processedCount + " queued messages");
        }
    }

    /**
     * Call this method from ChatController when it's fully initialized
     */
    public static void processPendingMessagesStatic(ChatController controller) {
        Receiver.controller = controller;
        Platform.runLater(() -> {
            PendingRealTimeMessage pending;
            int processedCount = 0;

            while ((pending = pendingMessages.poll()) != null) {
                try {
                    System.out.println("🔄 Processing static queued message from " +
                            (System.currentTimeMillis() - pending.timestamp) + "ms ago");

                    // Create a temporary receiver instance to use the handleChatUpdate method
                    // Or move the handleChatUpdate logic to a static utility method
                    processQueuedChat(pending.chat, controller);
                    processedCount++;
                } catch (Exception e) {
                    System.err.println("❌ Error processing static queued message: " + e.getMessage());
                }
            }

            if (processedCount > 0) {
                System.out.println("✅ Processed " + processedCount + " static queued messages");
            }
        });
    }

    /**
     * Static method to process queued chats
     */
    private static void processQueuedChat(Chat chat, ChatController controller) {
        if (chat == null || controller == null) return;

        // Similar logic to handleChatUpdate but static
        ChatController.allChats.put(chat.getChatId(), chat);

        String receiverPhone = ReceiverPhone.get(chat);
        if (receiverPhone != null) {
            ChatController.receiverMap.put(receiverPhone, chat.getChatId());
        }

        if (!SignedUser.chatList.contains(chat.getChatId())) {
            SignedUser.chatList.add(chat.getChatId());
            SignedUser.saveToFile();
        }

        // Refresh UI
        controller.populateChatList();

        // If this is the current chat, refresh messages
        if (ChatController.currentChatId != null && ChatController.currentChatId.equals(chat.getChatId())) {
            controller.populateChat(chat);
        }
    }

    public void stopReceiver() {
        running = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("❌ Error closing receiver socket: " + e.getMessage());
        }
    }

    private void cleanup() {
        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException e) {
            System.err.println("❌ Error closing input stream: " + e.getMessage());
        }
        System.out.println("🧹 Receiver cleanup completed");
    }

    /**
     * Helper class for queuing messages when ChatController isn't ready
     */
    private record PendingRealTimeMessage(Chat chat, Message msg, long timestamp) {
    }
}