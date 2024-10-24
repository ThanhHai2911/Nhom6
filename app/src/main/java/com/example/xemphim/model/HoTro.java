package com.example.xemphim.model;

public class HoTro {
    private String name;
    private String description;
    private String imageUrl;

    public HoTro() {
    }

    public HoTro(String name, String description, String imageUrl) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
