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
                String type = request.readLine();

                switch (type) {
                    case "MSG":
                        messageSend();
                        break;
                    case "LOGIN":
                        login();
                        break;
                    case "CREATE":
                        createUser();
                        break;


                }


            }


        } catch (IOException e) {
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

            try {
                User user = User.Find(phone);
                if (user.getPassword() == password) {
                    response.println("login successfull");
                    return;
                } else {
                    response.println("Wrong password");
                }
            } catch (Exception e) {
                response.println("User Does Not Exist");
            }


        } catch (IOException e) {
            System.out.println("Login Failed");
        }

    }

    private void createUser() {

        try {
            String name = request.readLine();
            String phone = request.readLine();
            String password = request.readLine();
            String url = request.readLine();

            User newUser = new User(name, url, phone, password);

            User.Add(newUser);

            response.println("User created");
        } catch (IOException e) {
            response.println("User creation failed");
        }

    }


}