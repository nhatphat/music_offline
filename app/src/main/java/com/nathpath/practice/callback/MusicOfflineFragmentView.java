package com.nathpath.practice.callback;

import com.nathpath.practice.models.Product;
import com.nathpath.practice.models.Song;

import java.util.List;

public interface MusicOfflineFragmentView {
    void getAllSongSuccess(List<Song> songs);
    void getAllSongFailed(String err);
}
