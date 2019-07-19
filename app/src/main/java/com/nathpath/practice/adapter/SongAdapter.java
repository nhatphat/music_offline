package com.nathpath.practice.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.nathpath.practice.R;
import com.nathpath.practice.models.Song;

import org.w3c.dom.Text;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {
    private Context context;
    private List<Song> songs;

    public SongAdapter(Context context, List<Song> songs) {
        this.context = context;
        this.songs = songs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = songs.get(position);
        if(song == null){
            return;
        }

        holder.imgSongAvatar.setImageResource(R.drawable.ic_music);
        if(!song.getAvatar().equals("no_image")) {
            Glide.with(context).load(song.getAvatar()).into(holder.imgSongAvatar);
        }
        holder.tvSongName.setText(song.getName());
        holder.tvSongSingerName.setText(song.getSinger());
        if(song.getPageOnline() != null){
            holder.imgDownload.setVisibility(View.VISIBLE);
            holder.imgDownload.setOnClickListener(v -> {
                if(onItemSongClickListener != null){
                    onItemSongClickListener.OnDownloadClick(song);
                }
            });
        }
        if(song.getTime() != null) {
            holder.tvSongTime.setText(convertDurationToTime(Long.parseLong(song.getTime())));
        }
    }

    @SuppressLint("DefaultLocale")
    public static String convertDurationToTime(long duration){
        int hh = 0;
        int mm = 0;
        int ss = 0;

        final int TIME_AN_HOUR = 3600000;
        final int TIME_A_MINUTE = 60000;
        final int TIME_A_SECOND = 1000;

        hh = (int) (duration / TIME_AN_HOUR);
        mm = (int) ((duration - (hh*TIME_AN_HOUR))/TIME_A_MINUTE);
        ss = (int) ((duration - (hh*TIME_AN_HOUR + mm*TIME_A_MINUTE))/TIME_A_SECOND);
        if(ss == 0){
            ss = 1;
        }

        if(hh == 0){
            return String.format("%02d:%02d", mm, ss);
        }

        return String.format("%02d:%02d:%02d", hh, mm, ss);
    }

    public void moveItem(int start, int end){
        if (start < end) {
            for (int i = start; i < end; i++) {
                Collections.swap(songs, i, i + 1);
            }
        } else {
            for (int i = start; i > end; i--) {
                Collections.swap(songs, i, i - 1);
            }
        }
        notifyItemMoved(start, end);
    }

    public void removeItem(int position){
        songs.remove(position);
        notifyItemRemoved(position);
    }

    public boolean deleteSong(String filename) {
        File file = new File(filename);
        return file.exists() && file.delete();
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imgSongAvatar;
        ImageView imgDownload;
        TextView tvSongName;
        TextView tvSongSingerName;
        TextView tvSongTime;

        public ViewHolder(View itemView) {
            super(itemView);

            imgSongAvatar = itemView.findViewById(R.id.imgSongAvatar);
            imgDownload = itemView.findViewById(R.id.img_download_song);
            tvSongName = itemView.findViewById(R.id.tvSongName);
            tvSongSingerName = itemView.findViewById(R.id.tvSongSingerName);
            tvSongTime = itemView.findViewById(R.id.tvSongTime);


            itemView.setOnClickListener(v -> {
                if(onItemSongClickListener != null){
                    onItemSongClickListener.OnItemSongClick(itemView, getAdapterPosition());
                }
            });
        }
    }

    private OnItemSongClickListener onItemSongClickListener;

    public void setOnItemSongClickListener(OnItemSongClickListener listener){
        if(onItemSongClickListener == null){
            this.onItemSongClickListener = listener;
        }
    }

    public interface OnItemSongClickListener{
        void OnItemSongClick(View view, int position);
        void OnDownloadClick(Song song);
    }
}
