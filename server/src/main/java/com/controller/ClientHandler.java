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
//        synchronized (this) {
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
                            case "SEARCHABLE":
                                searchUserToBlock();
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
//        }
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



    private void searchUserToBlock() {
        try {
            // Who’s doing the searching
            String searcherPhone = request.readLine();
            // Whom to look up
            String targetPhone   = request.readLine();

            // Find the target user in the list
            User target = null;
            for (User u : User.getUsers()) {
                if (u.getPhone().equals(targetPhone)) {
                    target = u;
                    break;
                }
            }

            if (target == null || target.getPhone().equals(searcherPhone)) {
                // 404 = target not in DB
                response.println("404");
            } else {
                // Check if the target has already blocked the searcher
                boolean hasBlocked = false;
                User currentUser = User.Find(searcherPhone);
                for (User blockedUser : currentUser.getBlocklist()) {
                    if (blockedUser.getPhone().equals(targetPhone)) {
                        hasBlocked = true;
                        break;
                    }
                }

                if (hasBlocked) {
                    response.println("403"); //already blocked
                } else {
                    // 200 = OK; send back the target’s public data
                    response.println("200");
                    response.println(target.publicToString());
                }
            }

//            response.flush();

        } catch (IOException e) {
            response.println("500");
//            response.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void messageSend() {
        try {
            String phone = request.readLine();
            Socket receiverSocket = socket;
            int hashCode = clientMap.get(phone); // getting receiver socket hashcode
            // searching receiver socket
            for (Socket soc : clients) {
                if (soc.hashCode() == hashCode) {
                    receiverSocket = soc;
                    break;
                }
            }
            int chatId = Integer.parseInt(request.readLine());
            Chat chat = new Chat(chatId);

            // receiver isnt online so socket not found
            if (receiverSocket.hashCode() == socket.hashCode()) {
                chat.add(request.readLine());

            }
            // receiver is online
            else {


                String msg = request.readLine();
                chat.add(msg);
                // sending to the receiver
                PrintWriter receiverResponse = new PrintWriter(receiverSocket.getOutputStream(), true);
                receiverResponse.println(msg);

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


// TODO : if wanna implement later

//    private void unblockUser() {
//        try {
//            // 1) Who’s doing the unblocking?
//            String searcherPhone = request.readLine();
//            // 2) Who they want to unblock
//            String targetPhone   = request.readLine();
//
//            // 3) Lookup
//            User target = null;
//              User currentUser = User.Find(searcherPhone);
//              for (User blockedUser : currentUser.getBlocklist()) {
//                    if (blockedUser.getPhone().equals(targetPhone)) {
//                        target = blockedUser;
//                        break;
//                    }
//                }
//
//            if (target == null) {
//                // No such user
//                response.println("404");
//            }
//            else {
//                // Try to remove the blocker entry
//               boolean removed = false;
//              for (User b : currentUser.getBlocklist()) {
//                    if (b.getPhone().equals(targetPhone)) {
//                        currentUser.getBlocklist().remove(b);
//                         removed = true;
//                          break;
//                         }
//                 }
//                if (removed) {
//                    // Success
//                    response.println("200");
//                } else {
//                    // They weren’t blocked to begin with
//                    response.println("409");
//                }
//            }
//            response.flush();
//
//        } catch (IOException e) {
//            // Unexpected I/O problem
//            response.println("500");
//            response.flush();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }



}