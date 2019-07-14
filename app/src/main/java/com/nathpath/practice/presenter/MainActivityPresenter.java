package com.nathpath.practice.presenter;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.nathpath.practice.callback.MainActivityView;
import com.nathpath.practice.models.Product;
import com.nathpath.practice.models.ProductManager;
import com.nathpath.practice.models.Song;
import com.nathpath.practice.utils.RetrofitAPI;
import com.nathpath.practice.utils.UrlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.ResourceObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class MainActivityPresenter {
    private Context context;
    private MainActivityView mCallBack;

    public MainActivityPresenter(Context context, MainActivityView mCallBack) {
        this.context = context;
        this.mCallBack = mCallBack;
    }

    public Disposable getAllProduct(){
        return RetrofitAPI.getRetrofit(UrlUtils.BASE_URL_PRODUCT)
                .create(ProductManager.class)
                .getAllProduct()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new ResourceObserver<List<Product>>() {
                    @Override
                    public void onNext(List<Product> products) {
                        mCallBack.getAllProductSuccess(products);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mCallBack.getAllProductFailed(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });

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

    private List<Song> getListSongFromCursor(Cursor cursor){
        List<Song> songs = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()){
                String id = getStringFromCursorWithCollumn(cursor, MediaStore.Audio.Media._ID);
                String data = getStringFromCursorWithCollumn(cursor, MediaStore.Audio.Media.DATA);
                String name = getStringFromCursorWithCollumn(cursor, MediaStore.Audio.Media.TITLE);
                String time = getStringFromCursorWithCollumn(cursor, MediaStore.Audio.Media.DURATION);
                String idAlbum = getStringFromCursorWithCollumn(cursor, MediaStore.Audio.Media.ALBUM_ID);
                String image = getImageUriOfSongMediaByIdAlbum(Integer.parseInt(idAlbum)).toString();

                Log.e("err", name + image);
                songs.add(new Song(Integer.parseInt(id), name, data, time, image));
            }

            cursor.close();
        }

        return songs;
    }

    private Cursor getCursorMp3FromUri(Uri uri){
        return context.getContentResolver().query(
                uri, null,
                MediaStore.Audio.Media.IS_MUSIC + " != 0", null,
                MediaStore.Audio.Media.TITLE + " ASC"
        );
    }

    public Disposable findSongsInLocal(){
        return Observable.fromCallable(() -> {
            Cursor mp3InSDCard = getCursorMp3FromUri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            Cursor mp3InPhone = getCursorMp3FromUri(MediaStore.Audio.Media.INTERNAL_CONTENT_URI);

            List<Song> songs = new ArrayList<>();
            songs.addAll(getListSongFromCursor(mp3InSDCard));
            songs.addAll(getListSongFromCursor(mp3InPhone));

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
