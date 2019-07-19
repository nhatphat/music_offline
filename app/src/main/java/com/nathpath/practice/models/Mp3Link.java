package com.nathpath.practice.models;

import com.google.gson.annotations.SerializedName;

public class Mp3Link {
    @SerializedName("mp3")
    private String link;

    public Mp3Link(String link) {
        this.link = link;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
