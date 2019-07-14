package com.nathpath.practice.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.nathpath.practice.R;
import com.nathpath.practice.models.Song;

import org.w3c.dom.Text;

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
        Glide.with(context).load(song.getAvatar()).into(holder.imgSongAvatar);
        holder.tvSongName.setText(song.getName());
        holder.tvSongTime.setText(convertDurationToTime(Long.parseLong(song.getTime())));
    }

    @SuppressLint("DefaultLocale")
    public String convertDurationToTime(long duration){
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

        return String.format("%02d:%02d:%02d", hh, mm, ss);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imgSongAvatar;
        TextView tvSongName;
        TextView tvSongTime;

        public ViewHolder(View itemView) {
            super(itemView);

            imgSongAvatar = itemView.findViewById(R.id.imgSongAvatar);
            tvSongName = itemView.findViewById(R.id.tvSongName);
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
    }
}
