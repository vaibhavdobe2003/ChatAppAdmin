package com.example.chatappadmin;

public class RequestModel {
    private String userName;
    private String timestamp;
    private String imageUrl;
    private String documentId; // ✅ Add this field

    public RequestModel() {} // required by Firestore

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public long getTimestamp() { return Long.parseLong(timestamp); }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
