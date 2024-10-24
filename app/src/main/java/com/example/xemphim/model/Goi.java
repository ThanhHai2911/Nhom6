package com.example.xemphim.model;

public class Goi {
    private int id;
    private String type;

    public Goi(int id, String type) {
        this.id = id;
        this.type = type;
    }

    public int getId() {
        return id;
    }
    // Constructor mặc định (bắt buộc phải có)
    public Goi() {
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
