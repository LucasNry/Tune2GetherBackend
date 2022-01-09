package com.t2g.app.facade;

import com.t2g.app.model.Playlist;
import com.t2g.app.model.Song;
import com.t2g.app.model.UserCredentialsTableEntry;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public abstract class StreamingServiceFacade<T extends Song> {
    private static final String REDIRECT_URI_TEMPLATE = "http://localhost:8080/redirect/%s/%s";

    private int batchSize;

    public abstract T getSongFromId(String id) throws Exception;

    public abstract List<T> getSongFromTitle(String title) throws Exception;

    public abstract T getSongFromSongObject(Song songObject) throws Exception;

    public abstract String getAssetIdFromURL(String serviceURL) throws Exception;

    public abstract Playlist getPlaylist(String playlistUrl) throws Exception;

    public abstract void createPlaylist(String userId, Playlist playlist) throws Exception;

    public abstract String getAuthenticationURL(String userId) throws Exception;

    public abstract UserCredentialsTableEntry getUserCredentials(String userId, String code) throws Exception;

    public abstract void refreshCredentials() throws Exception;

    public abstract String getServiceDomain();

    protected String getRedirectUri(String userId) {
        return String.format(REDIRECT_URI_TEMPLATE, getServiceDomain(), userId);
    }

    protected List<List<Song>> createSongBatch(List<Song> tracks) {
        List<List<Song>> batches = new ArrayList<>();

        for (int i = 0; i < Math.ceil(tracks.size() / (float) batchSize); i++) {
            batches.add(tracks.subList(i * batchSize, Math.min(tracks.size(), (i * batchSize) + batchSize)));
        }

        return batches;
    }
}
