package com;

//import com.controller.ClientHandlerNew;
import com.controller.ClientHandler;
import com.db.Chat;
import com.db.User;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server{
    public static void main(String[] args) {
        // loading all user data from file
        User.loadFromFile();
        Chat.loadAllChats();
        HashMap<String,Socket> clientMap = new HashMap<>();
        System.out.println(User.getAllUsers().get(0));
        ExecutorService executorService = Executors.newCachedThreadPool();
        int clientNum = 1;
        while(true){


        try(ServerSocket serverSocket = new ServerSocket(5000)){
            System.out.println("waiting for client ...");
            Socket socket  = serverSocket.accept(); // blocking call
            System.out.println("Server accepts "+(clientNum++)+" client connection");

            ClientHandler clientHandler = new ClientHandler(socket,clientMap);
            executorService.submit(clientHandler::router);
        }



        catch (IOException e){
            System.out.println("Client not listening");
        }
        }
    }
}
