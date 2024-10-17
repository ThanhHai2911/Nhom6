package com.example.xemphim.model;

public class FavoriteMovie {
    private String id; // ID của phim
    private String title; // Tiêu đề phim
    private String link; // Link phim
    private String slug; // Slug phim

    public FavoriteMovie() {
        // Cần một constructor rỗng cho Firebase
    }

    public FavoriteMovie(String id, String title, String link, String slug) {
        this.id = id;
        this.title = title;
        this.link = link;
        this.slug = slug;
    }

    // Getters và Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
}
