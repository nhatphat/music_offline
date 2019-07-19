package com.nathpath.practice;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.nathpath.practice.adapter.SongAdapter;
import com.nathpath.practice.adapter.TopicAdapter;
import com.nathpath.practice.base.BaseMusicFragment;
import com.nathpath.practice.callback.MusicOnlineFragmentView;
import com.nathpath.practice.callback.SongServicePlayerView;
import com.nathpath.practice.models.Song;
import com.nathpath.practice.models.Topic;
import com.nathpath.practice.presenter.MusicOnlineFragmentPresenter;
import com.nathpath.practice.presenter.SongServicePlayerPresenter;
import com.nathpath.practice.services.SongPlayerService;
import com.nathpath.practice.utils.CompositeDisposableManager;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;

public class MusicOnlineFragment extends BaseMusicFragment implements MusicOnlineFragmentView, SongServicePlayerView {
    private SearchView svSearchSong;
    private FrameLayout flLoading;
    private ImageView img_playAll;
    private ImageView img_home;
    private RecyclerView rv_topic;
    private TopicAdapter topicAdapter;
    private List<Topic> topicList;

    private SongServicePlayerPresenter songServicePlayerPresenter;
    private MusicOnlineFragmentPresenter mPresenter;

    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_music_online;
    }

    @Override
    protected void initDataWithPayload(SongPlayerService.PayLoad payLoad) {
        if(payLoad == null){
            return;
        }
        switch (payLoad.getKey()){
            case SongPlayerService.GET_CURRENT_LIST_SONG:
                List<Song> currentList = (List<Song>) payLoad.getData();
                if(currentList.size() > 0 && currentList.get(0).getPageOnline() != null){
                    updateSongList(currentList);
                    if(checkHomeTopicShow()){
                        toogleHomeTopic();
                    }
                }
                break;
        }
    }

    @Override
    protected void OnViewCreated(View view) {
        super.OnViewCreated(view);

        svSearchSong = view.findViewById(R.id.sv_searchSongOnline);
        flLoading = view.findViewById(R.id.fl_loading);
        img_playAll = view.findViewById(R.id.img_playAllSongOnline);
        img_home = view.findViewById(R.id.img_home_page_online);

        initRecyclerViewTopic(view);

        showSearchLoading(false);

        mPresenter = new MusicOnlineFragmentPresenter(context, this);
        songServicePlayerPresenter = new SongServicePlayerPresenter(this);

        context.sentEventToPlayerService(SongPlayerService.GET_CURRENT_LIST_SONG, null);
    }

    @Override
    protected int getRecyclerViewResource() {
        return R.id.rvMainSongOnline;
    }

    @Override
    protected void event() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            rvMainSong.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                context.openSongController(false);
            });
        }

        mSongAdapter.setOnItemSongClickListener(new SongAdapter.OnItemSongClickListener() {
            @Override
            public void OnItemSongClick(View view, int position) {
                Song song = mSongs.get(position);
                context.sentEventToPlayerService(SongPlayerService.SET_SONG_ONLINE, song);
                context.sentEventToPlayerService(SongPlayerService.UPDATE_CURRENT_INDEX_SONG, position);
                context.updateCurrentSong(song);
            }

            @Override
            public void OnDownloadClick(Song song) {
                confirmDownload(song);
            }
        });

        svSearchSong.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(query.matches("^\\s+") || query.isEmpty()){
                    toast("Nhập gì đó rồi tìm");
                }else{
                    Disposable disposable = mPresenter.searchSongWithKey(query);
                    CompositeDisposableManager.addDisposable(disposable);
                    showSearchLoading(true);
                    context.openSongController(false);
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        img_playAll.setOnClickListener(v -> {
            if(mSongs.size() > 0){
                toast("Đã thêm danh sách phát.");
//                context.lockSongControl(true);
                context.showRemoveAllListControl(true);
                context.sentEventToPlayerService(SongPlayerService.UPDATE_LIST_SONG, mSongs);
                context.sentEventToPlayerService(SongPlayerService.UPDATE_CURRENT_INDEX_SONG, -1);
            }else{
                toast("Chưa có sanh sách phát.");
            }
        });

        topicAdapter.setOnItemClickListener((view, posotion) -> {
            //todo
            CompositeDisposableManager.addDisposable(
                    mPresenter.getHotMusicFromTopic(
                            topicList.get(posotion).getTitle()
                    )
            );
            showSearchLoading(true);
            context.openSongController(false);
        });

        img_home.setOnClickListener(v -> {
            toogleHomeTopic();
        });
    }

    private boolean checkHomeTopicShow(){
        return rv_topic.getVisibility() == View.VISIBLE;
    }

    private void toogleHomeTopic(){
        boolean isShow = !checkHomeTopicShow();

        if(isShow || mSongs.size() <= 0){
            rvMainSong.setVisibility(View.GONE);
            rv_topic.setVisibility(View.VISIBLE);
            return;
        }

        rv_topic.setVisibility(View.GONE);
        rvMainSong.setVisibility(View.VISIBLE);
    }

    private void initRecyclerViewTopic(View view){
        rv_topic = view.findViewById(R.id.rvTopic);
        rv_topic.setHasFixedSize(true);
        rv_topic.setLayoutManager(new GridLayoutManager(context, 2));

        topicList = new ArrayList<>();
        topicList.add(new Topic("Việt Nam", R.drawable.bg_bxh_vn));
        topicList.add(new Topic("Trung Quốc", R.drawable.bg_bxh_china));
        topicList.add(new Topic("Nhật Bản", R.drawable.bg_bxh_japan));
        topicList.add(new Topic("Âu Mỹ", R.drawable.bg_bxh_us_uk));
        topicList.add(new Topic("Hàn Quốc", R.drawable.bg_bxh_korea));
        topicList.add(new Topic("Khác", R.drawable.bg_bxh_other));

        topicAdapter = new TopicAdapter(context, topicList);
        rv_topic.setAdapter(topicAdapter);
    }

    private void confirmDownload(Song song){
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setIcon(R.drawable.ic_download)
                .setTitle("Xác nhận tải xuống")
                .setMessage(song.getName() + " - " + song.getSinger())
                .setNegativeButton("Không", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton("Tải xuống", ((dialog, which) -> {
                    if(songServicePlayerPresenter != null){
                        toast("Đang chuẩn bị tải xuống, xem trên thanh thông báo.");
                        CompositeDisposableManager.addDisposable(
                                songServicePlayerPresenter.getLinkMp3FromSong(song)
                        );
                    }
                }));
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showSearchLoading(boolean isShow){
        if(flLoading == null){
            return;
        }

        if(isShow){
            flLoading.setVisibility(View.VISIBLE);
        }else{
            flLoading.setVisibility(View.GONE);
        }
    }

    private void downloadSong(Song song){
        requestPermissionWriteSdCard();
        if(!checkPermissionWriteSdCard()){
            toast("Vui lòng cấp quyền cho ứng dụng...");
            return;
        }

        String[] splitExt = song.getData().split("\\.");
        String ext = splitExt[splitExt.length - 1];
        String filename = song.getName() + "-" + System.currentTimeMillis() + "." + ext;

        String folder = Environment.DIRECTORY_DOWNLOADS + "/" + getString(R.string.app_name);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(song.getData()))
                .setTitle(song.getName())
                .setDescription("Ca sĩ: " + song.getSinger())
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(folder, filename);
        request.allowScanningByMediaScanner();
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadManager.enqueue(request);
        }
    }

    @Override
    public void searchSongSuccess(List<Song> songs) {
        updateSongList(songs);
        if(checkHomeTopicShow()){
            toogleHomeTopic();
        }
        showSearchLoading(false);
        svSearchSong.clearFocus();
    }

    @Override
    public void searchSongFailed(String err) {
        showSearchLoading(false);
        toast(err);
    }

    private boolean checkPermissionWriteSdCard(){
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissionWriteSdCard(){
        if(checkPermissionWriteSdCard()){
            return;
        }

        Dexter.withActivity(context)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

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

    @Override
    public void getLinkMp3FromPageOnlineSuccess(String link_mp3, Song song) {
        downloadSong(song);
    }

    @Override
    public void getLinkMp3FromPageOnlineFailed(String err) {
        toast("Xảy ra lỗi khi tải xuống, xin thử lại...");
    }
}
