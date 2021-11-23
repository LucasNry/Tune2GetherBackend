package com.t2g.app.facade;

import com.t2g.app.model.Song;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.data.tracks.GetTrackRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SpotifyAPIFacade extends StreamingServiceFacade {

    @Autowired
    private SpotifyApi spotifyApi;

    private static final String SONG_ID_SEPARATOR = "/";

    @Override
    public Song getSongFromId(String id) throws Exception {
        GetTrackRequest getTrackRequest = spotifyApi
                .getTrack(id)
                .build();

        Track track = getTrackRequest.execute();
        return new Song(track);
    }

    @Override
    public Song getSongFromTitle(String title) throws Exception {
        return null;
    }

    @Override
    public Song getSongFromSongObject(Song object) throws Exception {
        return null;
    }

    @Override
    public String getSongIdFromURL(String serviceURL) {
        String[] splitURL = serviceURL.split(SONG_ID_SEPARATOR);
        return splitURL[splitURL.length - 1];
    }
}
