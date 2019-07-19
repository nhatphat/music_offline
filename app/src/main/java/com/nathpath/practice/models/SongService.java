package com.nathpath.practice.models;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SongService {
    @GET("/search/{key}")
    Observable<List<Song>> searchSongWith(@Path("key") String key);

    @GET("/mp3")
    Observable<Mp3Link> getSongDataFromPageOnline(@Query("link_mp3") String pageOnline);

    @GET("/bxh")
    Observable<List<Song>> getSongFromTopic(@Query("topic") String topic);
}
