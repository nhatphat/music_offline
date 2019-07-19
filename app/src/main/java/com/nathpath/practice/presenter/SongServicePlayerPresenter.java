package com.nathpath.practice.presenter;

import android.util.Log;

import com.nathpath.practice.callback.SongServicePlayerView;
import com.nathpath.practice.models.Mp3Link;
import com.nathpath.practice.models.Song;
import com.nathpath.practice.models.SongService;
import com.nathpath.practice.utils.RetrofitAPI;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.ResourceObserver;
import io.reactivex.schedulers.Schedulers;

public class SongServicePlayerPresenter {
    private SongServicePlayerView mCallBack;

    public SongServicePlayerPresenter(SongServicePlayerView mCallBack) {
        this.mCallBack = mCallBack;
    }

    public Disposable getLinkMp3FromSong(Song song){
        if(mCallBack == null && song == null){
            return null;
        }

        return RetrofitAPI.getRetrofitSongService()
                .create(SongService.class)
                .getSongDataFromPageOnline(song.getPageOnline())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new ResourceObserver<Mp3Link>() {
                    @Override
                    public void onNext(Mp3Link mp3) {
                        song.setData(mp3.getLink());
                        mCallBack.getLinkMp3FromPageOnlineSuccess(mp3.getLink(), song);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mCallBack.getLinkMp3FromPageOnlineFailed(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
