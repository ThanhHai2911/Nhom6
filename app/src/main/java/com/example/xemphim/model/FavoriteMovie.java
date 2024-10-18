package com.example.xemphim.model;

public class FavoriteMovie {
    private String id_user; // ID của phim
    private String slug; // Slug phim

    public FavoriteMovie() {
        // Cần một constructor rỗng cho Firebase
    }

    public FavoriteMovie(String id_user, String slug) {
        this.id_user = id_user;
        this.slug = slug;
    }

    // Getters và setters

    public String getId_user() {
        return id_user;
    }

    public void setId_user(String id_user) {
        this.id_user = id_user;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
}
