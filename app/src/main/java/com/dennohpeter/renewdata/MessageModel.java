package com.dennohpeter.renewdata;

public class MessageModel {
    private String date;
    private String message;
    private String sender;

    MessageModel(String date, String message, String sender) {
        this.date = date;
        this.message = message;
        this.sender = sender;
    }

    String getDate() {
        return date;
    }

    String getMessage() {
        return message;
    }

    public String getSender() {
        return sender;
    }
}
