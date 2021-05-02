package com.gmail.rohan1007aggarwal.chatapp_rohan;

public class Contacts {

    public Contacts(){

    }

    public String username, status, imageUrl;

    public Contacts(String username, String status, String imageUrl) {
        this.username = username;
        this.status = status;
        this.imageUrl = imageUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
