package com.nathpath.practice.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.companion.CompanionDeviceManager;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.nathpath.practice.MainActivity;
import com.nathpath.practice.R;
import com.nathpath.practice.callback.SongServicePlayerView;
import com.nathpath.practice.models.Song;
import com.nathpath.practice.presenter.SongServicePlayerPresenter;
import com.nathpath.practice.utils.CompositeDisposableManager;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.ResourceObserver;

import static com.nathpath.practice.GlobalApplication.CHANNEL_ID;

public class SongPlayerService extends Service
        implements SongServicePlayerView,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener{

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
    public static final int UPDATE_CURRENT_INDEX_SONG = 13;
    public static final int SET_SONG_ONLINE = 14;
    public static final int SET_SONG_ONLINE_ERROR = 15;
    public static final int SET_LOCK_SONG_CONTROL = 16;
    public static final int SET_VOLUME = 17;
    public static final int UNSET_LIST_SONG = 18;


    private MediaPlayer mediaPlayer;
    private List<Song> songList;
    private Song currentSong = null;
    private int currentSongIndex = -1;
    private boolean isPrepare = false;
//    private boolean isOnline = false;
//    private boolean isComplete = false;
    private CompositeDisposable compositeDisposable;

    private SongServicePlayerPresenter mPresenter;

    @Override
    public void onCreate() {
        super.onCreate();
        compositeDisposable = new CompositeDisposable();
        Disposable disposable = sendProgressOfCurrentMusicToActivity();
        compositeDisposable.add(disposable);

        mPresenter = new SongServicePlayerPresenter(this);

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
                            try{
                                sendLocationBroadcast(GET_PROGRESS, mediaPlayer.getCurrentPosition());
                            }catch (Exception ex){
                                Log.e("khong gui progress dk", "");
                            }

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

        return START_NOT_STICKY;
    }

    private void startForegroundWith(String title, String content){
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_laucher)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setSound(null)

                .build();

        startForeground(112, notification);
    }

    private void controllerWithPayload(PayLoad payLoad){
        if(payLoad == null || mediaPlayer == null){
            return;
        }

        switch (payLoad.getKey()){
            case PREPARE_SONG:
                Song data_prepare = (Song) payLoad.getData();
                if(data_prepare != null){
                    isPrepare = true;
                    currentSong = data_prepare;
                    prepareSong(data_prepare.getData());
                }
                break;

            case SET_SONG:
                Song data = (Song) payLoad.getData();
                currentSong = data;
                if(data != null){
                    setSong(data.getData());
                }
                break;
            case SET_SONG_ONLINE:
                Song song = (Song) payLoad.getData();
//                isOnline = true;
                setSongOnline(song);
                break;
            case UPDATE_CURRENT_INDEX_SONG:
                currentSongIndex = (int) payLoad.getData();
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
                if(isPrepare){
                    mediaPlayer.prepareAsync();
                    sendLocationBroadcast(GET_IS_PLAYING, true);
                }else {
                    try {
                        if (currentSong != null) {
                            mediaPlayer.seekTo(seek);
                        }
                    } catch (Exception ex) {
                        Log.e("không seek dk", "");
                    }
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
                sendLocationBroadcast(GET_CURRENT_LIST_SONG, songList);
                break;
            case UPDATE_LIST_SONG:
                Log.e("err", "update list");
                List<Song> list = (List<Song>) payLoad.getData();
                songList.clear();
                songList.addAll(list);

//                currentSongIndex = 0;
//                currentSong = songList.get(0);
//                setSong(currentSong.getData());
//                sendLocationBroadcast(GET_CURRENT_SONG, currentSong);
//                sendLocationBroadcast(GET_IS_PLAYING, true);
                break;
            case UNSET_LIST_SONG:
                if(songList.size() > 0){
                    currentSongIndex = -1;
                    songList.clear();
                    sendLocationBroadcast(UNSET_LIST_SONG, null);
                    return;
                }
                break;
            case SET_VOLUME:
                boolean increase = (boolean) payLoad.getData();
                if(increase){
                    increaseVolume();
                }else{
                    decreaseVolume();
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

        stopForeground(true);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //mp.reset();
//        isComplete = true;
        if(songList.size() > 0){
            nextSong();
        }else{
            //prepareSong(currentSong.getData());
            sendLocationBroadcast(GET_IS_PLAYING, false);
            stopForeground(true);
        }

//        if(isOnline){
//            prepareSong(currentSong.getData());
//            sendLocationBroadcast(GET_IS_PLAYING, false);
//        }else if(songList.size() > 0){
//            nextSong();
//        }else{
//            currentSong = null;
//            sendLocationBroadcast(GET_DONE, null);
//            stopSelf();
//        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        startForegroundWith(currentSong.getName(), currentSong.getSinger());
        isPrepare = false;
//        isComplete = false;
        if(currentSong.getPageOnline() != null){
            currentSong.setTime(String.valueOf(mediaPlayer.getDuration()));
            sendLocationBroadcast(GET_CURRENT_SONG, currentSong);
        }
        sendLocationBroadcast(GET_IS_PLAYING, true);
    }

    private void setSongOnline(Song song){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        mediaPlayer.reset();

        if(song != null){
            currentSong = song;
            Disposable disposable = mPresenter.getLinkMp3FromSong(song);
            CompositeDisposableManager.addDisposable(disposable);
        }
    }

    public void nextSong(){
        if(songList.size() > 0){
            currentSongIndex++;
            if(currentSongIndex > songList.size() - 1){
                currentSongIndex = 0;
            }
            currentSong = songList.get(currentSongIndex);
            setSong(currentSong.getData());
            sendLocationBroadcast(SET_LOCK_SONG_CONTROL, true);
            sendLocationBroadcast(GET_CURRENT_SONG, currentSong);

        }else{
            sendLocationBroadcast(SET_LOCK_SONG_CONTROL, false);
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
            sendLocationBroadcast(SET_LOCK_SONG_CONTROL, true);
            sendLocationBroadcast(GET_CURRENT_SONG, currentSong);
        }else{
            sendLocationBroadcast(SET_LOCK_SONG_CONTROL, false);
        }
    }

    public void tooglePlaySong(){
        if(mediaPlayer == null || currentSong == null){
            return;
        }

        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            stopForeground(true);
            return;
        }

        if(isPrepare){
            mediaPlayer.prepareAsync();
            return;
        }

        startForegroundWith(currentSong.getName(), currentSong.getSinger());
        mediaPlayer.start();
    }

    private boolean prepareSong(String song){
        if(currentSong == null){
            return false;
        }

        if(song == null){
            if(currentSong.getPageOnline() != null){
                setSongOnline(currentSong);
            }
            return false;
        }

        Log.e("playing : ", song);

        if(mediaPlayer != null){
            try {
                if(mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.reset();
                mediaPlayer.setDataSource(song);

                return true;
            } catch (IOException e) {
                Log.e("loi set data mp3", "");
            }
        }
        return false;
    }

    public void setSong(String song){
        if(mediaPlayer != null){
            if(prepareSong(song))  {
//                if(isComplete){
//                    Log.e("errr", "pre")
//                    return;
//                }
                mediaPlayer.prepareAsync();
            }
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
//            mediaPlayer.release();
        }
        return false;
    }

    private void increaseVolume(){
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if(mediaPlayer != null && audioManager != null) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
        }
    }

    private void decreaseVolume(){
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if(mediaPlayer != null && audioManager != null) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
        }
    }

    @Override
    public void getLinkMp3FromPageOnlineSuccess(String link_mp3, Song song) {
        if(link_mp3 == null || link_mp3.isEmpty() || song == null || currentSong.getPageOnline() == null){
            return;
        }

        if(currentSong.getPageOnline().equals(song.getPageOnline())){
            Log.e("playing : ", link_mp3);
            currentSong.setData(link_mp3);
            setSong(link_mp3);
        }else{
            getLinkMp3FromPageOnlineFailed(null);
        }
    }

    @Override
    public void getLinkMp3FromPageOnlineFailed(String err) {
        //currentSong = null;
        sendLocationBroadcast(SET_SONG_ONLINE_ERROR, "Có lỗi khi chơi online, vui lòng thử lại...");
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
