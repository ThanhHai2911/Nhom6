package com.example.xemphim.model;

public class PaymentInfo {
    public String idLichSuTT;
    public String idUser;
    public String content;
    public int amount;
    public long paymentDate;
    public int idTrangThai;

    public PaymentInfo() {}

    public PaymentInfo(String idLichSuTT, String idUser, String content, int amount, long paymentDate, int idTrangThai) {
        this.idLichSuTT = idLichSuTT;
        this.idUser = idUser;
        this.content = content;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.idTrangThai = idTrangThai;
    }
}