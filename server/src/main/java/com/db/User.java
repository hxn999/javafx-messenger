package com.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class User {
    private String name;
    private String url;
    private String phone;
    private String password;
    private List<User> blocklist;
    private List<Integer> chatList;
    private static List<User> allUsers;

    static {
        allUsers = new ArrayList<>();
    }

    public static List<User> getUsers() {return allUsers;}
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

    public List<User> getBlocklist() {
        return blocklist;
    }

    public void setBlocklist(List<User> blocklist) {
        this.blocklist = blocklist;
    }

    public List<Integer> getChatList() {
        return chatList;
    }

    public void setChatList(List<Integer> chatList) {
        this.chatList = chatList;
    }

    public User(String name, String url, String phone, String password) {
        this.name = name;
        this.url = url;
        this.phone = phone;
        this.password = password;
        this.chatList = new ArrayList<>();
        this.blocklist = new ArrayList<>();
    }

    public User(String name, String url, String phone, String password, List<Integer> chatList, List<User> blocklist) {
        this.name = name;
        this.url = url;
        this.phone = phone;
        this.password = password;
        this.blocklist = blocklist;
        this.chatList = chatList;
//        this.allUsers = new ArrayList<>();
        // TODO initalize in a static block
    }

    public static void Add(User user) {
        String userString = user.name + ":" + user.phone + ":" + user.password + ":" + user.url + ":" + user.chatList.size() + ":";
        for (int cht : user.chatList) {
            userString = userString + cht + ":";
        }
        userString = userString + user.blocklist.size() + ":";
        for (User usr : user.blocklist) {
            userString = userString + usr.phone + ":";
        }

        String filePath = "server/src/main/db/users.txt";
        File usersFile = new File(filePath);
        FileWriter fw = null;
        try {
            fw = new FileWriter(usersFile, true);
            fw.write(user.toString());
            allUsers.add(user);
            fw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public static User Find(String phone) throws Exception {
        for (User u : allUsers) {

            if (u.phone.equals(phone)) {
                return u;
            }
        }
        throw new Exception("User does not exists");
    }


    public static void LoadHelper() {
        // read the file
        String filePath = "server/src/main/db/users.txt";
        File usersFile = new File(filePath);
        try {
            Scanner Reader = new Scanner(usersFile);
            while (Reader.hasNext()) {
                String line = Reader.nextLine();

                // separating data parts
                // format -> Name:Phone:Password:url:chatCount:int:int:int:int:blockCount:phone:phone:phone
                String[] data = line.split(":");
                // separating data
                String name = data[0];
                String phone = data[1];
                String password = data[2];
                String url = data[3];
                // creating chat list
                int chatCount = Integer.parseInt(data[4]);
                List<Integer> tempChatList = new ArrayList<>();
                for (int i = 5; i < (5 + chatCount); i++) {
                    tempChatList.add(Integer.parseInt(data[i]));
                }
                // creating block list
//                int blockCount = Integer.parseInt(data[5+chatCount]);
//                List<User> tempBlockList = new ArrayList<>() ;
//                for (int i = 6+chatCount; i < (6+ blockCount) ; i++) {
//                    try {
//                        User blockedUser = Find(data[i]);
//                        tempBlockList.add(blockedUser);
//                    } catch (Exception e) {
//                         e.printStackTrace();
//                    }
//                }
                // making blocklist null because first the users need to be loaded
                User temp = new User(name, url, phone, password, tempChatList, null);
                allUsers.add(temp);
                //putting into treemap


            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void Load() {
        // loading all users first
        LoadHelper();
        // read the file
        String filePath = "server/src/main/db/users.txt";
        File usersFile = new File(filePath);
        try {
            Scanner Reader = new Scanner(usersFile);
            while (Reader.hasNext()) {
                String line = Reader.nextLine();

                // separating data parts
                // format -> Name:Phone:Password:url:chatCount:int:int:int:int:blockCount:phone:phone:phone
                String[] data = line.split(":");
                // separating data
//                String name = data[0];
                String phone = data[1];
//                String password = data[2];
//                String url = data[3];
                // creating chat list
                int chatCount = Integer.parseInt(data[4]);
//                List<Integer> tempChatList = new ArrayList<>() ;
//                for (int i = 5; i < (5+ chatCount) ; i++) {
//                    tempChatList.add( Integer.parseInt(data[i]) );
//                }
                // creating block list
                int blockCount = Integer.parseInt(data[5 + chatCount]);
                System.out.println(blockCount);
                List<User> tempBlockList = new ArrayList<>();
                for (int i = 6 + chatCount; i < (6 + chatCount + blockCount); i++) {
                    try {
                        User blockedUser = Find(data[i]);

                        tempBlockList.add(blockedUser);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // making blocklist null because first the users need to be loaded
//                User temp = new User(name,url,phone,password,tempChatList,null);

                try {
                    Find(phone).setBlocklist(tempBlockList);
                } catch (Exception e) {

                    throw new RuntimeException(e);
                }
                //putting into treemap


            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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
        if (blocklist != null) {

            sb.append(":").append(blocklist.size());
            for (User u : blocklist) {
                sb.append(":").append(u.phone);
            }
        }

        return sb.toString();
    }

    // returns user string with only name phone url
    public String publicToString() {
        StringBuilder sb = new StringBuilder();

        sb.append(name).append(":")
                .append(phone).append(":")
                .append(url);

        return sb.toString();
    }

}
