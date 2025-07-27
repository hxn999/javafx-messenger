package com.db;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class User implements Serializable {
    private String name;
    private String url;
    private String phone;
    private String password;
    private List<String> blocklistPhones;
    private List<Integer> chatList;

    private static final String FILE_PATH = "server/src/main/db/users.dat";
    private static final List<User> allUsers = new ArrayList<>();

    // Constructors
    public User(String name, String url, String phone, String password) {
        this.name = name;
        this.url = url;
        this.phone = phone;
        this.password = password;
        this.chatList = new ArrayList<>();
        this.blocklistPhones = new ArrayList<>();
    }

    // Getters
    public String getName() { return name; }
    public String getUrl() { return url; }
    public String getPhone() { return phone; }
    public String getPassword() { return password; }
    public List<String> getBlocklistPhones() { return blocklistPhones; }
    public List<Integer> getChatList() { return chatList; }

    public static List<User> getAllUsers() {
        return allUsers;
    }

    // Add and save new user
    public static void addUser(User user) {
        allUsers.add(user);
        saveAllToFile();
    }

    // Find user by phone
    public static Optional<User> find(String phone) {
        System.out.println("hi finding user with phone: " + phone);
        return allUsers.stream().filter(u -> u.phone.equals(phone)).findFirst();
    }

    // Add phone to blocklist
    public void block(String phoneToBlock) {
        if (!blocklistPhones.contains(phoneToBlock)) {
            blocklistPhones.add(phoneToBlock);
            saveAllToFile();
        }
    }
    public boolean isBlocked(String phone) {
        return blocklistPhones != null && blocklistPhones.contains(phone);
    }

    public void unblock(String phone) {
        if (blocklistPhones != null && blocklistPhones.contains(phone)) {
            blocklistPhones.remove(phone);
            User.saveAllToFile(); // update the file after change
        }
    }

    // Save all users to file
    public static void saveAllToFile() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            out.writeObject(allUsers);
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    // Load all users from file
    public static void loadFromFile() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            List<User> loaded = (List<User>) in.readObject();
            allUsers.clear();
            allUsers.addAll(loaded);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    // For public display (name, phone, url)
    public String publicToString() {
        return name + ":" + phone + ":" + url;
    }

    @Override
    public String toString() {
        return "User{name='" + name + "', phone='" + phone + "'}";
    }
}
