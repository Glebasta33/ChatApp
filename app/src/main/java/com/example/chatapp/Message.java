package com.example.chatapp;

public class Message {
    private String author;
    private String textOfMessage;
    private long createdTime;
    private String imageUrl;

    public Message(String author, String textOfMessage, long createdTime, String imageUrl) {
        this.author = author;
        this.textOfMessage = textOfMessage;
        this.createdTime = createdTime;
        this.imageUrl = imageUrl;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
