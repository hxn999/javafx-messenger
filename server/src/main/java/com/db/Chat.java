package com.db;

import java.io.*;
import java.util.Scanner;
import java.util.TreeMap;

public class Chat {
    private int chatId;
    private String user1;
    private String user2;
    private String filePath;
    private File chatFile;
    // will store all messages sorted by timestamp
    private TreeMap<Integer,String> chats ;


    public TreeMap<Integer, String> getChats() {
        return chats;
    }

    public void setChats(TreeMap<Integer, String> chats) {
        this.chats = chats;
    }

    public Chat(int chatId) {

        this.chatId = chatId;
        chats = new TreeMap<>();

        // read the file
        this.filePath="server/src/main/db/chat-"+chatId+".txt";
        this.chatFile = new File(filePath);
        try {
            Scanner Reader = new Scanner(chatFile);
            while(Reader.hasNext())
            {
                String line = Reader.nextLine();

                // separating timestamp and message
                int timestampIndex = line.lastIndexOf(':');
                String message = line.substring(0,timestampIndex);
                String timestampString = line.substring(timestampIndex+1,line.length());
                int timestamp = Integer.parseInt(timestampString);

                //putting into treemap
                chats.put(timestamp,message);

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }



    }

    public void add(String msg)
    {
        try {
            FileWriter fw = new FileWriter(chatFile,true);
            fw.write(msg);
        } catch (IOException e) {
            System.out.println("Appending failed");
        }
    }

    public static void main(String[] args) {
        Chat le  = new Chat(1);
    }
    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public String getUser1() {
        return user1;
    }

    public void setUser1(String user1) {
        this.user1 = user1;
    }

    public String getUser2() {
        return user2;
    }

    public void setUser2(String user2) {
        this.user2 = user2;
    }
}
