package com.nathpath.practice;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.nathpath.practice.adapter.ProductAdapter;
import com.nathpath.practice.adapter.SongAdapter;
import com.nathpath.practice.callback.MainActivityView;
import com.nathpath.practice.models.Product;
import com.nathpath.practice.models.Song;
import com.nathpath.practice.presenter.MainActivityPresenter;
import com.nathpath.practice.services.SongPlayerService;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements MainActivityView {
    private Toolbar tbHeader;
    private RecyclerView rvMain;
    private RecyclerView rvMainSong;
    private List<Product> mProducts;
    private List<Song> mSongs;
    private ProductAdapter mProductAdapter;
    private SongAdapter mSongAdapter;
    private FloatingActionButton fabMain;
    private MainActivityPresenter mPresenter;

    private BottomSheetBehavior mBottomSheetSongController;
    private NestedScrollView mViewSongController;

    private ImageView img_avatar_song_ctr;
    private ImageView img_play_song_ctr;
    private ImageView img_prev_song_ctr;
    private ImageView img_next_song_ctr;
    private TextView tv_name_song_ctr;
    private TextView tv_total_time_song_ctr;
    private TextView tv_current_time_song_ctr;
    private SeekBar sb_time_song_ctr;


    private boolean isPlaying = false;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Serializable data = intent.getSerializableExtra(SongPlayerService.PAYLOAD);
            SongPlayerService.PayLoad payLoad = (SongPlayerService.PayLoad) data;
            initDataWithPayload(payLoad);
        }
    };;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissionReadSdCard();

        initView();
        initRecyclerView();

        setSupportActionBar(tbHeader);
        events();

        Log.e("err", "onCreate");

        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadcastReceiver, new IntentFilter(SongPlayerService.ACTION)
        );

        mBottomSheetSongController = BottomSheetBehavior.from(mViewSongController);

        mPresenter = new MainActivityPresenter(this, this);
        mPresenter.findSongsInLocal();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sentEventToPlayerService(SongPlayerService.GET_CURRENT_LIST_SONG, null);
        sentEventToPlayerService(SongPlayerService.GET_CURRENT_SONG, null);
    }

    private void initDataWithPayload(SongPlayerService.PayLoad payLoad){
        if(payLoad != null){
            switch (payLoad.getKey()){
                case SongPlayerService.GET_CURRENT_SONG:
                    Song song = (Song) payLoad.getData();
                    setInfoCurrentSong(song);
                    break;

                case SongPlayerService.GET_CURRENT_LIST_SONG:
                    boolean hasList = (boolean) payLoad.getData();
                    if(!hasList && (mSongs.size() > 0)){
                        sentEventToPlayerService(SongPlayerService.UPDATE_LIST_SONG, mSongs);
                    }
                    break;

                case SongPlayerService.GET_PROGRESS:
                    int progress = (int) payLoad.getData();
                    setProgressTime(progress);
                    break;

                case SongPlayerService.GET_IS_PLAYING:
                    isPlaying = (boolean) payLoad.getData();
                    toogleImageButtonPlay();
                    break;

                case SongPlayerService.GET_DONE:
                    if(sb_time_song_ctr != null){
                        isPlaying = false;
                        toogleImageButtonPlay();
                        sb_time_song_ctr.setProgress(0);
                        sb_time_song_ctr.setMax(100);
                        tv_name_song_ctr.setText("Chưa có bài hát");
                        tv_current_time_song_ctr.setText("00:00");
                        tv_total_time_song_ctr.setText("00:00");
                    }
                    break;
            }
        }
    }

    private void setInfoCurrentSong(Song song){
        if(song != null && tv_name_song_ctr != null){
            tv_name_song_ctr.setText(song.getName());
            tv_total_time_song_ctr.setText(mSongAdapter.convertDurationToTime(Long.parseLong(song.getTime())));
            sb_time_song_ctr.setMax(Integer.valueOf(song.getTime()) / 1000);
            sb_time_song_ctr.setProgress(0);

            if(getApplication() != null){
                Glide.with(getApplication())
                        .load(song.getAvatar())
                        .into(img_avatar_song_ctr);
            }
        }
    }

//    private void setCurrentSong(Song song){
//        if(song != null){
//            setInfoCurrentSong(song);
//        }else if(mSongs.size() > 0){
//            //set default song
//            Song song_default = mSongs.get(0);
//            sentEventToPlayerService(SongPlayerService.PREPARE_SONG, song_default);
//            setInfoCurrentSong(song_default);
//        }
//    }

    private void setProgressTime(int progress){
        if(sb_time_song_ctr != null){
            sb_time_song_ctr.setProgress(progress/1000);
            tv_current_time_song_ctr.setText(
                    mSongAdapter.convertDurationToTime(progress)
            );
        }
    }

    private void toogleSongController(){
        if(mBottomSheetSongController == null){
            return;
        }

        if(mBottomSheetSongController.getState() == BottomSheetBehavior.STATE_COLLAPSED){
            mBottomSheetSongController.setState(BottomSheetBehavior.STATE_EXPANDED);
        }else if(mBottomSheetSongController.getState() == BottomSheetBehavior.STATE_EXPANDED){
            mBottomSheetSongController.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    private boolean checkPermissionReadSdCard(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissionReadSdCard(){
        if(checkPermissionReadSdCard()){
            return;
        }

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        if(mPresenter != null){
                            mPresenter.findSongsInLocal();
                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if(response.isPermanentlyDenied()){
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Cần quyền truy cập bộ nhớ");
        builder.setMessage("Vui lòng cấp quyền mới xài được app nha...");
        builder.setPositiveButton("GOTO SETTINGS", (dialog, which) -> {
            dialog.cancel();
            openSettings();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    private void initRecyclerView(){
        rvMain = findViewById(R.id.rvMain);
        rvMainSong = findViewById(R.id.rvMainSong);
        rvMain.setHasFixedSize(true);
        rvMainSong.setHasFixedSize(true);
        rvMain.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvMainSong.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        mProducts = new ArrayList<>();
        mSongs = new ArrayList<>();
        mProductAdapter = new ProductAdapter(this, mProducts);
        mSongAdapter = new SongAdapter(this, mSongs);
        rvMain.setAdapter(mProductAdapter);
        rvMainSong.setAdapter(mSongAdapter);

    }

    private void initView(){
        tbHeader = findViewById(R.id.tbHeader);
        fabMain = findViewById(R.id.fabMain);
        mViewSongController = findViewById(R.id.bts_song_controller);
        img_avatar_song_ctr = findViewById(R.id.img_avatar_song_ctr);
        img_play_song_ctr = findViewById(R.id.img_play_song_ctr);
        img_prev_song_ctr = findViewById(R.id.img_prev_song_ctr);
        img_next_song_ctr = findViewById(R.id.img_next_song_ctr);
        tv_name_song_ctr = findViewById(R.id.tv_name_song_ctr);
        tv_current_time_song_ctr = findViewById(R.id.tv_time_song_current_ctr);
        tv_total_time_song_ctr = findViewById(R.id.tv_total_time_song_current_ctr);
        sb_time_song_ctr = findViewById(R.id.sb_time_song_ctr);
    }

    private void events(){
        sb_time_song_ctr.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    sentEventToPlayerService(SongPlayerService.SEEK_TO, progress*1000);
                    tv_current_time_song_ctr.setText(
                            mSongAdapter.convertDurationToTime(progress*1000)
                    );
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSongAdapter.setOnItemSongClickListener((view, position) -> {
            Song song = mSongs.get(position);
            sentEventToPlayerService(SongPlayerService.SET_SONG, song);
            isPlaying = true;
            toogleImageButtonPlay();
            setInfoCurrentSong(song);
        });

        fabMain.setOnClickListener(v -> {
            if(mPresenter != null){
                //mPresenter.getAllProduct();
                toast("Nghe nhạc thư giãn haha");
            }
        });

        img_avatar_song_ctr.setOnClickListener(v -> {
            toogleSongController();
        });

        img_play_song_ctr.setOnClickListener(v -> {
            tooglePlayMusic();
        });

        img_prev_song_ctr.setOnClickListener(v -> {
            sentEventToPlayerService(SongPlayerService.PREV, null);
        });

        img_next_song_ctr.setOnClickListener(v -> {
            sentEventToPlayerService(SongPlayerService.NEXT, null);
        });
    }

    private void toogleImageButtonPlay(){
        if(isPlaying){
            img_play_song_ctr.setImageResource(R.drawable.ic_pause);
        }else{
            img_play_song_ctr.setImageResource(R.drawable.ic_play);
        }
    }

    private void tooglePlayMusic(){
        if(isPlaying){
            isPlaying = false;
            sentEventToPlayerService(SongPlayerService.PAUSE, null);
        }else{
            isPlaying = true;
            sentEventToPlayerService(SongPlayerService.PLAY, null);
        }
        toogleImageButtonPlay();
    }

    private <D> void sentEventToPlayerService(int event, D data){
        Intent intent = new Intent(this, SongPlayerService.class);
        intent.putExtra(SongPlayerService.PAYLOAD, new SongPlayerService.PayLoad(event, data));
        startService(intent);
    }

    private void toast(String mess){
        Toast toast = Toast.makeText(this, mess, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Override
    public void getAllProductSuccess(List<Product> products) {
        if(products.size() > 0 && mProductAdapter != null && mProducts != null){
            mProducts.clear();
            mProducts.addAll(products);
            mProductAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void getAllProductFailed(String err) {
        toast(err);
    }

    @Override
    public void getAllSongSuccess(List<Song> songs) {
        if(songs.size() > 0 ){
            mSongs.clear();
            mSongs.addAll(songs);

            mSongAdapter.notifyDataSetChanged();

            sentEventToPlayerService(SongPlayerService.UPDATE_LIST_SONG, mSongs);
        }
    }

    @Override
    public void getAllSongFailed(String err) {
        toast(err);
    }
}
