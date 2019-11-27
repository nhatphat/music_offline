package com.nathpath.practice;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.nathpath.practice.adapter.MainViewPagerAdapter;
import com.nathpath.practice.adapter.SongAdapter;
import com.nathpath.practice.base.BaseActivity;
import com.nathpath.practice.callback.SwipeTouchListener;
import com.nathpath.practice.models.PageContent;
import com.nathpath.practice.models.Song;
import com.nathpath.practice.presenter.MusicOfflineFragmentPresenter;
import com.nathpath.practice.services.SongPlayerService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.observers.ResourceObserver;

public class MainActivity extends BaseActivity{
    private Toolbar tbHeader;

    private BottomSheetBehavior mBottomSheetSongController;
    private ConstraintLayout mViewSongController;

    private ConstraintLayout cl_container_song_ctr;
    private ProgressBar pb_loading_song_ctr;
    private ImageView img_avatar_song_ctr;
    private ImageView img_play_song_ctr;
    private ImageView img_prev_song_ctr;
    private ImageView img_next_song_ctr;
    private TextView tv_name_song_ctr;
    private TextView tv_total_time_song_ctr;
    private TextView tv_current_time_song_ctr;
    private SeekBar sb_time_song_ctr;
    private ImageView img_big_avatar_song_ctr;
    private TextView tv_singer_song_ctr;
    private ImageView img_remove_all_song_ctr;

    private AppBarLayout appBarLayoutMain;

    private List<PageContent> pageContents;
    private MainViewPagerAdapter mainViewPagerAdapter;
    private ViewPager viewPagerMain;
    private TabLayout tabLayoutMain;

    public boolean isPlaying = false;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Serializable data = intent.getSerializableExtra(SongPlayerService.PAYLOAD);
            SongPlayerService.PayLoad payLoad = (SongPlayerService.PayLoad) data;
            initDataWithPayload(payLoad);
        }
    };

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()){
//            case R.id.it_search:
//                toast("dang search");
//                return true;
//        }
//
//        return false;
//    }

    @Override
    protected void initView(){
        tbHeader = findViewById(R.id.tbHeader);
        appBarLayoutMain = findViewById(R.id.ablMain);
        cl_container_song_ctr = findViewById(R.id.cl_container_song_ctr);
        pb_loading_song_ctr = findViewById(R.id.pb_loading_song_ctr);
        mViewSongController = findViewById(R.id.bts_song_controller);
        img_avatar_song_ctr = findViewById(R.id.img_avatar_song_ctr);
        img_play_song_ctr = findViewById(R.id.img_play_song_ctr);
        img_prev_song_ctr = findViewById(R.id.img_prev_song_ctr);
        img_next_song_ctr = findViewById(R.id.img_next_song_ctr);
        tv_name_song_ctr = findViewById(R.id.tv_name_song_ctr);
        tv_current_time_song_ctr = findViewById(R.id.tv_time_song_current_ctr);
        tv_total_time_song_ctr = findViewById(R.id.tv_total_time_song_current_ctr);
        sb_time_song_ctr = findViewById(R.id.sb_time_song_ctr);
        img_big_avatar_song_ctr = findViewById(R.id.img_big_avatar_song_ctr);
        tv_singer_song_ctr = findViewById(R.id.tv_singer_song_ctr);
        img_remove_all_song_ctr = findViewById(R.id.img_remove_all_song_ctr);

        tabLayoutMain = findViewById(R.id.tl_tabMain);
        viewPagerMain = findViewById(R.id.vp_contentMain);
    }

    @Override
    protected void initData() {

        setSupportActionBar(tbHeader);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadcastReceiver, new IntentFilter(SongPlayerService.ACTION)
        );

        expandAppbar(false);
        mBottomSheetSongController = BottomSheetBehavior.from(mViewSongController);
        initTabAndViewPager();

        sentEventToPlayerService(SongPlayerService.GET_CURRENT_LIST_SONG, null);
    }

    private void initTabAndViewPager(){
        pageContents = new ArrayList<>();
        pageContents.add(new PageContent("Offline", R.drawable.ic_list_music, new MusicOfflineFragment()));
        pageContents.add(new PageContent("Online", R.drawable.ic_music_online, new MusicOnlineFragment()));
        mainViewPagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager(), this, pageContents);
        viewPagerMain.setAdapter(mainViewPagerAdapter);
        viewPagerMain.setCurrentItem(0);
        tabLayoutMain.setupWithViewPager(viewPagerMain);
        for (int i = 0; i < pageContents.size(); i++) {
            TabLayout.Tab tab = tabLayoutMain.getTabAt(i);
            if(tab != null){
                PageContent pageContent = pageContents.get(i);

                View view = getLayoutInflater().inflate(R.layout.item_tab_layout, null);
                ((ImageView)view.findViewById(R.id.img_icon_tab)).setImageResource(pageContent.getIcon());

                TextView tvTitle = view.findViewById(R.id.tv_title_tab);
                tvTitle.setText(pageContent.getTitle());
                if(i == 0){
                    tvTitle.setTextColor(Color.WHITE);
                }

                tab.setCustomView(view);
            }
        }
    }

    public void expandAppbar(boolean isExpand){
        appBarLayoutMain.setExpanded(isExpand, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void event() {
        tabLayoutMain.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View view = tab.getCustomView();
                if(view != null){
                    ((TextView)view.findViewById(R.id.tv_title_tab)).setTextColor(Color.WHITE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View view = tab.getCustomView();
                if(view != null){
                    ((TextView)view.findViewById(R.id.tv_title_tab)).setTextColor(Color.BLACK);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        sb_time_song_ctr.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    sentEventToPlayerService(SongPlayerService.SEEK_TO, progress*1000);
                    tv_current_time_song_ctr.setText(
                            SongAdapter.convertDurationToTime(progress*1000)
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

        img_avatar_song_ctr.setOnClickListener(v -> {
            toogleSongController();
        });

        img_play_song_ctr.setOnClickListener(v -> {
            if(sb_time_song_ctr.isEnabled()) {
                tooglePlayMusic();
            }else{
                toast("Không thể thao tác lúc này...");
            }
        });

        img_prev_song_ctr.setOnClickListener(v -> {
            if(sb_time_song_ctr.isEnabled()){
                lockSongControl(true);
                sentEventToPlayerService(SongPlayerService.PREV, null);
            }else{
                toast("Không thể thao tác lúc này...");
            }
        });

        img_next_song_ctr.setOnClickListener(v -> {
            if(sb_time_song_ctr.isEnabled()){
                lockSongControl(true);
                sentEventToPlayerService(SongPlayerService.NEXT, null);
            }else{
                toast("Không thể thao tác lúc này...");
            }
        });

        img_remove_all_song_ctr.setOnClickListener(v -> {
            sentEventToPlayerService(SongPlayerService.UNSET_LIST_SONG, null);
        });

        img_big_avatar_song_ctr.setOnTouchListener(new SwipeTouchListener(this){
            @Override
            public void onSwipeTop() {
                sentEventToPlayerService(SongPlayerService.SET_VOLUME, true);
            }

            @Override
            public void onSwipeBottom() {
                sentEventToPlayerService(SongPlayerService.SET_VOLUME, false);
            }

            @Override
            public void onSwipeLeft() {
                sentEventToPlayerService(SongPlayerService.SET_VOLUME, false);
            }

            @Override
            public void onSwipeRight() {
                sentEventToPlayerService(SongPlayerService.SET_VOLUME, true);
            }
        });
    }

    private void initDataWithPayload(SongPlayerService.PayLoad payLoad){
        if(payLoad != null){
            switch (payLoad.getKey()){
                case SongPlayerService.GET_CURRENT_LIST_SONG:
                    List<Song> hasList = (List<Song>) payLoad.getData();
                    if(hasList.size() > 0){
                        showRemoveAllListControl(true);
                    }else{
                        showRemoveAllListControl(false);
                    }
                    break;
                case SongPlayerService.GET_PROGRESS:
                    int progress = (int) payLoad.getData();
                    lockSongControl(false);
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
                case SongPlayerService.SET_SONG_ONLINE_ERROR:
                    String err = (String) payLoad.getData();
                    //toast(err);
                    Log.e("err", err);
                    break;
                case SongPlayerService.SET_LOCK_SONG_CONTROL:
                    boolean lock = (boolean) payLoad.getData();
                    lockSongControl(lock);
                    break;
                case SongPlayerService.UNSET_LIST_SONG:
                    toast("Danh sách phát trống.");
                    showRemoveAllListControl(false);
                    break;
                case SongPlayerService.GET_CURRENT_SONG:
                    Song song = (Song) payLoad.getData();
                    setInfoCurrentSongControl(song);
                    break;
            }
        }
    }

    public boolean checkListSongExists() {
        return img_remove_all_song_ctr != null
                && img_remove_all_song_ctr.getVisibility() == View.VISIBLE;

    }

    public void showRemoveAllListControl(boolean isShow){
        if(img_remove_all_song_ctr != null){
            if(isShow){
                img_remove_all_song_ctr.setVisibility(View.VISIBLE);
            }else{
                img_remove_all_song_ctr.setVisibility(View.GONE);
            }
        }
    }

    public void lockSongControl(boolean isLock){
        if(sb_time_song_ctr == null){
            return;
        }

        sb_time_song_ctr.setEnabled(!isLock);
        if(isLock){
            pb_loading_song_ctr.setVisibility(View.VISIBLE);
        }else{
            pb_loading_song_ctr.setVisibility(View.GONE);
        }
    }

    public void setInfoCurrentSongControl(Song song){
        if(song != null && tv_name_song_ctr != null){
            tv_name_song_ctr.setText(song.getName());
            tv_singer_song_ctr.setText(song.getSinger());

            if(song.getTime() != null){
                tv_total_time_song_ctr.setText(SongAdapter.convertDurationToTime(Long.parseLong(song.getTime())));
                sb_time_song_ctr.setMax(Integer.valueOf(song.getTime()) / 1000);
            }else{
                tv_total_time_song_ctr.setText("00:00");
                sb_time_song_ctr.setMax(100);
            }

            sb_time_song_ctr.setProgress(0);


            Log.e("avattatatar: ", song.getAvatar());
            cl_container_song_ctr.setBackgroundResource(R.drawable.ic_music);
            if(song.getAvatar().equals("no_image")){
                img_avatar_song_ctr.setImageResource(R.drawable.ic_music);
                img_big_avatar_song_ctr.setImageResource(R.drawable.ic_music);
            }else{
                if(getApplication() != null) {
                    RequestBuilder image = Glide.with(getApplication()).load(song.getAvatar());
                    image.into(img_avatar_song_ctr);
                    image.into(img_big_avatar_song_ctr);
                    image.into(new SimpleTarget() {
                        @Override
                        public void onResourceReady(@NonNull Object resource, @Nullable Transition transition) {
                                cl_container_song_ctr.setBackground((Drawable) resource);
                        }
                    });
                }
            }
        }
    }

    public void updateCurrentSong(Song song){
        if(song != null){
            toogleImageButtonPlay();
            setInfoCurrentSongControl(song);
            lockSongControl(true);
            openSongController(true);
        }
    }

    private void setProgressTime(int progress){
        if(sb_time_song_ctr != null){
            sb_time_song_ctr.setProgress(progress/1000);
            tv_current_time_song_ctr.setText(
                    SongAdapter.convertDurationToTime(progress)
            );
        }
    }

    public void openSongController(boolean isShow){
        if(mBottomSheetSongController == null){
            return;
        }

        if(isShow){
            mBottomSheetSongController.setState(BottomSheetBehavior.STATE_EXPANDED);
        }else{
            mBottomSheetSongController.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    private void toogleSongController(){
        if(mBottomSheetSongController == null){
            return;
        }

        if(mBottomSheetSongController.getState() == BottomSheetBehavior.STATE_COLLAPSED){
            openSongController(true);
        }else if(mBottomSheetSongController.getState() == BottomSheetBehavior.STATE_EXPANDED){
            openSongController(false);
        }
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

    public <D> void sentEventToPlayerService(int event, D data){
        Intent intent = new Intent(this, SongPlayerService.class);
        intent.putExtra(SongPlayerService.PAYLOAD, new SongPlayerService.PayLoad(event, data));
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(broadcastReceiver != null){
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    public void onBackPressed() {
        openSongController(false);
//        super.onBackPressed();
    }
}