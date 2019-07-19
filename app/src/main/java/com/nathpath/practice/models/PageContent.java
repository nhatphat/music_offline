package com.nathpath.practice.models;

import android.support.annotation.DrawableRes;

import com.nathpath.practice.base.BaseFragment;

public class PageContent {
    private String title;
    private @DrawableRes int icon;
    private BaseFragment page;

    public PageContent(String title, int icon, BaseFragment page) {
        this.title = title;
        this.icon = icon;
        this.page = page;
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

    public BaseFragment getPage() {
        return page;
    }

    public void setPage(BaseFragment page) {
        this.page = page;
    }
}
