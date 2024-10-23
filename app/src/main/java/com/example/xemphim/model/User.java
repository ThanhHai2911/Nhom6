package com.example.xemphim.model;

public class User {
    private String name;
    private String status;
    private boolean isNormal;
    private boolean isVIP;

    public User(String name, String status, boolean isNormal, boolean isVIP) {
        this.name = name;
        this.status = status;
        this.isNormal = isNormal;
        this.isVIP = isVIP;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public boolean isNormal() {
        return isNormal;
    }

    public boolean isVIP() {
        return isVIP;
    }
}

