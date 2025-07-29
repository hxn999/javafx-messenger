package com.db;

import java.io.Serializable;

public class Message implements Serializable {
    private String sender;
    private String receiver;
    private long timestamp;
    private String message;
    private int chatId;
    private boolean firstMsg;

    public Message(String sender, String receiver, long timestamp, String message) {
        this.sender = sender;
        this.receiver = receiver;
        this.timestamp = timestamp;
        this.message = message;
        this.firstMsg = false;
    }

    // Getters and setters
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public boolean isFirstMsg() {
        return firstMsg;
    }

    public void setFirstMsg(boolean firstMsg) {
        this.firstMsg = firstMsg;
    }

    // Add toString for debugging
    @Override
    public String toString() {
        return "Message{" +
                "sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", timestamp=" + timestamp +
                ", message='" + message + '\'' +
                ", chatId=" + chatId +
                ", firstMsg=" + firstMsg +
                '}';
    }

    // Add validation method
    public boolean isValid() {
        return sender != null && !sender.trim().isEmpty() &&
                receiver != null && !receiver.trim().isEmpty() &&
                message != null && !message.trim().isEmpty() &&
                timestamp > 0;
    }
}