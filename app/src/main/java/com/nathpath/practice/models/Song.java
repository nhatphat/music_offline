package com.nathpath.practice.models;

import android.net.Uri;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Song implements Serializable{
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("mp3")
    private String data;
    private String time;
    @SerializedName("image")
    private String avatar;
    @SerializedName("singer")
    private String singer;
    @SerializedName("page_mp3")
    private String page_online;
    private boolean isSelected = false;

    public Song(int id, String name, String singer, String data, String time, String avatar) {
        this.id = id;
        this.name = name;
        this.singer = singer;
        this.data = data;
        this.time = time;
        this.avatar = avatar;
    }

    public Song(String name, String singer, String page_online, String avatar) {
        this.name = name;
        this.singer = singer;
        this.avatar = avatar;
        this.page_online = page_online;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
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

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getPageOnline() {
        return page_online;
    }

    public void setPageOnline(String page_online) {
        this.page_online = page_online;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
