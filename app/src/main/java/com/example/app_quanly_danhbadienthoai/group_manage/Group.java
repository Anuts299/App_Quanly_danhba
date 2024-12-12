package com.example.app_quanly_danhbadienthoai.group_manage;

import java.util.HashMap;
import java.util.Map;

public class Group {
    private String id;
    private String ten_nhom;
    private String mo_ta;

    public Group() {
    }

    public Group(String id, String ten_nhom, String mo_ta) {
        this.id = id;
        this.ten_nhom = ten_nhom;
        this.mo_ta = mo_ta;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMo_ta() {
        return mo_ta;
    }

    public void setMo_ta(String mo_ta) {
        this.mo_ta = mo_ta;
    }

    public String getTen_nhom() {
        return ten_nhom;
    }

    public void setTen_nhom(String ten_nhom) {
        this.ten_nhom = ten_nhom;
    }

    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("ten_nhom", ten_nhom);
        result.put("mo_ta", mo_ta);
        return result;
    }

    @Override
    public String toString() {
        return "Group{" +
                "id='" + id + '\'' +
                ", ten_nhom='" + ten_nhom + '\'' +
                ", mo_ta='" + mo_ta + '\'' +
                '}';
    }
}
