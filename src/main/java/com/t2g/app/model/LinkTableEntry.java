package com.t2g.app.model;

import com.t2g.app.annotations.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
@Builder
public class LinkTableEntry extends FakeDBEntry {

    public static final String UNIVERSAL_ID = "universalId";
    private static final String SPOTIFY_ID = "spotifyId";
    private static final String YOUTUBE_ID = "youtubeId";
    private static final String DEEZER_ID = "deezerId";
    private static final String URL = "url";

    @JSONField(UNIVERSAL_ID)
    @Builder.Default
    private String universalId = "";

    @JSONField(SPOTIFY_ID)
    @Builder.Default
    private String spotifyId = "";

    @JSONField(YOUTUBE_ID)
    @Builder.Default
    private String youtubeId = "";

    @JSONField(DEEZER_ID)
    @Builder.Default
    private String deezerId = "";

    @JSONField(URL)
    @Builder.Default
    private Map<String, String> url = new HashMap<>();

    public LinkTableEntry(Map<String, String> songIdByServiceDomain, Map<String, String> url) {
        this.universalId = makeId(songIdByServiceDomain);
        this.spotifyId = songIdByServiceDomain.getOrDefault(StreamingService.SPOTIFY.getDomainName(), "");
        this.youtubeId = songIdByServiceDomain.getOrDefault(StreamingService.YOUTUBE.getDomainName(), "");
        this.deezerId = songIdByServiceDomain.getOrDefault(StreamingService.DEEZER.getDomainName(), "");
        this.url = url;
    }

    public LinkTableEntry(JSONObject jsonEntry) {
        this.universalId = (String) jsonEntry.get(UNIVERSAL_ID);
        this.spotifyId = (String) jsonEntry.get(SPOTIFY_ID);
        this.youtubeId = (String) jsonEntry.get(YOUTUBE_ID);
        this.deezerId = (String) jsonEntry.get(DEEZER_ID);
        this.url = new HashMap<String, String>((JSONObject) jsonEntry.get(URL));
    }

    private String makeId(Map<String, String> songIdByServiceDomain) {
        return songIdByServiceDomain.getOrDefault(StreamingService.SPOTIFY.getDomainName(), "") +
                songIdByServiceDomain.getOrDefault(StreamingService.YOUTUBE.getDomainName(), "") +
                songIdByServiceDomain.getOrDefault(StreamingService.DEEZER.getDomainName(), "");
    }
}
