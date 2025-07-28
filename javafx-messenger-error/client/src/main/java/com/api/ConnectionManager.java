package com.api;

import java.io.*;
import java.net.Socket;

public class ConnectionManager {
    private String host;
    private int port;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ConnectionManager(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public boolean connect() {
        try {
            System.out.println("Connecting to " + host + ":" + port);
            this.socket = new Socket(host, port);

            // ✅ Correct order: output first
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.out.flush(); // important: flush header
            System.out.println("ObjectOutputStream initialized");

            this.in = new ObjectInputStream(socket.getInputStream());
            System.out.println("ObjectInputStream initialized");

            return true;
        } catch (IOException e) {
            System.err.println("Connection failed: " + e.getMessage());
            return false;
        }
    }

    public boolean reconnect() {
        disconnect();
        return connect();
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public void disconnect() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    public void writeObject(Object obj) throws IOException {
        if (out == null) {
            throw new IOException("Output stream not initialized");
        }
        out.writeObject(obj);
        out.flush();
    }

    public Object readObject() throws IOException, ClassNotFoundException {
        if (in == null) {
            throw new IOException("Input stream not initialized");
        }
        return in.readObject();
    }

    public Socket getSocket() {
        return socket;
    }
}
