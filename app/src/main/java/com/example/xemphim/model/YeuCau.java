package com.example.xemphim.model;

public class YeuCau {
    private int amount;
    private String content;
    private String idLichSuTT;
    private int idTrangThai;
    private String idUser;
    private String paymentDate;

    // Constructor rỗng (yêu cầu cho Firebase)
    public YeuCau() {}

    // Constructor đầy đủ
    public YeuCau(int amount, String content, String idLichSuTT, int idTrangThai, String idUser, String paymentDate) {
        this.amount = amount;
        this.content = content;
        this.idLichSuTT = idLichSuTT;
        this.idTrangThai = idTrangThai;
        this.idUser = idUser;
        this.paymentDate = paymentDate;
    }

    // Getters và setters
    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getIdLichSuTT() {
        return idLichSuTT;
    }

    public void setIdLichSuTT(String idLichSuTT) {
        this.idLichSuTT = idLichSuTT;
    }

    public int getIdTrangThai() {
        return idTrangThai;
    }

    public void setIdTrangThai(int idTrangThai) {
        this.idTrangThai = idTrangThai;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }
}

