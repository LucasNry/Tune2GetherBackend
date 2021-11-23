package com.t2g.app.model;

import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Image;
import com.wrapper.spotify.model_objects.specification.Track;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class Song {

    public Song(Track track) { // TODO: Create one constructor per Streaming Service
        this.title = track.getName();
        this.artists = Arrays
                .stream(track.getArtists())
                .map(ArtistSimplified::getName)
                .collect(Collectors.toList());
        this.album = track
                .getAlbum()
                .getName();

        this.images = new ArrayList<>();
        for (Image image : track.getAlbum().getImages()) {
            images.add(
                    TrackCover
                            .builder()
                            .url(image.getUrl())
                            .height(image.getHeight())
                            .width(image.getWidth())
                            .build()
            );
        }

        this.uri = track.getUri();
    }

    private String title;

    private List<String> artists;

    private String album;

    private List<TrackCover> images;

    private String uri;
}
