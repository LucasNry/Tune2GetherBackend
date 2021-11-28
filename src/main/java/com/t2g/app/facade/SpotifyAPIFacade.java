package com.t2g.app.facade;

import com.t2g.app.manager.CredentialManager;
import com.t2g.app.model.Song;
import com.t2g.app.model.SpotifySong;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;
import com.wrapper.spotify.requests.data.tracks.GetTrackRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class SpotifyAPIFacade extends StreamingServiceFacade {
    private static final String SONG_ID_SEPARATOR = "/";

    @Autowired
    private SpotifyApi spotifyApi;

    @Autowired
    private CredentialManager credentialManager;

    @PostConstruct
    public void postConstruct() throws Exception {
        refreshCredentials();
    }

    @Override
    public Song getSongFromId(String id) throws Exception {
        GetTrackRequest getTrackRequest = spotifyApi
                .getTrack(id)
                .build();

        Track track = getTrackRequest.execute();
        return new SpotifySong(track);
    }

    @Override
    public List<Song> getSongFromTitle(String title) throws Exception {
        String queryString = SpotifySong
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
                .map(SpotifySong::new)
                .collect(Collectors.toList());
    }

    @Override
    public Song getSongFromSongObject(Song song) throws Exception {
        SpotifySong spotifySong = SpotifySong
                .builder()
                .title(song.getTitle())
                .artists(song.getArtists())
                .build();

        SearchTracksRequest searchTracksRequest = spotifyApi
                .searchTracks(spotifySong.getQueryString())
                .build();
        Paging<Track> tracks = searchTracksRequest.execute();

        return new SpotifySong(tracks.getItems()[0]);
    }

    @Override
    public String getSongIdFromURL(String serviceURL) throws MalformedURLException {
        URL url = new URL(serviceURL);
        String[] splitURL = url.getPath().split(SONG_ID_SEPARATOR);
        return splitURL[splitURL.length - 1];
    }

    @Override
    public void refreshCredentials() throws Exception {
        // This method will need to be refactored once we start making requests for users
        ClientCredentialsRequest clientCredentialsRequest = spotifyApi
                .clientCredentials()
                .build();

        ClientCredentials clientCredentials = clientCredentialsRequest.execute();
        spotifyApi.setAccessToken(clientCredentials.getAccessToken());

        int timeToExpireInSeconds = clientCredentials.getExpiresIn();
        credentialManager.addToQueue(
                this,
                TimeUnit.MILLISECONDS.convert(timeToExpireInSeconds, TimeUnit.SECONDS)
        );
    }
}
