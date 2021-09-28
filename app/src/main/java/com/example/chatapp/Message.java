package com.example.chatapp;

public class Message {
    private String author;
    private String textOfMessage;
    private long createdTime;

    public Message(String author, String textOfMessage, long createdTime) {
        this.author = author;
        this.textOfMessage = textOfMessage;
        this.createdTime = createdTime;
    }

    public Message() {
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTextOfMessage() {
        return textOfMessage;
    }

    public void setTextOfMessage(String textOfMessage) {
        this.textOfMessage = textOfMessage;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }
}
