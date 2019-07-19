package com.nathpath.practice.callback;

import com.nathpath.practice.models.Song;

import java.util.List;

public interface MusicOnlineFragmentView {
    void searchSongSuccess(List<Song> songs);
    void searchSongFailed(String err);
}
