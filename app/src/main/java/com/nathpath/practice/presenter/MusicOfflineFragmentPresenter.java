package com.nathpath.practice.presenter;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import com.nathpath.practice.callback.MusicOfflineFragmentView;
import com.nathpath.practice.models.Product;
import com.nathpath.practice.models.ProductManager;
import com.nathpath.practice.models.Song;
import com.nathpath.practice.utils.RetrofitAPI;
import com.nathpath.practice.utils.UrlUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.ResourceObserver;
import io.reactivex.schedulers.Schedulers;

public class MusicOfflineFragmentPresenter {
    private Context context;
    private MusicOfflineFragmentView mCallBack;

    public MusicOfflineFragmentPresenter(Context context, MusicOfflineFragmentView mCallBack) {
        this.context = context;
        this.mCallBack = mCallBack;
    }

    private String getStringFromCursorWithCollumn(Cursor cursor, String collumn){
        if(cursor != null){
            return cursor.getString(cursor.getColumnIndex(collumn));
        }
        return "";
    }

    private Uri getImageUriOfSongMediaByIdAlbum(int idAlbum){
        Uri sArtworkUri = Uri
                .parse("content://media/external/audio/albumart");
        Uri image = ContentUris.withAppendedId(sArtworkUri,
                idAlbum);
        return image;
    }

    private boolean checkMp3ExistInList(List<String> list, String title){
        for (int i = 0; i < list.size(); i++) {
            if(list.get(i).equals(title)){
                return true;
            }
        }
        return false;
    }

    private List<Song> getListSongFromCursor(Cursor cursor){
        List<Song> songs = new ArrayList<>();
        List<String> titles = new ArrayList<>();

        if (cursor != null) {
            while (cursor.moveToNext()){
                String data = getStringFromCursorWithCollumn(cursor, MediaStore.Audio.Media.DATA);
                if(data.endsWith(".ogg")){
                    continue;
                }

                String title = getStringFromCursorWithCollumn(cursor, MediaStore.Audio.Media.TITLE);
                if(checkMp3ExistInList(titles, title)){
                    continue;
                }
                titles.add(title);

                String id = getStringFromCursorWithCollumn(cursor, MediaStore.Audio.Media._ID);
                String name = getStringFromCursorWithCollumn(cursor, MediaStore.Audio.Media.DISPLAY_NAME);
                String singer = getStringFromCursorWithCollumn(cursor, MediaStore.Audio.Media.ARTIST);
                String time = getStringFromCursorWithCollumn(cursor, MediaStore.Audio.Media.DURATION);
                String idAlbum = getStringFromCursorWithCollumn(cursor, MediaStore.Audio.Media.ALBUM_ID);
                String image =  getImageUriOfSongMediaByIdAlbum(Integer.parseInt(idAlbum)).toString();

                Log.e(name, id + " / " + singer);
                songs.add(new Song(Integer.parseInt(id), title, singer, data, time, image));
            }

            cursor.close();
        }

        return songs;
    }

    private Cursor getCursorMp3FromUri(Uri uri){
        return context.getContentResolver().query(
                uri,
                null,
                MediaStore.Audio.Media.IS_MUSIC + " != 0",
                null,
                null
        );
    }

    public Disposable findSongsInLocal(){
        return Observable.fromCallable(() -> {
            Cursor mp3InSDCard = getCursorMp3FromUri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            //Cursor mp3InPhone = getCursorMp3FromUri(MediaStore.Audio.Media.INTERNAL_CONTENT_URI);

            List<Song> songs = new ArrayList<>(getListSongFromCursor(mp3InSDCard));
            //songs.addAll(getListSongFromCursor(mp3InPhone));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                songs.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
            }

            return  songs;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new ResourceObserver<List<Song>>() {
                    @Override
                    public void onNext(List<Song> songs) {
                        mCallBack.getAllSongSuccess(songs);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mCallBack.getAllSongFailed(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
