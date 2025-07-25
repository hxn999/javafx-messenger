//package com.api;
//
//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.net.Socket;
//import com.server.Response;
//
//public class Receiver extends Thread {
//
//    private Socket socket;
//    private String type;
//    private ObjectInputStream receive;
//    private Response data;
//    public static Receiver receiver;
//
//    public Receiver(String name, Socket socket) {
//        super(name);
//        this.socket = socket;
//        try {
//            this.receive = new ObjectInputStream(socket.getInputStream());
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        this.receiver = this;
//    }
//
//    private void receive() {
//        synchronized (this) {
//            while (true) {
//                System.out.println("waiting for data to send");
//                System.out.println(Thread.currentThread().getState());
//                try {
//                    // Read Response object from server
//                    data = (Response) receive.readObject();
//
//                    if (data != null) {
//                        System.out.println("Received response: " + data);
//                    }
//                    System.out.println("not waiting for data to send");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (ClassNotFoundException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
////
////    public synchronized void sendMessage(String receiver, String message) {
////        String type="MSG";
////        this.request = "MSG\n" +
////                receiver+"\n" +
////                message+"\n";
////        notify();
////
////    }
////    public synchronized void login(String phone, String password) {
////        String type="LOGIN";
////        request = "LOGIN\n" +
////                phone+"\n" +
////                password+"\n";
////        notify();
////
////    }
//
//    @Override
//    public void run() {
//        receive();
//    }
//}
