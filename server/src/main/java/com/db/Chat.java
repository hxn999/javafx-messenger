package com.db;

import java.io.*;
import java.util.*;

public class Chat implements Serializable {
    private int chatId;
    private String user1;
    private String user2;
    private List<Message> messages;

    private static final String FILE_PATH = "server/src/main/db/chats.dat";
    private static final Map<Integer, Chat> allChats = new HashMap<>();
    private static int chatCount = 0;

    // Constructors
    public Chat(int chatId, String user1, String user2) {
        this.chatId = chatId;
        this.user1 = user1;
        this.user2 = user2;
        this.messages = new ArrayList<>();
    }

    // Getters
    public int getChatId() { return chatId; }
    public String getUser1() { return user1; }
    public String getUser2() { return user2; }
    public List<Message> getMessages() { return messages; }

    public static Map<Integer, Chat> getAllChats() {
        return allChats;
    }

    // Add message to a chat
    public void addMessage(Message message) {
        messages.add(message);
        saveAllChats();
    }

    // Create a new chat and store it
    public static Chat createChat(String phone1, String phone2) {
        Chat chat = new Chat(chatCount++, phone1, phone2);
        allChats.put(chat.chatId, chat);
        saveAllChats();
        return chat;
    }

    // Find chat by ID
    public static Chat findChat(int chatId) throws Exception {
        if (!allChats.containsKey(chatId))
            throw new Exception("Chat not found.");
        return allChats.get(chatId);
    }

    // Save all chats to file
    public static void saveAllChats() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            out.writeObject(allChats);
        } catch (IOException e) {
            System.err.println("Error saving chats: " + e.getMessage());
        }
    }

    // Load all chats on server startup
    public static void loadAllChats() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Map<Integer, Chat> loadedChats = (Map<Integer, Chat>) in.readObject();
            allChats.clear();
            allChats.putAll(loadedChats);
            // Update chatCount so IDs stay unique
            chatCount = allChats.keySet().stream().max(Integer::compareTo).orElse(0) + 1;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading chats: " + e.getMessage());
        }
    }

    // Optional: Print messages (for debugging)
    public void printMessages() {
        for (Message msg : messages) {
            System.out.println("[" + msg.getTimestamp() + "] " +
                    msg.getSender() + " → " + msg.getReceiver() + ": " +
                    msg.getMessage());
        }
    }
}
