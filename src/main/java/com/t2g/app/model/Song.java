package com.t2g.app.model;

import lombok.Data;

@Data
public class Song {
    private String title;

    private String description;

    private String artist;

    private String album;

    private String coverSrcUrl;

    private String uri;
}
