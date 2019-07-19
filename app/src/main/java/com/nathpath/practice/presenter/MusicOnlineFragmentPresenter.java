package com.nathpath.practice.presenter;

import android.content.Context;
import android.util.Log;

import com.nathpath.practice.callback.MusicOnlineFragmentView;
import com.nathpath.practice.models.Song;
import com.nathpath.practice.models.SongService;
import com.nathpath.practice.utils.RetrofitAPI;

import java.util.Date;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.ResourceObserver;
import io.reactivex.schedulers.Schedulers;

public class MusicOnlineFragmentPresenter {
    private Context context;
    private MusicOnlineFragmentView mCallBack;

    public MusicOnlineFragmentPresenter(Context context, MusicOnlineFragmentView mCallBack) {
        this.context = context;
        this.mCallBack = mCallBack;
    }

    public Disposable searchSongWithKey(String key){
        if(mCallBack == null){
            return null;
        }

        return RetrofitAPI.getRetrofitSongService()
                .create(SongService.class)
                .searchSongWith(key)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new ResourceObserver<List<Song>>() {
                    @Override
                    public void onNext(List<Song> songs) {
                        Log.e("tim xong roi", "kkkk");
                        mCallBack.searchSongSuccess(songs);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mCallBack.searchSongFailed(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public Disposable getHotMusicFromTopic(String topic){
        if(mCallBack == null){
            return null;
        }

        return RetrofitAPI.getRetrofitSongService()
                .create(SongService.class)
                .getSongFromTopic(topic)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new ResourceObserver<List<Song>>() {
                    @Override
                    public void onNext(List<Song> songs) {
                        mCallBack.searchSongSuccess(songs);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mCallBack.searchSongFailed(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
