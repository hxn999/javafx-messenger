package com.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.TreeMap;

public class ClientChat {
    private int chatId;
    private String user1;
    private String user2;
    private String filePath;
    private File chatFile;
    // will store all messages sorted by timestamp
    private TreeMap<Long,String> chats ;


    public TreeMap<Long, String> getChats() {
        return chats;
    }

    public void setChats(TreeMap<Long, String> chats) {
        this.chats = chats;
    }

    public ClientChat(int chatId) {

        this.chatId = chatId;
        chats = new TreeMap<>();

        // read the file
        this.filePath="client/src/main/resources/db/chat-"+chatId+".txt";
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
                long timestamp = Integer.parseInt(timestampString);

                //putting into treemap
                chats.put(timestamp,message);

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }



    }

    public static Chat CreateChat(String phone1,String phone2,int chatId) {
        String newFilePath = "client/src/main/resources/db/chat-"+chatId+".txt";
        String newSentLinesFilePath = "client/src/main/resources/db/chat-"+chatId+".txt";

        File newChatFile = new File(newFilePath);
        File newSentLinesFile = new File(newSentLinesFilePath);

        try {
            newChatFile.createNewFile();
            newSentLinesFile.createNewFile();
            FileWriter fw = new FileWriter(newChatFile);
            FileWriter fw2 = new FileWriter(newSentLinesFile);
            User user1 = User.Find(phone1);
            User user2 = User.Find(phone2);
            fw.write(user1.publicToString() + "\n" + user2.publicToString() + "\n");
            fw.close();
            fw2.write("0 0");
            fw2.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        Chat chat = new Chat(chatId);

        File chatCountFile = new File("client/src/main/resources/db/chat-"+chatId+".txt");

        try {
            FileWriter fw = new FileWriter(chatCountFile);

            fw.write(String.valueOf(chatId));
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return chat;

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
