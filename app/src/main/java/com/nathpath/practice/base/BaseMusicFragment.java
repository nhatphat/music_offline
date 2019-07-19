package com.nathpath.practice.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.IdRes;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.nathpath.practice.MainActivity;
import com.nathpath.practice.R;
import com.nathpath.practice.adapter.SongAdapter;
import com.nathpath.practice.models.Song;
import com.nathpath.practice.services.SongPlayerService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseMusicFragment extends BaseFragment{
    protected RecyclerView rvMainSong;
    protected List<Song> mSongs;
    protected SongAdapter mSongAdapter;
    protected MainActivity context;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Serializable data = intent.getSerializableExtra(SongPlayerService.PAYLOAD);
            SongPlayerService.PayLoad payLoad = (SongPlayerService.PayLoad) data;
            initDataWithPayload(payLoad);
        }
    };

    protected abstract void initDataWithPayload(SongPlayerService.PayLoad payLoad);

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof MainActivity){
            this.context = (MainActivity) context;
        }

        LocalBroadcastManager.getInstance(context).registerReceiver(
                broadcastReceiver,
                new IntentFilter(SongPlayerService.ACTION)
        );
    }

    @Override
    protected void OnViewCreated(View view) {
        initRecyclerView(view);
    }

    protected void initRecyclerView(View view){
        rvMainSong = view.findViewById(getRecyclerViewResource());
        rvMainSong.setHasFixedSize(true);
        rvMainSong.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

        mSongs = new ArrayList<>();
        mSongAdapter = new SongAdapter(context, mSongs);
        rvMainSong.setAdapter(mSongAdapter);
    }

    protected abstract @IdRes int getRecyclerViewResource();

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    protected void updateSongList(List<Song> songList){
        if(songList.size() > 0){
            mSongs.clear();
            mSongs.addAll(songList);
            mSongAdapter.notifyDataSetChanged();
        }
    }
}
