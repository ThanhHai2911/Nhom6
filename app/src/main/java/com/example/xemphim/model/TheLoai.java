package com.example.xemphim.model;
public class TheLoai {
    private String id;   // Đổi Long thành String
    private String name; // Vẫn giữ String cho name

    // Constructor
    public TheLoai() { }

    public TheLoai(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getter and setter methods
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
