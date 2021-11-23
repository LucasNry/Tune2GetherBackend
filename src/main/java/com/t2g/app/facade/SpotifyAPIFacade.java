package com.t2g.app.facade;

import com.t2g.app.model.Song;
import org.springframework.stereotype.Component;

@Component
public class SpotifyAPIFacade extends StreamingServiceFacade {
    private static final String SONG_ID_SEPARATOR = "/";

    @Override
    public Song getSongFromId(String id) {
        return null;
    }

    @Override
    public Song getSongFromTitle(String title) {
        return null;
    }

    @Override
    public Song getSongFromSongObject(Song object) {
        return null;
    }

    @Override
    public String getSongIdFromURL(String serviceURL) {
        String[] splitURL = serviceURL.split(SONG_ID_SEPARATOR);
        return splitURL[splitURL.length - 1];
    }
}
