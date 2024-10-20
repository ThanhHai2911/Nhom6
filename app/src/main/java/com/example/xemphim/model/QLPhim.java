package com.example.xemphim.model;

public class QLPhim {
    private String tenPhim;
    private String namPhim;
    private String loaiPhim;
    private String theLoai;
    private String trangThai;
    private String ngayTao;
    private int imageResId; // Assuming you're using a drawable resource ID

    // Constructor
    public QLPhim(String tenPhim, String namPhim, String loaiPhim, String theLoai, String trangThai, String ngayTao, int imageResId) {
        this.tenPhim = tenPhim;
        this.namPhim = namPhim;
        this.loaiPhim = loaiPhim;
        this.theLoai = theLoai;
        this.trangThai = trangThai;
        this.ngayTao = ngayTao;
        this.imageResId = imageResId;
    }

    // Getters
    public String getTenPhim() {
        return tenPhim;
    }

    public String getNamPhim() {
        return namPhim;
    }

    public String getLoaiPhim() {
        return loaiPhim;
    }

    public String getTheLoai() {
        return theLoai;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public String getNgayTao() {
        return ngayTao;
    }

    public int getImageResId() {
        return imageResId;
    }
}
