package com.example.app_quanly_danhbadienthoai.contacts_group_manage;

import java.util.HashMap;
import java.util.Map;

public class Contacts_group {
    private String id;
    private String id_contacts;
    private String id_group;

    public Contacts_group() {
    }

    public Contacts_group(String id, String id_contacts, String id_group) {
        this.id = id;
        this.id_contacts = id_contacts;
        this.id_group = id_group;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId_contacts() {
        return id_contacts;
    }

    public void setId_contacts(String id_contacts) {
        this.id_contacts = id_contacts;
    }

    public String getId_group() {
        return id_group;
    }

    public void setId_group(String id_group) {
        this.id_group = id_group;
    }
    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("id_contacts", id_contacts);
        result.put("id_group", id_group);
        return result;
    }

    @Override
    public String toString() {
        return "Contacts_group{" +
                "id='" + id + '\'' +
                ", id_contacts='" + id_contacts + '\'' +
                ", id_group='" + id_group + '\'' +
                '}';
    }
}
