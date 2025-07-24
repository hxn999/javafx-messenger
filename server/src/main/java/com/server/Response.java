package com.server;

import com.api.Path;

import java.io.Serializable;

public class Response implements Serializable {
    private String sender;
    private Object body;
    private int statusCode;
    private Path path;
    public Response(String sender, Object body, int statusCode) {
        this.sender = sender;
        this.body = body;
        this.statusCode = statusCode;
    }
    public Response(String sender, Object body, Path path) {
        this.sender = sender;
        this.body = body;
        this.path = path;
    }
    public Response(int statusCode) {
        this.statusCode = statusCode;
        this.sender ="server";
    }
    public Response(String msg,int statusCode) {
        this.body = msg;
        this.sender ="server";
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

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
