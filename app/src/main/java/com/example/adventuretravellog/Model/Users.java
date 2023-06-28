package com.example.adventuretravellog.Model;

public class Users {
    private String username;
    private String email;
    private String mobile;

    public Users() {
        // Default constructor required for Firebase database
    }

    public Users(String username, String email, String mobile) {
        this.username = username;
        this.email = email;
        this.mobile = mobile;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
