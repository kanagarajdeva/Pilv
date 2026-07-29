package com.pilv.chat;

public class ChatMessage {
    private String text;
    private String senderId;
    private String senderName;
    private String senderLocation;
    private long timestamp;

    public ChatMessage() {
        // Empty constructor for Firestore
    }

    public ChatMessage(String text, String senderId, String senderName, String senderLocation) {
        this.text = text;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderLocation = senderLocation;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters
    public String getText() { return text; }
    public String getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getSenderLocation() { return senderLocation; }
    public long getTimestamp() { return timestamp; }

    // Setters
    public void setText(String text) { this.text = text; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public void setSenderLocation(String senderLocation) { this.senderLocation = senderLocation; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}