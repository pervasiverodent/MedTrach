package com.java.medtrach.model;

public class UserModel {
    private String uid, username, password;

    public UserModel() {
    }

    public UserModel(String uid, String username, String password) {
        this.uid = uid;
        this.username = username;
        this.password = password;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
