package com.nathpath.practice.models;

import android.support.annotation.DrawableRes;

public class Topic {
    private String title;
    private @DrawableRes int icon;

    public Topic(String title, int icon) {
        this.title = title;
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
