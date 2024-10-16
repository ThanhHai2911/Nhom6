package com.example.xemphim.model;

import java.util.List;

public class Movie {
    private String _id;
    private String name;
    private String slug;
    private String origin_name;
    private String poster_url;
    private String thumb_url;
    private int year;
    private String content;
    private List<String> actor;
    private List<String> director;

    public Movie() {
    }

    private Modified modified;

    public String getSlug() {
        return slug;
    }

    public String getContent() {
        return content;
    }

    public List<String> getDirector() {
        return director;
    }

    public List<String> getActor() {
        return actor;
    }

    public String getThumb_url() {
        return thumb_url;
    }

    public static class Modified {
        private String time;

        // Getter and Setter
        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }

    // Getters and Setters for Movie
    public String getId() {
        return _id;
    }

    public void setId(String _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPoster_url() {
        return poster_url;
    }

    public void setPoster_url(String poster_url) {
        this.poster_url = poster_url;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}

