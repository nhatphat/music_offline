package com.nathpath.practice.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.nathpath.practice.models.PageContent;

import java.util.List;

public class MainViewPagerAdapter extends FragmentStatePagerAdapter {
    private Context context;
    private List<PageContent> pageContents;

    public MainViewPagerAdapter(FragmentManager fm, Context context, List<PageContent> pageContents) {
        super(fm);
        this.context = context;
        this.pageContents = pageContents;
    }

    @Override
    public Fragment getItem(int position) {
        return pageContents.get(position).getPage();
    }

    @Override
    public int getCount() {
        return pageContents.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return pageContents.get(position).getTitle();
    }
}
