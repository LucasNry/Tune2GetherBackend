package com.t2g.app.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.util.Collection;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
public class DeezerSong extends Song {
    private static final String QUERY_PARAMETER_SEPARATOR = " ";
    private static final String ENCODED_QUERY_PARAMETER_SEPARATOR = "%20";
    private static final String QUERY_PARAMETER_KEY_VALUE_SEPARATOR = ":";
    private static final String QUERY_PARAMETER_VALUE_TEMPLATE = "'%s'";

    @Builder
    public DeezerSong(String title, List<String> artists, String album, List<TrackCover> images, String uri) {
        super(title, artists, album, images, uri, QUERY_PARAMETER_SEPARATOR);
    }

    public DeezerSong(Song song) {
        super(song.getTitle(), song.getArtists(), null, null, null, QUERY_PARAMETER_SEPARATOR);
    }

    @Override
    public String serializeField(String key, Object value) {
        return key +
                QUERY_PARAMETER_KEY_VALUE_SEPARATOR +
                String.format(QUERY_PARAMETER_VALUE_TEMPLATE, value);
    }

    @Override
    public String serializeCollection(String key, Collection<?> value) {
        Object obj = ((List<Object>) value).get(0);
        return serializeField(key, obj);
    }

    @Override
    public String getQueryString() throws Exception {
        return super.getQueryString().replaceAll(QUERY_PARAMETER_SEPARATOR, ENCODED_QUERY_PARAMETER_SEPARATOR);
    }
}
