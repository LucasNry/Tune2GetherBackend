package com.t2g.app.model;

import com.google.gson.JsonObject;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
public class YTSong extends Song {
    private static final String QUERY_PARAMETER_SEPARATOR = "+";
    private static final String ENCODED_SPACE_CHAR = "%20";
    private static final String YT_INTERNAL_CLASSIFICATION = "- Topic";
    public static final String YOUTUBE_VIDEO_URL_TEMPLATE = "https://www.youtube.com/watch?v=%s";

    @Builder
    public YTSong(String title, List<String> artists, String album, List<TrackCover> images, String uri) {
        super(null, title, artists, album, images, uri, QUERY_PARAMETER_SEPARATOR);
    }

    public YTSong(JsonObject trackInfo) {
        super(
                trackInfo
                        .getAsJsonObject("id")
                        .getAsJsonPrimitive("videoId")
                        .getAsString(),
                trackInfo.getAsJsonObject("snippet")
                        .getAsJsonPrimitive("title")
                        .getAsString(),
                Collections.singletonList(
                        trackInfo.getAsJsonObject("snippet")
                                .getAsJsonPrimitive("channelTitle")
                                .getAsString()
                                .replace(YT_INTERNAL_CLASSIFICATION, "")
                ),
                "",
                Collections.singletonList(
                        TrackCover
                                .builder()
                                .url(trackInfo.getAsJsonObject("snippet")
                                        .getAsJsonObject("thumbnails")
                                        .getAsJsonObject("high")
                                        .getAsJsonPrimitive("url")
                                        .getAsString())
                                .height(trackInfo.getAsJsonObject("snippet")
                                        .getAsJsonObject("thumbnails")
                                        .getAsJsonObject("high")
                                        .getAsJsonPrimitive("height")
                                        .getAsInt())
                                .width(trackInfo.getAsJsonObject("snippet")
                                        .getAsJsonObject("thumbnails")
                                        .getAsJsonObject("high")
                                        .getAsJsonPrimitive("width")
                                        .getAsInt())
                                .build()
                ),
                getVideoId(trackInfo),
                QUERY_PARAMETER_SEPARATOR
        );
    }

    @Override
    public String serializeField(String key, Object value) {
        return (String) value;
    }

    @Override
    public String serializeCollection(String key, Collection<?> value) {
        Object obj = ((List<Object>) value).get(0);
        return serializeField(key, obj);
    }

    @Override
    public String getQueryString() throws Exception {
        return super.getQueryString().replaceAll(" ", ENCODED_SPACE_CHAR);
    }

    private static String getVideoId(JsonObject trackInfo) {
        try {
            return String.format(
                    YOUTUBE_VIDEO_URL_TEMPLATE,
                    trackInfo
                            .getAsJsonPrimitive("id")
                            .getAsString()
            );
        } catch (Exception e) {
            return String.format(
                    YOUTUBE_VIDEO_URL_TEMPLATE,
                    trackInfo
                            .getAsJsonObject("id")
                            .getAsJsonPrimitive("videoId")
                            .getAsString()
            );
        }
    }
}
