package com.controller;

import com.db.Chat;
import com.db.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
        Request style from client

        "REQUEST TYPE (router will call function according to req type)
        PAYLOAD/BODY"

        for messaging
        "MSG
         RECEIVER PHONE
         name:message:timestamp"

         for login
         "LOGIN
          PHONE
          PASSWORD"

          for account creation
         "CREATE
          NAME
          PHONE
          PASSWORD
          URL
          "

          for searching accounts
         "SEARCH
          NAME
          "
          for getting all chat update
         "CHAT_UPDATE
          PHONE
          "


        server will response the client with a status code and then payload

        "STATUS_CODE
         PAYLOAD"

         status codes
         200 -> ok
         401 -> unauthorized
         404 -> not found
         500 -> internal server error




*/


public class ClientHandler {
    private Socket socket;
    private BufferedReader request; // request will receive client data
    private PrintWriter response; // response will send data to client
    private List<Socket> clients; // store all online clients
    private HashMap<String, Integer> clientMap; // for finding recienver socket with phone

    public ClientHandler(Socket socket, List<Socket> clients, HashMap<String, Integer> clientMap) {
        this.socket = socket;
        this.clients = clients;
        this.clientMap = clientMap;

        // setting up streams for send and recieve
        try {
            this.request = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.response = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("Stream Error !");
        }

    }

    public void router() {
        try {
            while (true) {
//                System.out.println("hi");
                String type = request.readLine();
                System.out.println(type);

                if (type != null) {

                    switch (type) {
                        case "MSG":
                            messageSend();
                            break;
                        case "LOGIN":
                            System.out.println("its login");
                            login();
                            break;
                        case "CREATE":
                            createUser();
                            break;
                        case "SEARCH":
                            searchUser();
                            break;
                        case "CHAT_UPDATE":
                            chatUpdate();
                            break;

                    }
                } else {
//                    System.out.println("the req is null");
                }


//            System.out.println("h");
            }

        } catch (IOException e) {
            System.out.println("IO Error !");
        }
    }

    private void searchUser() {
        try {
            String name = request.readLine();
            String searcherPhone = request.readLine();
            String regex = ".*" + Pattern.quote(name) + ".*";
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            String responseString = "";
            for (User u : User.getUsers()) {
                Matcher matcher = pattern.matcher(u.getName());
                if (matcher.matches()) {

                    for (User blockedUser : u.getBlocklist()) {
                        // if user has blocked searcher , we dont send
                        if (blockedUser.getPhone().equals(searcherPhone)) {
                            continue;
                        }
                    }


                    responseString = responseString + "," + u.publicToString();
                }
            }

            System.out.println(responseString);
            responseString = "200\n" + responseString + "\n";

            response.println(responseString);

        } catch (IOException e) {
            response.println("404");
        }
    }

    private void messageSend() {
        try {
            String chatId = request.readLine();
            String receiverPhone = request.readLine();
            String senderPhone = request.readLine();
            Chat chat = null;

            // need to create new chat file
            if (chatId.equals("null")) {
                chat = Chat.CreateChat(senderPhone, receiverPhone);
                chatId = String.valueOf(chat.getChatId());
            } else {
                chat = new Chat(Integer.parseInt(chatId));
            }

            Socket receiverSocket = socket;
            int hashCode = clientMap.get(receiverPhone); // getting receiver socket hashcode
            // searching receiver socket
            for (Socket soc : clients) {
                if (soc.hashCode() == hashCode) {
                    receiverSocket = soc;
                    break;
                }
            }


            // receiver isnt online so socket not found
            if (receiverSocket.hashCode() == socket.hashCode()) {
                chat.add(request.readLine());

            }

            // receiver is online and socket found
            else {


                String msg = request.readLine();
                chat.add(msg);

                // sending to the receiver
                PrintWriter receiverResponse = new PrintWriter(receiverSocket.getOutputStream(), true);
                receiverResponse.println(chatId+"\n"+msg+"\n");

            }

            PrintWriter receiverResponse = new PrintWriter(receiverSocket.getOutputStream(), true);


        } catch (IOException e) {

        }


    }

    private void login() {
        try {
            String phone = request.readLine();
            String password = request.readLine();

            System.out.println(phone);
            System.out.println(password);


            try {
                User user = User.Find(phone);
                if (Objects.equals(user.getPassword(), password)) {
                    String userString = user.toString();
                    String responseString = "200\n" +
                            userString +
                            "\n";

                    response.println(responseString);
                    return;
                } else {
                    response.println("401");
                }
            } catch (Exception e) {
                response.println("404");
            }


        } catch (IOException e) {
            System.out.println("500");
        }

    }

    private void createUser() {

        try {
            System.out.println("hi create");
            String name = request.readLine();
            String phone = request.readLine();
            String password = request.readLine();
//            String url = request.readLine();
            String url = "";
            User newUser = new User(name, url, phone, password);

            User.Add(newUser);

            String userString = newUser.toString();
            String responseString = "200\n" +
                    userString +
                    "\n";

            response.println(responseString);
        } catch (IOException e) {
            response.println("500");
        }

    }

    private void chatUpdate() {
        try {
            String phone = request.readLine();
            User user = User.Find(phone);

            // Get all chats for this user
            StringBuilder chatData = new StringBuilder();

            // For each chat ID in user's chat list
            for (int chatId : user.getChatList()) {
                Chat chat = new Chat(chatId);

                // Get the chat file content
                chat.getSentLines();

                // Add chat data to response
                chatData.append(chatId).append("\n");

                // Add chat messages that haven't been sent to this user yet
                try {
                    long totalLines = chat.countTotalFileLines();
                    int sentLines = 0;

                    // Determine if this user is user1 or user2 in the chat
                    if (chat.getUser1() != null && chat.getUser1().equals(phone)) {
                        sentLines = chat.getUser1sentLines();
                        chat.setUser1sentLines((int)totalLines);
                    } else if (chat.getUser2() != null && chat.getUser2().equals(phone)) {
                        sentLines = chat.getUser2sentLines();
                        chat.setUser2sentLines((int)totalLines);
                    }

                    // Add unsent messages to response
                    if (totalLines > sentLines) {
                        // Read the chat file and add unsent lines
                        java.io.File chatFile = new java.io.File(chat.getFilePath());
                        java.util.Scanner reader = new java.util.Scanner(chatFile);

                        // Skip lines that have already been sent
                        for (int i = 0; i < sentLines; i++) {
                            if (reader.hasNextLine()) {
                                reader.nextLine();
                            }
                        }

                        // Add unsent lines to response
                        while (reader.hasNextLine()) {
                            chatData.append(reader.nextLine()).append("\n");
                        }
                        reader.close();
                    }

                    // Update sent lines count
                    chat.updateSentLines();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                chatData.append("|\n");
            }

            // Send response to client
            String responseString = "200\n" + chatData.toString() ;
            response.println(responseString);

        } catch (Exception e) {
            System.out.println("500");
            response.println("500");
            e.printStackTrace();
        }
    }

}
