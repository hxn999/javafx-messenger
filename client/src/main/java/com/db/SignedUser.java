package com.db;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SignedUser {

    public static String name;
    public static String url;
    public static String phone;
    public static String password;
    public static List<String> blockList;
    public static List<Integer> chatList;
    public static String filePath ;
static{

    filePath = "client/src/main/resources/db/signedUser.txt";
}
    public static void Load() {
        // read the file

        File usersFile = new File(filePath);
        try {
            Scanner Reader = new Scanner(usersFile);
            while (Reader.hasNext()) {
                String line = Reader.nextLine();

                // separating data parts
                // format -> Name:Phone:Password:url:chatCount:int:int:int:int:blockCount:phone:phone:phone
                String[] data = line.split(":");
                // separating data
                name = data[0];
                phone = data[1];
                password = data[2];
                url = data[3];
                // creating chat list
                int chatCount = Integer.parseInt(data[4]);
                chatList = new ArrayList<>();
                for (int i = 5; i < (5 + chatCount); i++) {
                    chatList.add(Integer.parseInt(data[i]));
                }
                // creating block list
                int blockCount = Integer.parseInt(data[5+chatCount]);
//                ArrayList<Object> blockList = new ArrayList<>();
                blockList = new ArrayList<>();
                for (int i = 6+chatCount; i < (6+ blockCount) ; i++) {
                    try {
                        blockList.add(data[i]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void Save(String body) {

        File usersFile = new File(filePath);
        try {
            FileWriter fw = new FileWriter(usersFile,true);
            fw.write(body);
            fw.close();
            Load();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isLoggedIn()
    {
        File file = new File(filePath);
        if(file.length()==0||!file.exists())
        {
            return false;
        }else{
            return true;
        }
    }



















    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getBlocklist() {
        return blockList;
    }

    public void setBlocklist(List<String> blocklist) {
        this.blockList = blocklist;
    }

    public List<Integer> getChatList() {
        return chatList;
    }

    public void setChatList(List<Integer> chatList) {
        this.chatList = chatList;
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(name).append(":")
                .append(phone).append(":")
                .append(password).append(":")
                .append(url).append(":");

        // Chat list count and values
        sb.append(chatList.size());
        for (int chatId : chatList) {
            sb.append(":").append(chatId);
        }

        // Block list count and phone numbers
        if (blockList != null) {

            sb.append(":").append(blockList.size());
            for (String phone : blockList) {
                sb.append(":").append(phone);
            }
        }

        return sb.toString();
    }

}
