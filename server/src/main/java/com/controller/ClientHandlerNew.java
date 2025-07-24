//package com.controller;
//
//import com.db.Chat;
//import com.db.User;
//import com.server.Response;
//
//import java.io.*;
//import java.net.Socket;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Objects;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
///*
//        Request style from client
//
//        "REQUEST TYPE (router will call function according to req type)
//        PAYLOAD/BODY"
//
//        for messaging
//        "MSG
//         RECEIVER PHONE
//         name:message:timestamp"
//
//         for login
//         "LOGIN
//          PHONE
//          PASSWORD"
//
//          for account creation
//         "CREATE
//          NAME
//          PHONE
//          PASSWORD
//          URL
//          "
//
//          for searching accounts
//         "SEARCH
//          NAME
//          "
//          for getting all chat update
//         "CHAT_UPDATE
//          PHONE
//          "
//
//
//        server will response the client with a status code and then payload
//
//        "STATUS_CODE
//         PAYLOAD"
//
//         status codes
//         200 -> ok
//         401 -> unauthorized
//         404 -> not found
//         500 -> internal server error
//
//
//
//
//*/
//
//
//public class ClientHandlerNew {
//    private Socket socket;
//    private ObjectInputStream request; // request will receive client data
//    private ObjectOutputStream response; // response will send data to client
//    private List<Socket> clients; // store all online clients
//    private HashMap<String, Integer> clientMap; // for finding receiver socket with phone
//
//    // Helper method to send responses
//    private void sendResponse(String body, int statusCode) {
//        try {
//            Response serverResponse = new Response("server", body, statusCode);
//            response.writeObject(serverResponse);
//            response.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public ClientHandlerNew(Socket socket, List<Socket> clients, HashMap<String, Integer> clientMap) {
//        this.socket = socket;
//        this.clients = clients;
//        this.clientMap = clientMap;
//
//        // setting up streams for send and receive
//        try {
//            this.response = new ObjectOutputStream(socket.getOutputStream());
//            this.request = new ObjectInputStream(socket.getInputStream());
//        } catch (IOException e) {
//            System.out.println("Stream Error !");
//            e.printStackTrace();
//        }
//    }
//
//    public void router() {
//        try {
//            while (true) {
//                // Read Response object from client
//                Response clientResponse = (Response) request.readObject();
//                System.out.println("Received request: " + clientResponse);
//
//                if (clientResponse != null) {
//                    // Extract the request type from the body
//                    String requestBody = (String) clientResponse.getBody();
//                    String[] lines = requestBody.split("\n");
//                    String type = lines[0];
//                    System.out.println("Request type: " + type);
//
//                    switch (type) {
//                        case "MSG":
//                            messageSend(clientResponse);
//                            break;
//                        case "LOGIN":
//                            System.out.println("its login");
//                            login(clientResponse);
//                            break;
//                        case "CREATE":
//                            createUser(clientResponse);
//                            break;
//                        case "SEARCH":
//                            searchUser(clientResponse);
//                            break;
//                        case "SEARCHABLE":
//                            searchUserToBlock(clientResponse);
//                            break;
//                        case "CHAT_UPDATE":
//                            chatUpdate(clientResponse);
//                            break;
//                    }
//                } else {
//                    System.out.println("The request is null");
//                }
//            }
//        } catch (IOException e) {
//            System.out.println("IO Error!");
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            System.out.println("Class not found!");
//            e.printStackTrace();
//        }
//    }
//
//
//    private void searchUserToBlock(Response clientResponse) {
//        try {
//            // Extract data from the Response object
//            String requestBody = (String) clientResponse.getBody();
//            String[] lines = requestBody.split("\n");
//
//            // Who's doing the searching
//            String searcherPhone = lines[1];
//            // Whom to look up
//            String targetPhone = lines[2];
//
//            // Find the target user in the list
//            User target = null;
//            for (User u : User.getUsers()) {
//                if (u.getPhone().equals(targetPhone)) {
//                    target = u;
//                    break;
//                }
//            }
//
//            if (target == null || target.getPhone().equals(searcherPhone)) {
//                // 404 = target not in DB
//                sendResponse("404", 404);
//            } else {
//                // Check if the target has already blocked the searcher
//                boolean hasBlocked = false;
//                User currentUser = User.Find(searcherPhone);
//                for (User blockedUser : currentUser.getBlocklist()) {
//                    if (blockedUser.getPhone().equals(targetPhone)) {
//                        hasBlocked = true;
//                        break;
//                    }
//                }
//
//                if (hasBlocked) {
//                    sendResponse("403", 403); //already blocked
//                } else {
//                    // 200 = OK; send back the target's public data
//                    sendResponse(target.publicToString(), 200);
//                }
//            }
//        } catch (Exception e) {
//            sendResponse("500", 500);
//            e.printStackTrace();
//        }
//    }
//
//
//    private void searchUser(Response clientResponse) {
//        try {
//            // Extract data from the Response object
//            String requestBody = (String) clientResponse.getBody();
//            String[] lines = requestBody.split("\n");
//
//            String name = lines[1];
//            String searcherPhone = lines[2];
//
//            String regex = ".*" + Pattern.quote(name) + ".*";
//            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
//            String responseString = "";
//            for (User u : User.getUsers()) {
//                Matcher matcher = pattern.matcher(u.getName());
//                if (matcher.matches()) {
//                    for (User blockedUser : u.getBlocklist()) {
//                        // if user has blocked searcher , we dont send
//                        if (blockedUser.getPhone().equals(searcherPhone)) {
//                            continue;
//                        }
//                    }
//                    responseString = responseString + "," + u.publicToString();
//                }
//            }
//
//            System.out.println(responseString);
//            sendResponse(responseString, 200);
//        } catch (Exception e) {
//            sendResponse("404", 404);
//            e.printStackTrace();
//        }
//    }
//
//    private void messageSend(Response clientResponse) {
//        try {
//            // Extract data from the Response object
//            String requestBody = (String) clientResponse.getBody();
//            String[] lines = requestBody.split("\n");
//
//            String chatId = lines[1];
//            String receiverPhone = lines[2];
//            String senderPhone = lines[3];
//            String message = lines[4];
//
//            Chat chat = null;
//
//            // need to create new chat file
//            if (chatId.equals("null")) {
//                chat = Chat.CreateChat(senderPhone, receiverPhone);
//                chatId = String.valueOf(chat.getChatId());
//            } else {
//                chat = new Chat(Integer.parseInt(chatId));
//            }
//
//            Socket receiverSocket = socket;
//            int hashCode = clientMap.get(receiverPhone); // getting receiver socket hashcode
//            // searching receiver socket
//            for (Socket soc : clients) {
//                if (soc.hashCode() == hashCode) {
//                    receiverSocket = soc;
//                    break;
//                }
//            }
//
//            // receiver isnt online so socket not found
//            if (receiverSocket.hashCode() == socket.hashCode()) {
//                chat.add(message);
//            }
//            // receiver is online and socket found
//            else {
//                chat.add(message);
//
//                // sending to the receiver
//                ObjectOutputStream receiverOut = new ObjectOutputStream(receiverSocket.getOutputStream());
//                Response messageResponse = new Response("server", chatId + "\n" + message + "\n", 200);
//                receiverOut.writeObject(messageResponse);
//                receiverOut.flush();
//            }
//        } catch (IOException e) {
//            sendResponse("500", 500);
//            e.printStackTrace();
//        }
//    }
//
//    private void login(Response clientResponse) {
//        try {
//            // Extract data from the Response object
//            String requestBody = (String) clientResponse.getBody();
//            String[] lines = requestBody.split("\n");
//
//            String phone = lines[1];
//            String password = lines[2];
//
//            System.out.println(phone);
//            System.out.println(password);
//
//            try {
//                User user = User.Find(phone);
//                if (Objects.equals(user.getPassword(), password)) {
//                    String userString = user.toString();
//                    sendResponse(userString, 200);
//                    return;
//                } else {
//                    sendResponse("401", 401);
//                }
//            } catch (Exception e) {
//                sendResponse("404", 404);
//            }
//        } catch (Exception e) {
//            System.out.println("500");
//            sendResponse("500", 500);
//            e.printStackTrace();
//        }
//    }
//
//    private void createUser(Response clientResponse) {
//        try {
//            // Extract data from the Response object
//            String requestBody = (String) clientResponse.getBody();
//            String[] lines = requestBody.split("\n");
//
//            System.out.println("hi create");
//            String name = lines[1];
//            String phone = lines[2];
//            String password = lines[3];
//            String url = "";
//            User newUser = new User(name, url, phone, password);
//
//            User.Add(newUser);
//
//            String userString = newUser.toString();
//            sendResponse(userString, 200);
//        } catch (Exception e) {
//            sendResponse("500", 500);
//            e.printStackTrace();
//        }
//    }
//
//    private void chatUpdate(Response clientResponse) {
//        try {
//            // Extract data from the Response object
//            String requestBody = (String) clientResponse.getBody();
//            String[] lines = requestBody.split("\n");
//
//            String phone = lines[1];
//            User user = User.Find(phone);
//
//            // Get all chats for this user
//            StringBuilder chatData = new StringBuilder();
//
//            // For each chat ID in user's chat list
//            for (int chatId : user.getChatList()) {
//                Chat chat = new Chat(chatId);
//
//                // Get the chat file content
//                chat.getSentLines();
//
//                // Add chat data to response
//                chatData.append(chatId).append("\n");
//
//                // Add chat messages that haven't been sent to this user yet
//                try {
//                    long totalLines = chat.countTotalFileLines();
//                    int sentLines = 0;
//
//                    // Determine if this user is user1 or user2 in the chat
//                    if (chat.getUser1() != null && chat.getUser1().equals(phone)) {
//                        sentLines = chat.getUser1sentLines();
//                        chat.setUser1sentLines((int)totalLines);
//                    } else if (chat.getUser2() != null && chat.getUser2().equals(phone)) {
//                        sentLines = chat.getUser2sentLines();
//                        chat.setUser2sentLines((int)totalLines);
//                    }
//
//                    // Add unsent messages to response
//                    if (totalLines > sentLines) {
//                        // Read the chat file and add unsent lines
//                        java.io.File chatFile = new java.io.File(chat.getFilePath());
//                        java.util.Scanner reader = new java.util.Scanner(chatFile);
//
//                        // Skip lines that have already been sent
//                        for (int i = 0; i < sentLines; i++) {
//                            if (reader.hasNextLine()) {
//                                reader.nextLine();
//                            }
//                        }
//
//                        // Add unsent lines to response
//                        while (reader.hasNextLine()) {
//                            chatData.append(reader.nextLine()).append("\n");
//                        }
//                        reader.close();
//                    }
//
//                    // Update sent lines count
//                    chat.updateSentLines();
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                chatData.append("|\n");
//            }
//
//            // Send response to client
//            sendResponse(chatData.toString(), 200);
//        } catch (Exception e) {
//            System.out.println("500");
//            sendResponse("500", 500);
//            e.printStackTrace();
//        }
//    }
//}