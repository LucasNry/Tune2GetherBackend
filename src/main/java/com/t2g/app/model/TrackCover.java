package com.t2g.app.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrackCover {
    private String url;

    private int height;

    private int width;
}
