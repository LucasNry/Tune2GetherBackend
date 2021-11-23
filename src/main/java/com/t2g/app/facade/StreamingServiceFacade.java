package com.t2g.app.facade;

import com.t2g.app.model.Song;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

public abstract class StreamingServiceFacade {

    public abstract Song getSongFromId(String id) throws Exception;

    public abstract Song getSongFromTitle(String title) throws Exception;

    public abstract Song getSongFromSongObject(Song object) throws Exception;

    public abstract String getSongIdFromURL(String serviceURL) throws Exception;
}
