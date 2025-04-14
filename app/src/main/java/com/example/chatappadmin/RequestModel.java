package com.example.chatappadmin;

import java.util.List;

public class RequestModel {
    private String imageUrl;
    private String messageType;
    private String receiverId;
    private String senderId;
    private List<String> users;
    private String timestamp;
    private String userName;

    // Empty constructor required for Firestore
    public RequestModel() {
    }

    public RequestModel(String imageUrl, String messageType, String receiverId, String senderId, List<String> users, String timestamp,String userName) {
        this.imageUrl = imageUrl;
        this.messageType = messageType;
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.users = users;
        this.timestamp = timestamp;
    }

    public RequestModel(String username, String time) {
        this.userName = username;
        this.timestamp = time;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageTyoe(String messageTyoe) {
        this.messageType = messageTyoe;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public long getTimestamp() {
        return Long.parseLong(timestamp);
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
