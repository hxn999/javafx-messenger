package com.db;

import java.io.File;
import java.io.FileNotFoundException;
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
    private static  List<User> allUsers;


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
    }

    public static void Add(User user) {
        String userString = user.name+":"+user.phone+":"+user.password+":"+user.url+":"+user.chatList.size()+":";
        for(int cht: user.chatList){
            userString = userString+cht+":";
        }
        userString = userString+ user.blocklist.size()+":";
        for(User usr: user.blocklist){
            userString = userString+usr.phone+":";
        }
    }

    public static User Find(String phone) throws Exception
    {
        for(User u:allUsers)
        {
            if(Objects.equals(u.phone, phone))
            {
                return u;
            }
        }
        throw new Exception("User does not exists");
    }


    public static void Load()
    {
        // read the file
        String filePath="server/src/main/db/users.txt";
        File usersFile = new File(filePath);
        try {
            Scanner Reader = new Scanner(usersFile);
            while(Reader.hasNext())
            {
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
                List<Integer> tempChatList = new ArrayList<>() ;
                for (int i = 5; i < (5+ chatCount) ; i++) {
                    tempChatList.add( Integer.parseInt(data[i]) );
                }
                // creating block list
                int blockCount = Integer.parseInt(data[5+chatCount]);
                List<User> tempBlockList = new ArrayList<>() ;
                for (int i = 6+chatCount; i < (6+ blockCount) ; i++) {
                    try {
                        User blockedUser = Find(data[i]);
                        tempBlockList.add(blockedUser);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                User temp = new User(name,url,phone,password,tempChatList,tempBlockList);

                //putting into treemap


            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}