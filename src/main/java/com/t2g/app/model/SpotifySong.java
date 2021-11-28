package com.t2g.app.model;

import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Track;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
public class SpotifySong extends Song {
    private static final String QUERY_PARAMETER_SEPARATOR = " ";
    private static final String QUERY_PARAMETER_KEY_VALUE_SEPARATOR = ":";

    @Builder
    public SpotifySong(
            String title,
            List<String> artists,
            String album,
            List<TrackCover> images,
            String uri
    ) {
        super(
                title,
                artists,
                album,
                images,
                uri,
                QUERY_PARAMETER_SEPARATOR
        );
    }

    public SpotifySong(Track track) {
        super(
                track.getName(),
                Arrays
                        .stream(track.getArtists())
                        .map(ArtistSimplified::getName)
                        .collect(Collectors.toList()),
                track
                        .getAlbum()
                        .getName(),
                Arrays
                        .stream(track.getAlbum().getImages())
                        .map(
                                image -> TrackCover
                                        .builder()
                                        .url(image.getUrl())
                                        .height(image.getHeight())
                                        .width(image.getWidth())
                                        .build()
                        )
                        .collect(Collectors.toList()),
                track
                        .getExternalUrls()
                        .get(StreamingService.SPOTIFY.getDomainName()),
                QUERY_PARAMETER_SEPARATOR
        );
    }

    @Override
    public String serializeField(String key, Object value) {
        return key +
                QUERY_PARAMETER_KEY_VALUE_SEPARATOR +
                value;
    }

    @Override
    public String serializeCollection(String key, Collection<?> value) {
        Object obj = ((List<Object>) value).get(0);
        return serializeField(key, obj);
    }
}
