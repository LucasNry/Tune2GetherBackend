package com.t2g.app.facade;

import com.t2g.app.model.Song;

public abstract class StreamingServiceFacade {

    public abstract Song getSongFromId(String id);

    public abstract Song getSongFromTitle(String title);

    public abstract Song getSongFromSongObject(Song object);

    public abstract String getSongIdFromURL(String serviceURL);
}
