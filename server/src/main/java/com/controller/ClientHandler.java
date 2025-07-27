package com.controller;

import com.api.Data;
import com.db.Chat;
import com.db.Message;
import com.db.User;
import com.server.Response;

import java.io.*;
import java.net.Socket;
import java.util.*;
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
    private ObjectInputStream request; // request will receive client data
    private ObjectOutputStream response; // response will send data to client
    private HashMap<String, Socket> clientMap; // for finding receiver socket with phone
    private Response clientResponse;


    // Helper method to send responses
    private void sendResponse(String body, int statusCode) {
        try {
            Response serverResponse = new Response("server", body, statusCode, clientResponse.getRequestId());
            response.writeObject(serverResponse);
            response.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ClientHandler(Socket socket, HashMap<String, Socket> clientMap) {
        this.socket = socket;
        this.clientMap = clientMap;

        // setting up streams for send and receive
        try {
            this.response = new ObjectOutputStream(socket.getOutputStream());
            this.request = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.out.println("Stream Error !");
            e.printStackTrace();
        }
    }

    public void router() {
        try {
            while (true) {
                // Read Response object from client
                clientResponse = (Response) request.readObject();
                System.out.println("Received request: " + clientResponse.getRequestId());

                if (clientResponse != null) {

                    switch (clientResponse.getPath()) {
                        case MSG:
                            messageSend();
                            break;
                        case LOGIN:
                            login();
                            break;
                        case CREATE_ACCOUNT:
                            createUser();
                            break;
                        case SEARCH:
                            searchUser();
                            break;
                        case BLOCK:
                            block();
                            break;
                        case UNBLOCK:
                            unBlock();
                            break;
                        case CHAT:
                            chatFetch();
                            break;

                        case SINGLE_SEARCH:
                            singleSearchUser();
                            break;
                    }
                } else {
                    System.out.println("The request is null");
                }
            }
        } catch (IOException e) {
            System.out.println("IO Error!");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found!");
            e.printStackTrace();
        }
    }

    private void singleSearchUser() {

        try {

            Data body = (Data) clientResponse.getBody();

            Optional<User> mUser = User.find(body.receiverPhone);
            System.out.println("Searching user: " + body.receiverPhone);
            if (!mUser.isPresent()) {
                System.out.println("User not found: ");
                response.writeObject(new Response(500, clientResponse.getRequestId()));
                return;
            }
            User user = mUser.get();

            System.out.println(user);

            response.writeObject(new Response("server",user,200,clientResponse.getRequestId()));

        } catch (Exception e) {

        }

    }

    private void searchUser() {


        try {

            Data body = (Data) clientResponse.getBody();
            List<User> foundUsers = new ArrayList<>();
            // using regular expression to find names
            String regex = ".*" + Pattern.quote(body.recieverName) + ".*";
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

            for (User u : User.getAllUsers()) {
                Matcher matcher = pattern.matcher(u.getName());
                // if name matched and its not the sender
                if (matcher.matches() && !u.getPhone().equals(clientResponse.getSender())) {

                    if(!u.isBlocked(body.phone))
                    {
                        System.out.println(u.getName());
                        foundUsers.add(u);
                    }




                }
            }

            response.writeObject(new Response("server",foundUsers,200,clientResponse.getRequestId()));

        } catch (Exception e) {

        }

    }

    private void messageSend() {
        try {
            System.out.println("Message send request received: " + clientResponse.getRequestId());
            Message msg = (Message) clientResponse.getBody();

            // checking if user is blocked or not
            Optional<User> mUser = User.find(msg.getReceiver());
            if (mUser.isPresent()) {
                User user = mUser.get();
                if (user.isBlocked(msg.getSender())) {
                    // receiver blocked user , nothing to send or save
                    response.writeObject(new Response(500, clientResponse.getRequestId()));
                    return;
                }
            }
            // if its the starting of the chat
            Chat chat = null;
            if (msg.isFirstMsg()) {
                System.out.println("first message");
                chat = Chat.createChat(msg.getSender(), msg.getReceiver());
                User.find(msg.getReceiver()).get().getChatList().add(chat.getChatId());
                User.find(msg.getSender()).get().getChatList().add(chat.getChatId());
                User.saveAllToFile();

                response.writeObject(new Response("server", chat, 200, clientResponse.getRequestId()));
                msg.setChatId(chat.getChatId());
            }

            Socket receiverSocket = clientMap.get(msg.getReceiver());
            ObjectOutputStream receiverStream = null;
            if (receiverSocket != null) {
                receiverStream = getCachedOutputStream(receiverSocket);
            }

            if (!msg.isFirstMsg()) {
                System.out.println("not first message");
                chat = Chat.findChat(msg.getChatId());
            }

            chat.addMessage(msg);
            response.writeObject(new Response("server", chat, 200, clientResponse.getRequestId()));

            // receiver is online
            if (receiverSocket != null && receiverStream != null) {
                try {
                    if (msg.isFirstMsg()) {
                        receiverStream.writeObject(chat);
                    } else {
                        receiverStream.writeObject(msg);
                    }
                    receiverStream.flush();
                    System.out.println("Real-time message sent to receiver: " + msg.getReceiver());
                } catch (IOException e) {
                    System.err.println("Failed to send real-time message to receiver: " + msg.getReceiver());
                    e.printStackTrace();
                }
            } else {
                System.out.println("Receiver not online: " + msg.getReceiver());
            }

        } catch (Exception e) {
            System.err.println("Error in messageSend: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Cache ObjectOutputStream per socket to avoid stream corruption
    private static final Map<Socket, ObjectOutputStream> outputStreamCache = new HashMap<>();
    private ObjectOutputStream getCachedOutputStream(Socket socket) throws IOException {
        if (!outputStreamCache.containsKey(socket)) {
            outputStreamCache.put(socket, new ObjectOutputStream(socket.getOutputStream()));
        }
        return outputStreamCache.get(socket);
    }

    private void login() {
        try {
            Data body = (Data) clientResponse.getBody();
            System.out.println(clientResponse.getRequestId());
            System.out.println("hiii");
            Optional<User> mUser = User.find(body.phone);

            if (!mUser.isPresent()) {
                response.writeObject(new Response(500, clientResponse.getRequestId()));
                return;
            }
            User user = mUser.get();

            if (user.getPassword().equals(body.password)) {
                response.writeObject(new Response("server", user, 200, clientResponse.getRequestId()));
                // Register user's socket for real-time messaging
                clientMap.put(user.getPhone(), socket);
            } else {
                response.writeObject(new Response(500, clientResponse.getRequestId()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createUser() {
        try {
            User newUser = (User) clientResponse.getBody();
            Optional<User> mUser = User.find(newUser.getPhone());
            if (mUser.isPresent()) {
                response.writeObject(new Response(500, clientResponse.getRequestId()));
                return;
            }
            User.addUser(newUser);
            response.writeObject(new Response("server", newUser, 200, clientResponse.getRequestId()));
            // Register new user's socket for real-time messaging
            clientMap.put(newUser.getPhone(), socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void chatFetch() {
        try {

            Data body = (Data) clientResponse.getBody();

            Chat chat = Chat.findChat(body.chatId);

            response.writeObject(new Response("server", chat, 200, clientResponse.getRequestId()));


        } catch (Exception e) {
            try {
                response.writeObject(new Response(500, clientResponse.getRequestId()));
            } catch (IOException ex) {
                e.printStackTrace();
            }
        }

    }

    private void block() {
        try {

            Data body = (Data) clientResponse.getBody();

            Optional<User> mUser = User.find(body.phone);
            if (mUser.isPresent()) {
                User user = mUser.get();
                user.block(body.receiverPhone);
            }


        } catch (Exception e) {

        }
    }
    private void unBlock() {
        try {

            Data body = (Data) clientResponse.getBody();

            Optional<User> mUser = User.find(body.phone);
            if (mUser.isPresent()) {
                User user = mUser.get();
                user.unblock(body.receiverPhone);
            }


        } catch (Exception e) {

        }
    }


}
