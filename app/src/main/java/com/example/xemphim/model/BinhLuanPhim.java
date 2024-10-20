package com.example.xemphim.model;

public class BinhLuanPhim {
    public String userId;
    public String slug;
    public String commentText;
    public long timestamp;
    private String userName; // Thêm trường tên người dùng
    private String formattedDate; // Thêm trường ngày giờ định dạng

    // Constructor không đối số và có đối số
    public BinhLuanPhim() {}

    // Constructor
    public BinhLuanPhim(String userId, String movieSlug, String commentText, long timestamp, String userName, String formattedDate) {
        this.userId = userId;
        this.slug = movieSlug;
        this.commentText = commentText;
        this.timestamp = timestamp;
        this.userName = userName;
        this.formattedDate = formattedDate;
    }
    // Các getter và setter cho các trường dữ liệu
    public String getUserName() {
        return userName;
    }

    public String getFormattedDate() {
        return formattedDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String userName) {
        this.slug = userName;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
