package com.t2g.app.model;

import com.google.gson.JsonObject;
import com.t2g.app.annotations.QueryName;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Image;
import com.wrapper.spotify.model_objects.specification.Track;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
public class Song {
    private static final String QUERY_PARAMETER_SEPARATOR = " ";
    private static final String QUERY_PARAMETER_KEY_VALUE_SEPARATOR = ":";

    @QueryName("track")
    @Builder.Default
    private String title = null;

    @QueryName("artist")
    @Builder.Default
    private List<String> artists = null;

    @QueryName("album")
    @Builder.Default
    private String album = null;

    private List<TrackCover> images;

    private String url;

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

        this.url = track
                .getExternalUrls()
                .get(StreamingService.SPOTIFY.getDomainName());
    }

    public Song(JsonObject trackInfo){
        JsonObject songSnippet = trackInfo.getAsJsonObject("snippet");
        JsonObject songId = trackInfo.getAsJsonObject("id");

        String title = songSnippet
                .getAsJsonPrimitive("title")
                .getAsString();
        this.title = title;

        String artist = songSnippet
                .getAsJsonPrimitive("channelTitle")
                .getAsString();
        this.artists.add(artist);

        JsonObject imagesObject = songSnippet
                .getAsJsonObject("thumbnails")
                .getAsJsonObject("high");

        String imgUrl = imagesObject
                .getAsJsonPrimitive("url")
                .getAsString();

        int imgHeight = imagesObject
                .getAsJsonPrimitive("height")
                .getAsInt();

        int imgWidth = imagesObject
                .getAsJsonPrimitive("width")
                .getAsInt();

        this.images.add(
                TrackCover
                        .builder()
                        .url(imgUrl)
                        .height(imgHeight)
                        .width(imgWidth)
                        .build()
        );

        String videoId = songId
                .getAsJsonPrimitive("videoId")
                .getAsString();

        String defaultYTDomain = "https://www.youtube.com/watch?v=";

        this.url = defaultYTDomain + videoId;
    }

    public String getQueryString() throws Exception {
        StringBuilder sb = new StringBuilder();

        for (Field field : Song.class.getDeclaredFields()) {
            if (field.isAnnotationPresent(QueryName.class)) {
                String key = field.getAnnotation(QueryName.class).value();
                Object value = field.get(this);

                if (value != null) {
                    if (sb.length() > 0) {
                        sb.append(QUERY_PARAMETER_SEPARATOR);
                    }

                    if (value instanceof Collection<?>) {
                        Object obj = ((List<Object>) value).get(0);
                        sb
                                .append(key)
                                .append(QUERY_PARAMETER_KEY_VALUE_SEPARATOR)
                                .append(obj);
                    } else {
                        sb
                                .append(key)
                                .append(QUERY_PARAMETER_KEY_VALUE_SEPARATOR)
                                .append(value);
                    }
                }
            }
        }

        return sb.toString();
    }
}
