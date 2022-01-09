package com.t2g.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class Playlist {
    private String title;

    private String description;

    private List<Song> tracks;
}
