package com.example.xemphim.model;

public class ThongBao {

    private String title;
    private String time;
    private String content;

    // Constructor
    public ThongBao(String title, String time, String content) {
        this.title = title;
        this.time = time;
        this.content = content;
    }

    // Getter và setter
    public String getTitle() {
        return title;
    }

    public String getTime() {
        return time;
    }

    public String getContent() {
        return content;
    }
}

