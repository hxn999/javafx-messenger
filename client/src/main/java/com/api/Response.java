package com.api;

public class Response {
    public int statusCode;
    public String body;

    public Response(String status) {
        statusCode=Integer.parseInt(status);
        body = "null";
    }

    @Override
    public String toString() {
        return "statusCode: " + statusCode + ", body: " + body;
    }
}
