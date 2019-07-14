package com.nathpath.practice.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.nathpath.practice.models.Song;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.ResourceObserver;

public class SongPlayerService extends Service
        implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener{

    public static final String PAYLOAD = "payload";
    public static final String ACTION = "action";
    public static final int PREPARE_SONG = 0;
    public static final int SET_SONG = 1;
    public static final int PLAY = 2;
    public static final int NEXT = 3;
    public static final int PREV = 4;
    public static final int PAUSE = 5;
    public static final int SEEK_TO = 6;

    public static final int GET_CURRENT_SONG = 7;
    public static final int GET_CURRENT_LIST_SONG = 8;
    public static final int GET_PROGRESS = 9;
    public static final int GET_IS_PLAYING = 10;
    public static final int GET_DONE = 11;
    public static final int UPDATE_LIST_SONG = 12;

    private MediaPlayer mediaPlayer;
    private List<Song> songList;
    private Song currentSong = null;
    private int currentSongIndex = -1;
    private boolean isPrepare = false;
    private CompositeDisposable compositeDisposable;

    @Override
    public void onCreate() {
        super.onCreate();
        compositeDisposable = new CompositeDisposable();
        Disposable disposable = sendProgressOfCurrentMusicToActivity();
        compositeDisposable.add(disposable);

        songList = new ArrayList<>();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
    }

    private Disposable sendProgressOfCurrentMusicToActivity(){
        return Observable.interval(1, TimeUnit.SECONDS)
                .flatMap(aLong -> Observable.just(1))
                .subscribeWith(new ResourceObserver<Integer>() {
                    @Override
                    public void onNext(Integer integer) {
                        if(mediaPlayer != null && currentSong != null && mediaPlayer.isPlaying()){
                            Log.e("dang chay....", " ......");
                            sendLocationBroadcast(GET_PROGRESS, mediaPlayer.getCurrentPosition());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Serializable data = intent.getSerializableExtra(PAYLOAD);
        PayLoad payLoad = (PayLoad) data;
        controllerWithPayload(payLoad);

        return START_STICKY;
    }

    private void controllerWithPayload(PayLoad payLoad){
        if(payLoad == null || mediaPlayer == null){
            return;
        }

        switch (payLoad.getKey()){
            case PREPARE_SONG:
                Song data_prepare = (Song) payLoad.getData();
                if(data_prepare != null){
                    prepareSong(data_prepare.getData());
                }
                currentSong = data_prepare;
                break;

            case SET_SONG:
                Song data = (Song) payLoad.getData();
                if(data != null){
                    setSong(data.getData());
                }
                currentSong = data;
                break;

            case PLAY:
            case PAUSE:
                tooglePlaySong();
                break;
            case PREV:
                prevSong();
                break;
            case NEXT:
                nextSong();
                break;
            case SEEK_TO:
                int seek = (int) payLoad.getData();
                if(currentSong != null) {
                    mediaPlayer.seekTo(seek);
                }
                break;
            case GET_CURRENT_SONG:
                sendLocationBroadcast(GET_CURRENT_SONG, currentSong);
                if(currentSong != null){
                    sendLocationBroadcast(GET_IS_PLAYING, mediaPlayer.isPlaying());
                    sendLocationBroadcast(GET_PROGRESS, mediaPlayer.getCurrentPosition());
                }
                break;
            case GET_CURRENT_LIST_SONG:
                sendLocationBroadcast(GET_CURRENT_LIST_SONG, songList.size() > 0);
                break;
            case UPDATE_LIST_SONG:
                Log.e("err", "update list");
                List<Song> list = (List<Song>) payLoad.getData();
                songList.clear();
                songList.addAll(list);

                if(currentSong == null){
                    currentSongIndex = 0;
                    currentSong = songList.get(0);
                    prepareSong(currentSong.getData());
                    sendLocationBroadcast(GET_CURRENT_SONG, currentSong);
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(compositeDisposable != null && !compositeDisposable.isDisposed()){
            compositeDisposable.dispose();
        }

        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.reset();
        if(songList.size() > 0){
            nextSong();
        }else{
            currentSong = null;
            sendLocationBroadcast(GET_DONE, null);

            stopSelf();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        isPrepare = false;
    }

    public void nextSong(){
        if(songList.size() > 0){
            currentSongIndex++;
            if(currentSongIndex > songList.size() - 1){
                currentSongIndex = 0;
            }
            currentSong = songList.get(currentSongIndex);
            setSong(currentSong.getData());
            sendLocationBroadcast(GET_CURRENT_SONG, currentSong);
            sendLocationBroadcast(GET_IS_PLAYING, true);
        }
    }

    public void prevSong(){
        if(songList.size() > 0){
            currentSongIndex--;
            if(currentSongIndex < 0){
                currentSongIndex = songList.size() - 1;
            }
            currentSong = songList.get(currentSongIndex);
            setSong(currentSong.getData());
            sendLocationBroadcast(GET_CURRENT_SONG, currentSong);
            sendLocationBroadcast(GET_IS_PLAYING, true);
        }
    }


    public void tooglePlaySong(){
        if(mediaPlayer == null || currentSong == null){
            return;
        }

        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            return;
        }

        if(isPrepare){
            mediaPlayer.prepareAsync();
            return;
        }

        mediaPlayer.start();
    }

    private void prepareSong(String song){
        Log.e("err", song);
        if(mediaPlayer != null){
            try {
                if(mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.reset();
                mediaPlayer.setDataSource(song);
                isPrepare = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setSong(String song){
        if(mediaPlayer != null){
            prepareSong(song);
            mediaPlayer.prepareAsync();
        }
    }

    private <D> void sendLocationBroadcast(int event, D data) {
        Intent intent = new Intent(ACTION);
        intent.putExtra(PAYLOAD, new PayLoad<>(event, data));
        LocalBroadcastManager.getInstance(SongPlayerService.this).sendBroadcast(intent);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (mediaPlayer != null){
            mediaPlayer.reset();
            mediaPlayer.release();
        }
        return false;
    }

    public static class PayLoad<D> implements Serializable{
        private int key;
        private D data;

        public PayLoad(int key, D data) {
            this.key = key;
            this.data = data;
        }

        public int getKey() {
            return key;
        }

        public void setKey(int key) {
            this.key = key;
        }

        public D getData() {
            return data;
        }

        public void setData(D data) {
            this.data = data;
        }
    }
}
