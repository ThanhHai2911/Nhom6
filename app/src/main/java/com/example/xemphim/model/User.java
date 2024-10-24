package com.example.xemphim.model;

public class User {
    private String id_user;
    private String name;
    private String status;
    private boolean isNormal;
    private boolean isVIP;
    private String goi;
    private boolean isChecked; // Thêm thuộc tính isChecked

    public User() {
    }

    public User(String id_user, String name, String status, String goi) {
        this.id_user = id_user;
        this.name = name;
        this.status = status;
        this.goi = goi;
    }

    public String getGoi() {
        return goi;
    }

    public void setGoi(String goi) {
        this.goi = goi;
    }

    public User(String id_user, String name, String status, boolean isNormal, boolean isVIP) {
        this.id_user = id_user;
        this.name = name;
        this.status = status;
        this.isNormal = isNormal;
        this.isVIP = isVIP;
    }

    public String getId_user() {
        return id_user;
    }

    public void setId_user(String id_user) {
        this.id_user = id_user;
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
    public boolean isChecked() {
        return isChecked;
    }
    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }
}

