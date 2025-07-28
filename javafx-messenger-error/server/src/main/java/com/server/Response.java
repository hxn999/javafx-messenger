package com.server;

import com.api.Path;
import com.db.Chat;

import java.io.Serializable;

public class Response implements Serializable {
    private String sender;
    private Object body;
    private int statusCode;
    private Path path;
    private String requestId;
    Boolean isMessage = false;
    Chat chat = null;

    public Response(String sender, Object body, int statusCode, String requestId) {
        this.sender = sender;
        this.body = body;
        this.statusCode = statusCode;
        this.requestId = requestId;
    }

    public Response(String sender, Chat chat, int statusCode, String requestId) {
        this.sender = sender;
        this.chat = chat;
        this.statusCode = statusCode;
        this.requestId = requestId;
    }
    public Response(String sender, Chat chat, int statusCode, String requestId, boolean isMessage) {
        this.sender = sender;
        this.chat = chat;
        this.statusCode = statusCode;
        this.requestId = requestId;
        this.isMessage = isMessage;
    }
    public Response(String sender, Object body, Path path, String requestId) {
        this.sender = sender;
        this.body = body;
        this.path = path;
        this.requestId = requestId;
    }
    public Response(int statusCode) {
        this.statusCode = statusCode;
        this.sender ="server";
    }
    public Response(int statusCode, String requestId) {
        this.statusCode = statusCode;
        this.sender ="server";
        this.requestId = requestId;
    }
    public Response(String msg,int statusCode) {
        this.body = msg;
        this.sender ="server";
    }

    public String getRequestId() {
        return this.requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setIsMessage(Boolean isMessage) {
        this.isMessage = isMessage;
    }

    public boolean isIsMessage() {
        return isMessage;
    }

    public Chat getChat() {
        return chat;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
