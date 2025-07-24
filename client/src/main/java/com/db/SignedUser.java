package com.db;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SignedUser {
    public static String name;
    public static String url;
    public static String phone;
    public static String password;
    public static List<String> blocklistPhones = new ArrayList<>();
    public static List<Integer> chatList = new ArrayList<>();

    private static final String FILE_PATH = "client/src/main/resources/db/signedUser.dat";

    private static class SignedUserData implements Serializable {
        String name;
        String url;
        String phone;
        String password;
        List<String> blocklistPhones;
        List<Integer> chatList;
    }

    public static void saveToFile() {
        SignedUserData data = new SignedUserData();
        data.name = name;
        data.url = url;
        data.phone = phone;
        data.password = password;
        data.blocklistPhones = blocklistPhones;
        data.chatList = chatList;

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            out.writeObject(data);
        } catch (IOException e) {
            System.err.println("Error saving signed user: " + e.getMessage());
        }
    }

    public static void loadFromFile() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            SignedUserData data = (SignedUserData) in.readObject();
            name = data.name;
            url = data.url;
            phone = data.phone;
            password = data.password;
            blocklistPhones = data.blocklistPhones;
            chatList = data.chatList;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading signed user: " + e.getMessage());
        }
    }

    public static void logout() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("Signed out successfully.");
                // Clear in-memory data too
                name = null;
                url = null;
                phone = null;
                password = null;
                blocklistPhones = new ArrayList<>();
                chatList = new ArrayList<>();
            } else {
                System.err.println("Failed to delete signed user file.");
            }
        }
    }


    public static boolean isLoggedIn() {
        loadFromFile();
        File file = new File(FILE_PATH);
        return file.exists();
    }


    public static boolean isBlocked(String phoneToCheck) {
        return blocklistPhones != null && blocklistPhones.contains(phoneToCheck);
    }

    public static void save(User user) {
        name = user.getName();
        url = user.getUrl();
        phone = user.getPhone();
        password = user.getPassword();
        blocklistPhones = new ArrayList<>(user.getBlocklistPhones());
        chatList = new ArrayList<>(user.getChatList());

        saveToFile();
    }

    public static String publicToString() {
        return name + ":" + phone + ":" + url;
    }

    @Override
    public String toString() {
        return "SignedUser{name='" + name + "', phone='" + phone + "'}";
    }
}
