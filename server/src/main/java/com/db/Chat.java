package com.db;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Scanner;
import java.util.TreeMap;

public class Chat {
    private int chatId;
    private String user1;
    private String user2;
    private String filePath;
    private File chatFile;
    private static int chatCount;
    // will store all messages sorted by timestamp
    private TreeMap<Long, String> chats;

    // tracks how much lines from the chat file has sent to user1
    private int user1sentLines;
    // tracks how much lines from the chat file has sent to user2
    private int user2sentLines;

    static {
        File chatCountFile = new File("server/src/main/db/totalChatCount.txt");

        try {
            Scanner chatCountScanner = new Scanner(chatCountFile);
            chatCount = chatCountScanner.nextInt();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public TreeMap<Long, String> getChats() {
        return chats;
    }

    public void setChats(TreeMap<Long, String> chats) {
        this.chats = chats;
    }

    public Chat(int chatId) {

        this.chatId = chatId;
        chats = new TreeMap<>();

        // read the file
        this.filePath = "server/src/main/db/chat-" + chatId + ".txt";
        this.chatFile = new File(filePath);
        try {
            Scanner Reader = new Scanner(chatFile);
            while (Reader.hasNext()) {
                String line = Reader.nextLine();

                // separating timestamp and message
                int timestampIndex = line.lastIndexOf(':');
                String message = line.substring(0, timestampIndex);
                String timestampString = line.substring(timestampIndex + 1, line.length());
                long timestamp = Integer.parseInt(timestampString);

                //putting into treemap
                chats.put(timestamp, message);

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }

    public void add(String msg) {
        try {
            FileWriter fw = new FileWriter(chatFile, true);
            fw.write(msg);
        } catch (IOException e) {
            System.out.println("Appending failed");
        }
    }

    public static Chat CreateChat(String phone1,String phone2) {
        String newFilePath = "server/src/main/db/chat-" + chatCount + ".txt";
        String newSentLinesFilePath = "server/src/main/db/chat-" + chatCount +"-sentLines.txt";

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

        Chat chat = new Chat(chatCount);

        File chatCountFile = new File("server/src/main/db/totalChatCount.txt");

        try {
            FileWriter fw = new FileWriter(chatCountFile);

            fw.write(String.valueOf(chatCount));
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return chat;

    }

    public void updateSentLines() {
        String newSentLinesFilePath = "server/src/main/db/chat-" + chatCount +"-sentLines.txt";
        File newSentLinesFile = new File(newSentLinesFilePath);
        try {
            FileWriter fw = new FileWriter(newSentLinesFile);
            String updatedSentLines = user1sentLines+" "+user2sentLines;
            fw.write(updatedSentLines);
            fw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long countTotalFileLines() throws IOException {
        return Files.lines(chatFile.toPath()).count();
    }

    public void getSentLines()
    {
        String newSentLinesFilePath = "server/src/main/db/chat-" + chatCount +"-sentLines.txt";
        File newSentLinesFile = new File(newSentLinesFilePath);
        try {
            Scanner reader = new Scanner(newSentLinesFile);
            user1sentLines = reader.nextInt();
            user2sentLines = reader.nextInt();
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

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

    public int getUser1sentLines() {
        return user1sentLines;
    }

    public void setUser1sentLines(int user1sentLines) {
        this.user1sentLines = user1sentLines;
    }

    public int getUser2sentLines() {
        return user2sentLines;
    }

    public void setUser2sentLines(int user2sentLines) {
        this.user2sentLines = user2sentLines;
    }

    public String getFilePath() {
        return filePath;
    }
}
