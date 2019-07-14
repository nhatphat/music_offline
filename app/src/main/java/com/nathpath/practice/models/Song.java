package com.nathpath.practice.models;

import android.net.Uri;

import java.io.Serializable;

public class Song implements Serializable{
    public static final String DATA = "data";

    private int id;
    private String name;
    private String data;
    private String time;
    private String avatar;

    public Song(int id, String name, String data, String time, String avatar) {
        this.id = id;
        this.name = name;
        this.data = data;
        this.time = time;
        this.avatar = avatar;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
