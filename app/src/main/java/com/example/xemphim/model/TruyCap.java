package com.example.xemphim.model;

public class TruyCap {
    private String id_user;
    private long thoigiantruycap;

    public TruyCap() {
        // Constructor rỗng cần thiết cho Firebase
    }

    public TruyCap(String id_user, long thoigiantruycap) {
        this.id_user = id_user;
        this.thoigiantruycap = thoigiantruycap;
    }

    public String getId_user() {
        return id_user;
    }

    public void setId_user(String id_user) {
        this.id_user = id_user;
    }

    public long getThoigiantruycap() {
        return thoigiantruycap;
    }

    public void setThoigiantruycap(long thoigiantruycap) {
        this.thoigiantruycap = thoigiantruycap;
    }
}

