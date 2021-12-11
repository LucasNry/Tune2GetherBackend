package com.t2g.app.facade;

import com.t2g.app.model.Song;

import java.util.List;

public abstract class StreamingServiceFacade<T extends Song> {

    public abstract T getSongFromId(String id) throws Exception;

    public abstract List<T> getSongFromTitle(String title) throws Exception;

    public abstract T getSongFromSongObject(Song object) throws Exception;

    public abstract String getSongIdFromURL(String serviceURL) throws Exception;

    public abstract void refreshCredentials() throws Exception;
}
