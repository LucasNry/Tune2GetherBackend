package com.t2g.app.model;

import com.t2g.app.annotations.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

/*
Table schema:
{
    id : * Song id from Spotify + Song id from Youtube + Song id from Deezer *,
    spotifyId: * Song id from Spotify *,
    youtubeId: * Song id from Youtube *,
    deezerId: * Song id from Deezer *,
    url :  {
        spotify : * Spotify url *
        youtube : * Youtube url *
        deezer : * Deezer url *
    }
}
*/
@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
public class LinkTableEntry extends FakeDBEntry<String> {

    private static final String SPOTIFY_ID = "spotifyId";
    private static final String YOUTUBE_ID = "youtubeId";
    private static final String DEEZER_ID = "deezerId";
    private static final String URL = "url";

    @JSONField(SPOTIFY_ID)
    private String spotifyId;

    @JSONField(YOUTUBE_ID)
    private String youtubeId;

    @JSONField(DEEZER_ID)
    private String deezerId;

    @JSONField(URL)
    private Map<String, String> url;

    @Builder
    public LinkTableEntry(
            String universalId,
            String spotifyId,
            String youtubeId,
            String deezerId,
            Map<String, String> url
    ) {
        super(universalId);
        this.spotifyId = spotifyId;
        this.youtubeId = youtubeId;
        this.deezerId = deezerId;
        this.url = url;

    }

    public LinkTableEntry(Map<String, String> songIdByServiceDomain, Map<String, String> url) {
        super(makeId(songIdByServiceDomain));
        this.spotifyId = songIdByServiceDomain.getOrDefault(StreamingService.SPOTIFY.getDomainName(), "");
        this.youtubeId = songIdByServiceDomain.getOrDefault(StreamingService.YOUTUBE.getDomainName(), "");
        this.deezerId = songIdByServiceDomain.getOrDefault(StreamingService.DEEZER.getDomainName(), "");
        this.url = url;
    }

    public LinkTableEntry(JSONObject jsonEntry) {
        super((String) jsonEntry.get(PRIMARY_KEY));
        this.spotifyId = (String) jsonEntry.get(SPOTIFY_ID);
        this.youtubeId = (String) jsonEntry.get(YOUTUBE_ID);
        this.deezerId = (String) jsonEntry.get(DEEZER_ID);
        this.url = new HashMap<String, String>((JSONObject) jsonEntry.get(URL));
    }

    private static String makeId(Map<String, String> songIdByServiceDomain) {
        return songIdByServiceDomain.getOrDefault(StreamingService.SPOTIFY.getDomainName(), "") +
                songIdByServiceDomain.getOrDefault(StreamingService.YOUTUBE.getDomainName(), "") +
                songIdByServiceDomain.getOrDefault(StreamingService.DEEZER.getDomainName(), "");
    }
}
