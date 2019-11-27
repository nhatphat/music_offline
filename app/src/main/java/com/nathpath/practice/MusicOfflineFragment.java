package com.nathpath.practice;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.nathpath.practice.adapter.SongAdapter;
import com.nathpath.practice.base.BaseMusicFragment;
import com.nathpath.practice.callback.ItemRecyclerViewTouchHelperCallback;
import com.nathpath.practice.callback.ItemRecyclerViewTouchListener;
import com.nathpath.practice.callback.MusicOfflineFragmentView;
import com.nathpath.practice.models.Song;
import com.nathpath.practice.presenter.MusicOfflineFragmentPresenter;
import com.nathpath.practice.services.SongPlayerService;
import com.nathpath.practice.utils.CompositeDisposableManager;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import pl.droidsonroids.gif.GifImageView;

public class MusicOfflineFragment extends BaseMusicFragment
        implements MusicOfflineFragmentView, ItemRecyclerViewTouchListener {
    private SwipeRefreshLayout srl_refreshMusic;
    private ImageView img_refreshList;
    private ImageView img_playAll;
    private SearchView sv_searchSongOffline;

    private MusicOfflineFragmentPresenter mPresenter;

    private List<Song> mSongCopy;

    public MusicOfflineFragment(){}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestPermissionReadSdCard();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_music_offline;
    }

    @Override
    protected void OnViewCreated(View view) {
        super.OnViewCreated(view);

        //init touch event for recycler
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new ItemRecyclerViewTouchHelperCallback(this, context)
        );
        itemTouchHelper.attachToRecyclerView(rvMainSong);

        srl_refreshMusic = view.findViewById(R.id.srl_refreshMusicOffline);
        img_refreshList = view.findViewById(R.id.img_reloadSong);
        img_playAll = view.findViewById(R.id.img_playAllSongOffline);
        sv_searchSongOffline = view.findViewById(R.id.sv_searchSongOffline);

        mPresenter = new MusicOfflineFragmentPresenter(context, this);
        Disposable disposable = mPresenter.findSongsInLocal();
        CompositeDisposableManager.addDisposable(disposable);
        srl_refreshMusic.setRefreshing(true);
    }

    @Override
    protected int getRecyclerViewResource() {
        return R.id.rvMainSong;
    }

    @Override
    public void onResume() {
        super.onResume();
//        context.sentEventToPlayerService(SongPlayerService.GET_CURRENT_LIST_SONG, null);
//        context.sentEventToPlayerService(SongPlayerService.GET_CURRENT_SONG, null);
    }

    @Override
    protected void initDataWithPayload(SongPlayerService.PayLoad payLoad){
        if(payLoad == null){
            return;
        }

        switch (payLoad.getKey()){
            case SongPlayerService.GET_CURRENT_SONG:
                Song song = (Song) payLoad.getData();
                if(song == null && mSongs.size() > 0){
                    song = mSongs.get(0);

                    context.lockSongControl(false);
                    context.sentEventToPlayerService(SongPlayerService.PREPARE_SONG, song);
                }

                int itemIndex = findPositionItemSongBySong(song);
                if(itemIndex == -1){
                    return;
                }
                if(currentSong != -1) {
                    oldSong = currentSong;
                    mSongs.get(oldSong).setSelected(false);
                    mSongAdapter.notifyItemChanged(oldSong);
                }

                currentSong = itemIndex;
                song = mSongs.get(currentSong);
                song.setSelected(true);
                mSongAdapter.notifyItemChanged(currentSong);

                context.setInfoCurrentSongControl(song);

                if(rvMainSong != null && itemIndex != -1) {
                    rvMainSong.smoothScrollToPosition(itemIndex);
                }
                break;

            case SongPlayerService.GET_CURRENT_LIST_SONG:
//                List<Song> hasList = (List<Song>) payLoad.getData();
//                if(hasList.size() > 0){
//                    updateSongList(hasList);
//                }else if(mPresenter != null){
//                    CompositeDisposableManager.addDisposable(
//                            mPresenter.findSongsInLocal()
//                    );
//                }
                break;
        }
    }

    @Override
    protected void event(){
        sv_searchSongOffline.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(mSongs.size() <= 0){
                    toast("Không tìm thấy kết quả.");
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.isEmpty() && mSongCopy != null){
                    updateSongList(mSongCopy);
                }else{
                    searchSongOfflineWithKey(newText);
                }
                return false;
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            rvMainSong.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                context.openSongController(false);
            });
        }

        mSongAdapter.setOnItemSongClickListener(new SongAdapter.OnItemSongClickListener() {
            @Override
            public void OnItemSongClick(View view, int position) {
                playSongSelectedAt(position);

//                Song song = mSongs.get(position);
//
//                oldSong = currentSong;
//                mSongs.get(oldSong).setSelected(false);
//                mSongAdapter.notifyItemChanged(oldSong);
//
//                currentSong = position;
//                song.setSelected(true);
//                mSongAdapter.notifyItemChanged(currentSong);
//
//                context.sentEventToPlayerService(SongPlayerService.SET_SONG, song);
//                context.sentEventToPlayerService(SongPlayerService.UPDATE_CURRENT_INDEX_SONG, position);
//
//                context.isPlaying = true;
//                context.updateCurrentSong(song);
            }

            @Override
            public void OnDownloadClick(Song song) {
                toast(song.getName());
            }
        });

        srl_refreshMusic.setOnRefreshListener(() -> {
            refreshListSong();
        });

        img_refreshList.setOnClickListener(v -> {
            refreshListSong();
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
    }

    private void searchSongOfflineWithKey(String key){
        List<Song> result = new ArrayList<>();
        for (int i = 0; i < mSongs.size(); i++) {
            Song song = mSongs.get(i);
            if(song.getName().toLowerCase().contains(key.toLowerCase())
                    || song.getSinger().toLowerCase().contains(key.toLowerCase())){
                result.add(song);
            }
        }
        updateSongList(result);
    }

    private void refreshListSong(){
        toast("Đang cập nhật danh sách");
        CompositeDisposableManager.addDisposable(mPresenter.findSongsInLocal());
        srl_refreshMusic.setRefreshing(true);
    }

    private boolean checkPermissionReadSdCard(){
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissionReadSdCard(){
        if(checkPermissionReadSdCard()){
            return;
        }

        Dexter.withActivity(context)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        if(mPresenter != null){
                            Disposable disposable = mPresenter.findSongsInLocal();
                            CompositeDisposableManager.addDisposable(disposable);
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

    @Override
    public void getAllSongSuccess(List<Song> songs) {
        updateSongList(songs);
        mSongCopy = new ArrayList<>(songs);
        context.sentEventToPlayerService(SongPlayerService.GET_CURRENT_SONG, null);
        srl_refreshMusic.setRefreshing(false);
        toast("Đã cập nhật");
    }

    @Override
    public void getAllSongFailed(String err) {
        srl_refreshMusic.setRefreshing(false);
        toast(err);
    }

    @Override
    public void onSwipe(int position, int direction) {
        switch (direction){
            case ItemTouchHelper.START:
            case ItemTouchHelper.END:
                Song songDel = mSongs.get(position);
                mSongAdapter.removeItem(position);
                confirmDeleteSong(songDel, position);
                break;
        }
    }

    @Override
    public void onMove(int startPosition, int endPosition) {
        mSongAdapter.moveItem(startPosition, endPosition);

        if(currentSong == startPosition){
            currentSong = endPosition;
        }

        if(context.checkListSongExists()){
            context.sentEventToPlayerService(SongPlayerService.UPDATE_CURRENT_INDEX_SONG, endPosition);
            context.sentEventToPlayerService(SongPlayerService.UPDATE_LIST_SONG, mSongs);
        }

    }

    private void confirmDeleteSong(Song song, int position){
        View view = View.inflate(context, R.layout.item_song, null);
        Glide.with(context).load(song.getAvatar()).into((ImageView)view.findViewById(R.id.imgSongAvatar));
        ((TextView)view.findViewById(R.id.tvSongName)).setText(song.getName());
        ((TextView)view.findViewById(R.id.tvSongSingerName)).setText(song.getSinger());
        ((TextView)view.findViewById(R.id.tvSongTime)).setText(SongAdapter.convertDurationToTime(Long.parseLong(song.getTime())));
        view.findViewById(R.id.img_download_song).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.img_song_playing).setVisibility(View.INVISIBLE);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setIcon(R.drawable.ic_delete_red)
                .setTitle("Xác nhận xóa")
                .setCancelable(false)
                .setView(view)
                .setNegativeButton("Xóa", (dialog, which) -> {
                    if(mSongAdapter.deleteSong(song.getData())){
                        toast("Đã xóa");
                        if(context.checkListSongExists()){
                            context.sentEventToPlayerService(SongPlayerService.UPDATE_LIST_SONG, mSongs);
                        }
                    }else{
                        toast("Xảy ra lỗi khi xóa, xin thử lại");
                        mSongs.add(position, song);
                        mSongAdapter.notifyItemInserted(position);
                        rvMainSong.smoothScrollToPosition(position);
                    }
                })
                .setPositiveButton("Không", (dialog, which) -> {
                    dialog.dismiss();
                    mSongs.add(position, song);
                    mSongAdapter.notifyItemInserted(position);
                    rvMainSong.smoothScrollToPosition(position);
                });
        Dialog dialog = builder.create();
        dialog.show();
    }
}
