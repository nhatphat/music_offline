package com.nathpath.practice.callback;

import com.nathpath.practice.models.Song;

public interface SongServicePlayerView {
    void getLinkMp3FromPageOnlineSuccess(String link_mp3, Song song);
    void getLinkMp3FromPageOnlineFailed(String err);
}
