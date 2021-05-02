package com.gmail.rohan1007aggarwal.chatapp_rohan;

public class Messages {
    private String time,date, to, messageId, message, type, from;

    public Messages(){

    }

    public Messages(String time, String date, String to, String messageId, String message, String type, String from) {
        this.time = time;
        this.date = date;
        this.to = to;
        this.messageId = messageId;
        this.message = message;
        this.type = type;
        this.from = from;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
