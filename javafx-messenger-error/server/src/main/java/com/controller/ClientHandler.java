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






//    // New method to handle real-time messaging safely
//    private void sendRealtimeMessage(Message msg, Chat chat) {
//        Socket receiverSocket = clientMap.get(msg.getReceiver());
//
//        if (receiverSocket != null && !receiverSocket.isClosed()) {
//            try {
//                // Get the receiver's ClientHandler and send through their response stream
//                ClientHandler receiverHandler = getClientHandler(receiverSocket);
//                if (receiverHandler != null) {
//                    if (msg.isFirstMsg()) {
//                        receiverHandler.sendToClient(chat);
//                    } else {
//                        receiverHandler.sendToClient(msg);
//                    }
//                    System.out.println("Real-time message sent to receiver: " + msg.getReceiver());
//                } else {
//                    System.out.println("Receiver handler not found: " + msg.getReceiver());
//                }
//            } catch (Exception e) {
//                System.err.println("Failed to send real-time message to receiver: " + msg.getReceiver());
//                e.printStackTrace();
//                // Remove the socket from map if it's corrupted
//                clientMap.remove(msg.getReceiver());
//            }
//        } else {
//            System.out.println("Receiver not online: " + msg.getReceiver());
//        }
//    }

    // Method to send object to this client


    // You'll need to maintain a map of socket to ClientHandler
    private static final Map<Socket, ClientHandler> handlerMap = new HashMap<>();

    // Add this to your constructor
    public ClientHandler(Socket socket, HashMap<String, Socket> clientMap) {
        this.socket = socket;
        this.clientMap = clientMap;

        try {
            // Critical: Create and flush ObjectOutputStream first
            this.response = new ObjectOutputStream(socket.getOutputStream());
            this.response.flush();

            // Then create ObjectInputStream
            this.request = new ObjectInputStream(socket.getInputStream());

            // Register this handler
            handlerMap.put(socket, this);

            System.out.println("Client handler initialized successfully");
        } catch (IOException e) {
            System.err.println("Failed to initialize streams: " + e.getMessage());
            e.printStackTrace();
            try {
                if (socket != null) socket.close();
            } catch (IOException closeEx) {
                closeEx.printStackTrace();
            }
        }
    }

    private ClientHandler getClientHandler(Socket socket) {
        return handlerMap.get(socket);
    }

    // Update cleanup method to remove from handler map
    private void cleanup() {
        try {
            // Remove from handler map
            handlerMap.remove(socket);

            // Remove client from map if they were logged in
            String userPhone = null;
            for (Map.Entry<String, Socket> entry : clientMap.entrySet()) {
                if (entry.getValue() == socket) {
                    userPhone = entry.getKey();
                    break;
                }
            }
            if (userPhone != null) {
                clientMap.remove(userPhone);
                System.out.println("User " + userPhone + " disconnected");
            }

            // Close streams and socket
            if (request != null) request.close();
            if (response != null) response.close();
            if (socket != null && !socket.isClosed()) socket.close();

        } catch (IOException e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }

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

//    public ClientHandler(Socket socket, HashMap<String, Socket> clientMap) {
//        this.socket = socket;
//        this.clientMap = clientMap;
//
//        // setting up streams for send and receive
//        try {
//            this.response = new ObjectOutputStream(socket.getOutputStream());
//            this.response.flush();
//            this.request = new ObjectInputStream(socket.getInputStream());
//        } catch (IOException e) {
//            System.out.println("Stream Error !");
//            e.printStackTrace();
//        }
//    }

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
            System.out.println("Client disconnected: " + e.getMessage());
            cleanup();
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found!");
            e.printStackTrace();
            cleanup();
        } finally {
            cleanup();
        }
    }


    /**
     * Sends a Response object to this client
     * This method is synchronized to prevent concurrent writes to the same stream
     */
    public synchronized void sendToClient(Response responseObj) throws IOException {
        if (response != null && responseObj != null) {
            try {
                response.writeObject(responseObj);
                response.flush();

                // Log what type of response we're sending
                if (responseObj.isIsMessage()) {
                    System.out.println("📤 Real-time message sent to client - RequestID: " + responseObj.getRequestId());
                } else {
                    System.out.println("📤 RPC response sent to client - RequestID: " + responseObj.getRequestId());
                }

            } catch (IOException e) {
                System.err.println("❌ Failed to send response to client: " + e.getMessage());
                // Mark this connection as problematic
                throw e; // Re-throw so caller can handle cleanup
            }
        } else {
            if (response == null) {
                System.err.println("❌ Cannot send - ObjectOutputStream is null");
            }
            if (responseObj == null) {
                System.err.println("❌ Cannot send - Response object is null");
            }
        }
    }


    /**
     * Legacy method for backward compatibility - converts Object to Response
     * @deprecated Use sendToClient(Response) instead
     */
    @Deprecated
    public synchronized void sendToClient(Object obj) throws IOException {
        if (obj instanceof Response) {
            sendToClient((Response) obj);
        } else {
            System.err.println("⚠️ Warning: sendToClient called with non-Response object: " +
                    (obj != null ? obj.getClass().getSimpleName() : "null"));

            if (response != null && obj != null) {
                response.writeObject(obj);
                response.flush();
            }
        }
    }

    // Overloaded version for sending objects
    private void sendResponse(Object body, int statusCode) {
        try {
            Response serverResponse = new Response("server", body, statusCode, clientResponse.getRequestId());
            sendToClient(serverResponse);
        } catch (IOException e) {
            System.err.println("❌ Failed to send response: " + e.getMessage());
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
            System.out.println("📨 Message send request received: " + clientResponse.getRequestId());
            Message msg = (Message) clientResponse.getBody();

            if (msg == null) {
                System.err.println("❌ Received null message");
                response.writeObject(new Response(500, clientResponse.getRequestId()));
                return;
            }

            System.out.println("📝 Processing message: '" + msg.getMessage() + "' from " +
                    msg.getSender() + " to " + msg.getReceiver());

            // checking if user is blocked or not
            Optional<User> mUser = User.find(msg.getReceiver());
            if (mUser.isPresent()) {
                User user = mUser.get();
                if (user.isBlocked(msg.getSender())) {
                    System.out.println("🚫 Message blocked - receiver has blocked sender");
                    response.writeObject(new Response(500, clientResponse.getRequestId()));
                    return;
                }
            }

            var ref = new Object() {
                Chat chat = null;
                boolean isNewChat = false;
            };

            // if its the starting of the chat
            if (msg.isFirstMsg()) {
                System.out.println("🆕 Creating new chat - first message");
                ref.chat = Chat.createChat(msg.getSender(), msg.getReceiver());
                ref.isNewChat = true;

                // Add chat to both users' chat lists
                User.find(msg.getReceiver()).ifPresent(receiver -> {
                    if (!receiver.getChatList().contains(ref.chat.getChatId())) {
                        receiver.getChatList().add(ref.chat.getChatId());
                    }
                });
                User.find(msg.getSender()).ifPresent(sender -> {
                    if (!sender.getChatList().contains(ref.chat.getChatId())) {
                        sender.getChatList().add(ref.chat.getChatId());
                    }
                });
                User.saveAllToFile();

                // Set the chat ID for the message
                msg.setChatId(ref.chat.getChatId());
                System.out.println("✅ New chat created with ID: " + ref.chat.getChatId());
            } else {
                System.out.println("📬 Adding to existing chat ID: " + msg.getChatId());
                ref.chat = Chat.findChat(msg.getChatId());
                ref.isNewChat = false;
                if (ref.chat == null) {
                    System.err.println("❌ Chat not found for ID: " + msg.getChatId());
                    response.writeObject(new Response(500, clientResponse.getRequestId()));
                    return;
                }
            }

            // Add message to chat
            ref.chat.addMessage(msg);
            System.out.println("📝 Message added to chat. Total messages: " + ref.chat.getMessages().size());

            // Send response back to sender (this is the RPC response)
            Response senderResponse = new Response("server", ref.chat, 200, clientResponse.getRequestId(), false);
            response.writeObject(senderResponse);
            System.out.println("📤 Response sent to sender");

            // Send real-time message to receiver with appropriate data
            sendRealtimeMessage(msg, ref.chat, ref.isNewChat);

        } catch (Exception e) {
            System.err.println("❌ Error in messageSend: " + e.getMessage());
            e.printStackTrace();
            try {
                response.writeObject(new Response(500, clientResponse.getRequestId()));
            } catch (IOException ioException) {
                System.err.println("❌ Failed to send error response: " + ioException.getMessage());
            }
        }
    }

    // Modified method to send appropriate data based on whether it's a new chat or existing chat
    private void sendRealtimeMessage(Message msg, Chat chat, boolean isNewChat) {
        Socket receiverSocket = clientMap.get(msg.getReceiver());

        if (receiverSocket != null && !receiverSocket.isClosed()) {
            ClientHandler receiverHandler = handlerMap.get(receiverSocket);
            if (receiverHandler != null) {
                try {
                    System.out.println("📤 Preparing real-time message for receiver: " + msg.getReceiver());
                    System.out.println("📝 Message content: " + msg.getMessage());

                    Response realtimeResponse;

                    if (isNewChat) {
                        // For new chat, send the entire chat object
                        System.out.println("💬 Sending new chat with ID: " + chat.getChatId());
                        realtimeResponse = new Response("server", chat, 200,
                                UUID.randomUUID().toString(), true);
                    } else {
                        // For existing chat, send only the message
                        System.out.println("📝 Sending message to existing chat: " + chat.getChatId());
                        realtimeResponse = new Response("server", msg, 200,
                                UUID.randomUUID().toString(), true);
                    }

                    // Send to receiver
                    receiverHandler.sendToClient(realtimeResponse);
                    System.out.println("✅ Real-time message sent successfully to: " + msg.getReceiver());

                } catch (IOException e) {
                    System.err.println("❌ Failed to send real-time message to receiver: " + msg.getReceiver());
                    System.err.println("❌ Error details: " + e.getMessage());
                    e.printStackTrace();

                    // Remove the corrupted socket from maps
                    clientMap.remove(msg.getReceiver());
                    handlerMap.remove(receiverSocket);

                    System.out.println("🧹 Cleaned up corrupted connection for: " + msg.getReceiver());
                }
            } else {
                System.out.println("⚠️ Receiver handler not found for: " + msg.getReceiver());
                System.out.println("📊 Available handlers: " + handlerMap.size());
            }
        } else {
            if (receiverSocket == null) {
                System.out.println("📴 Receiver not connected: " + msg.getReceiver());
            } else {
                System.out.println("🔌 Receiver socket closed: " + msg.getReceiver());
                // Clean up closed socket
                clientMap.remove(msg.getReceiver());
            }
            System.out.println("👥 Online users: " + clientMap.keySet());
        }
    }

    // Cache ObjectOutputStream per socket to avoid stream corruption
    public synchronized void sendToReceiver(Object obj) {
        try {
            response.writeObject(obj);
            response.flush();
        } catch (IOException e) {
            System.err.println("Failed to send to receiver: " + e.getMessage());
            e.printStackTrace();
        }
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
