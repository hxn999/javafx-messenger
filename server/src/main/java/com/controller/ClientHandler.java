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
            Response serverResponse = new Response("server", body, statusCode);
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
                System.out.println("Received request: " + clientResponse);

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
                        case EDITACCOUNT:
                            EditProfileInfo();
                            break;
                        case LOGOUT:
                            logout();
                            break;
                        case TOBLOCK:
                            searchToBlock();
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


                        System.out.println(u.getName());
                        foundUsers.add(u);





                }
            }

            response.writeObject(new Response("server",foundUsers,200));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void searchToBlock() {
        try {
            Data body = (Data) clientResponse.getBody();
            Optional<User> mUser = User.find(body.phone);
            Optional<User> mtUser = User.find(body.receiverPhone);

            if (mtUser.isEmpty()) {
                response.writeObject(new Response(500));
                return;
            }

            if (mUser.isPresent()) {
                User user = mUser.get();
                if (!user.isBlocked(body.receiverPhone)) {
                    response.writeObject(new Response("Server", user, 200));
                } else {
                    response.writeObject(new Response(500));
                }
            } else {
                response.writeObject(new Response(500));
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.writeObject(new Response(500));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }



    private void messageSend() {
        try {
            Message msg = (Message) clientResponse.getBody();

            // checking if user is blocked or not

            Optional<User> mUser = User.find(msg.getReceiver());
            if (mUser.isPresent()) {
                User user = mUser.get();
                if (user.isBlocked(msg.getSender())) {
                    // receiver blocked user , nothing to send or save
                    response.writeObject(new Response(500));
                    return;
                }
            }
            // if its the starting of the chat
            Chat chat=null;
            if(msg.isFirstMsg())
            {
               chat= Chat.createChat(msg.getReceiver(),msg.getSender());
                msg.setChatId(chat.getChatId());
            }


            Socket receiverSocket = clientMap.get(msg.getReceiver());
            ObjectOutputStream receiverStream = new ObjectOutputStream(receiverSocket.getOutputStream());

            if(!msg.isFirstMsg())
            {

                chat = Chat.findChat(msg.getChatId());
            }

            chat.addMessage(msg);


            // receiver is online
            if (receiverSocket != null) {
                if (msg.isFirstMsg()) {
                    // sending full chat file if its the start of conversation
                    receiverStream.writeObject(chat);
                } else {
                    receiverStream.writeObject(msg);
                }
            }


        } catch (Exception e) {

        }


    }

    private void login() {
        try {
            Data body = (Data) clientResponse.getBody();

            Optional<User> mUser = User.find(body.phone);

            if (mUser.isEmpty()) {
                response.writeObject(new Response(500));
                return;

            }
            User user = mUser.get();

            if (user.getPassword().equals(body.password)) {
                clientMap.put(user.getPhone(), socket);
                response.writeObject(new Response("server",user,200));
            } else {
                response.writeObject(new Response(500));
            }


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void logout() {
        try {
            Data body = (Data) clientResponse.getBody();
            Optional<User> mUser = User.find(body.phone);
            if (mUser.isPresent()) {
                User user = mUser.get();
                clientMap.remove(user.getPhone());
                sendResponse(user.getName(), 200);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void EditProfileInfo() {
        try {
            Data body = (Data) clientResponse.getBody();
            Optional<User> mUser = User.find(body.oldPhone);
            if (!mUser.isPresent()) {
                response.writeObject(new Response(500));
                return;
            }
            User user = mUser.get();
            if (body.phone != null) {
                user.setPhone(body.phone);
            }
            if (body.name != null) {
                user.setName(body.name);
            }
            if (body.password != null) {
                user.setPassword(body.password);
            }
            if (body.url != null) {
                user.setUrl(body.url);
            }


            User.saveAllToFile();  // <-- Add this line to commit changes

            // --- Send the updated user back to the client ---
           Socket receiverSocket = clientMap.get(body.oldPhone);
            if (receiverSocket != null) {
                clientMap.remove(body.oldPhone);
                clientMap.put(user.getPhone(), receiverSocket);
            } else {
                System.out.println("Receiver socket not found for phone: " + body.oldPhone);
            }
            response.writeObject(new Response("server", user, 200));
        } catch (IOException e) {
            e.printStackTrace();
            // You might also want to notify the client of the failure:
            try {
                response.writeObject(new Response(500));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }


    private void createUser() {

        try {
            User newUser = (User) clientResponse.getBody();
            Optional<User> mUser = User.find(newUser.getPhone());
            if (mUser.isPresent()) {
                response.writeObject(new Response(500));
                return;
            }
            User.addUser(newUser);
            response.writeObject(new Response("server",newUser,200));
            clientMap.put(newUser.getPhone(), socket);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void chatFetch() {
        try {

            Data body = (Data) clientResponse.getBody();

            Chat chat = Chat.findChat(body.chatId);

            response.writeObject(new Response("server", chat, 200));


        } catch (Exception e) {
            try {
                response.writeObject(new Response(500));
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
                response.writeObject(new Response("server", user, 200));
            }else{
                response.writeObject(new Response(500));
            }


        } catch (Exception e) {
            try {
                response.writeObject(new Response(500));
            } catch (IOException ex) {
                e.printStackTrace();
            }
        }
    }
    private void unBlock() {
        try {

            Data body = (Data) clientResponse.getBody();

            Optional<User> mUser = User.find(body.phone);
            if (mUser.isPresent()) {
                User user = mUser.get();
                    user.unblock(body.receiverPhone);
                    response.writeObject(new Response("server", user, 200));

            }else{
                response.writeObject(new Response(500));
            }


        } catch (Exception e) {
            try {
                response.writeObject(new Response(500));
            } catch (IOException ex) {
                e.printStackTrace();
            }
        }
    }


}

