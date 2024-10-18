package com.example.xemphim.model;

public class Phim {

    private String id_movie;
    private String title;
    private String description;
    private String release_date;
    private String duration;
    private float rating;
    private String poster_url;
    private String url_phim;
    private int year;

    public Phim() {
        // Firebase yêu cầu constructor mặc định
    }

    // Các constructor khác nếu cần
    public Phim(String id_movie, String title, String description, String release_date, String duration, float rating, String poster_url, String url_phim, int year) {
        this.id_movie = id_movie;
        this.title = title;
        this.description = description;
        this.release_date = release_date;
        this.duration = duration;
        this.rating = rating;
        this.poster_url = poster_url;
        this.url_phim = url_phim;
        this.year = year;
    }

    // Các getter và setter cho các trường
    public String getId_movie() {
        return id_movie;
    }

    public void setId_movie(String id_movie) {
        this.id_movie = id_movie;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRelease_date() {
        return release_date;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getPoster_url() {
        return poster_url;
    }

    public void setPoster_url(String poster_url) {
        this.poster_url = poster_url;
    }

    public String getUrl_phim() {
        return url_phim;
    }

    public void setUrl_phim(String url_phim) {
        this.url_phim = url_phim;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
