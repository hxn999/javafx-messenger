package com;

import com.controller.ClientHandler;
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
        User.Load();


        List<Socket> clients = new ArrayList<>();
        HashMap<String,Integer> clientMap = new HashMap<>();
        ExecutorService executorService = Executors.newCachedThreadPool();

        try(ServerSocket serverSocket = new ServerSocket(5000)){

            Socket socket  = serverSocket.accept(); // blocking call
            System.out.println("Server accepts client connection");
            clients.add(socket);
            ClientHandler clientHandler = new ClientHandler(socket,clients,clientMap);
            executorService.submit(()->{
                clientHandler.router();
            });



        }catch (IOException e){
            System.out.println("Client not listening");
        }
    }
}