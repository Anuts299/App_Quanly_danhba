package com.example.app_quanly_danhbadienthoai.contacts_manage;

import java.util.HashMap;
import java.util.Map;

public class Contacts {
    private String id;
    private String ten_lien_he;
    private String so_dien_thoai;
    private String email;
    private String dia_chi;
    private String ngay_sinh;
    private String anh;

    public Contacts() {
    }

    public Contacts(String id, String ten_lien_he, String so_dien_thoai, String email, String dia_chi, String ngay_sinh, String anh) {
        this.id = id;
        this.ten_lien_he = ten_lien_he;
        this.so_dien_thoai = so_dien_thoai;
        this.email = email;
        this.dia_chi = dia_chi;
        this.ngay_sinh = ngay_sinh;
        this.anh = anh;
    }

    public String getAnh() {
        return anh;
    }

    public void setAnh(String anh) {
        this.anh = anh;
    }

    public String getDia_chi() {
        return dia_chi;
    }

    public void setDia_chi(String dia_chi) {
        this.dia_chi = dia_chi;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNgay_sinh() {
        return ngay_sinh;
    }

    public void setNgay_sinh(String ngay_sinh) {
        this.ngay_sinh = ngay_sinh;
    }

    public String getSo_dien_thoai() {
        return so_dien_thoai;
    }

    public void setSo_dien_thoai(String so_dien_thoai) {
        this.so_dien_thoai = so_dien_thoai;
    }

    public String getTen_lien_he() {
        return ten_lien_he;
    }

    public void setTen_lien_he(String ten_lien_he) {
        this.ten_lien_he = ten_lien_he;
    }

    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("ten_lien_he", ten_lien_he);
        result.put("ngay_sinh", ngay_sinh);  // Lưu dưới dạng String
        result.put("so_dien_thoai", so_dien_thoai);
        result.put("dia_chi", dia_chi);
        result.put("so_dien_thoai", so_dien_thoai);
        result.put("anh", anh);
        return result;
    }

    @Override
    public String toString() {
        return "Contacts{" +
                "anh='" + anh + '\'' +
                ", id='" + id + '\'' +
                ", ten_lien_he='" + ten_lien_he + '\'' +
                ", so_dien_thoai='" + so_dien_thoai + '\'' +
                ", email='" + email + '\'' +
                ", dia_chi='" + dia_chi + '\'' +
                ", ngay_sinh='" + ngay_sinh + '\'' +
                '}';
    }
}
