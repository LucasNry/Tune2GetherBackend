package com.t2g.app.facade;

import com.t2g.app.model.Song;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;
import com.wrapper.spotify.requests.data.tracks.GetTrackRequest;
import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SpotifyAPIFacade extends StreamingServiceFacade {
    private static final String SONG_ID_SEPARATOR = "/";

    @Autowired
    private SpotifyApi spotifyApi;

    @PostConstruct
    public void postConstruct() throws Exception {
        ClientCredentialsRequest clientCredentialsRequest = spotifyApi
                .clientCredentials()
                .build();

        ClientCredentials clientCredentials = clientCredentialsRequest.execute();
        spotifyApi.setAccessToken(clientCredentials.getAccessToken());
        //TODO: Add to credentialManager along with the time to expire of the token to manage the accessToken
    }

    @Override
    public Song getSongFromId(String id) throws Exception {
        GetTrackRequest getTrackRequest = spotifyApi
                .getTrack(id)
                .build();

        Track track = getTrackRequest.execute();
        return new Song(track);
    }

    @Override
    public List<Song> getSongFromTitle(String title) throws Exception {
        String queryString = Song
                .builder()
                .title(title)
                .build()
                .getQueryString();
        SearchTracksRequest searchTracksRequest = spotifyApi
                .searchTracks(queryString)
                .build();

        Paging<Track> tracks = searchTracksRequest.execute();
        return Arrays
                .stream(tracks.getItems())
                .map(Song::new)
                .collect(Collectors.toList());
    }

    @Override
    public Song getSongFromSongObject(Song song) throws Exception {
        String queryString = song.getQueryString();
        SearchTracksRequest searchTracksRequest = spotifyApi
                .searchTracks(queryString)
                .build();
        Paging<Track> tracks = searchTracksRequest.execute();

        return new Song(tracks.getItems()[0]);
    }

    @Override
    public String getSongIdFromURL(String serviceURL) throws MalformedURLException {
        URL url = new URL(serviceURL);
        String[] splitURL = url.getPath().split(SONG_ID_SEPARATOR);
        return splitURL[splitURL.length - 1];
    }
}
