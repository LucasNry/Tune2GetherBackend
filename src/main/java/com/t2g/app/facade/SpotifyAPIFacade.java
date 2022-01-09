package com.t2g.app.facade;

import com.t2g.app.dao.UserCredentialsTableDAO;
import com.t2g.app.manager.CredentialManager;
import com.t2g.app.model.Playlist;
import com.t2g.app.model.Song;
import com.t2g.app.model.SpotifySong;
import com.t2g.app.model.StreamingService;
import com.t2g.app.model.UserCredentialsTableEntry;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.IPlaylistItem;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.model_objects.specification.User;
import com.wrapper.spotify.requests.AbstractRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.playlists.AddItemsToPlaylistRequest;
import com.wrapper.spotify.requests.data.playlists.CreatePlaylistRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistsItemsRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;
import com.wrapper.spotify.requests.data.tracks.GetTrackRequest;
import com.wrapper.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class SpotifyAPIFacade extends StreamingServiceFacade {
    private static final String ID_SEPARATOR = "/";
    private static final String SPOTIFY_URI_TEMPLATE = "spotify:track:%s";

    @Autowired
    private SpotifyApi spotifyApi;

    @Autowired
    private CredentialManager credentialManager;

    @Autowired
    private UserCredentialsTableDAO userCredentialsTableDAO;

    public SpotifyAPIFacade() {
        super(100);
    }

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
    public Song getSongFromSongObject(Song songObject) throws Exception {
        SpotifySong spotifySong = SpotifySong
                .builder()
                .title(songObject.getTitle().split("-")[0])
                .artists(songObject.getArtists())
                .build();

        SearchTracksRequest searchTracksRequest = spotifyApi
                .searchTracks(spotifySong.getQueryString())
                .build();
        Paging<Track> tracks = searchTracksRequest.execute();

        return new SpotifySong(tracks.getItems()[0]);
    }

    @Override
    public String getAssetIdFromURL(String serviceURL) throws MalformedURLException {
        URL url = new URL(serviceURL);
        String[] splitURL = url.getPath().split(ID_SEPARATOR);
        return splitURL[splitURL.length - 1];
    }

    @Override
    public Playlist getPlaylist(String playlistUrl) throws Exception {
        String spotifyPlaylistId = getAssetIdFromURL(playlistUrl);
        GetPlaylistRequest getPlaylistRequest = spotifyApi
                .getPlaylist(spotifyPlaylistId)
                .build();
        com.wrapper.spotify.model_objects.specification.Playlist spotifyPlaylist = getPlaylistRequest.execute();
        List<PlaylistTrack> tracks = new ArrayList<>(Arrays.asList(spotifyPlaylist.getTracks().getItems()));
        if (spotifyPlaylist.getTracks().getLimit() < spotifyPlaylist.getTracks().getTotal()) {
            boolean haveAllSongsBeenFetched = false;
            while (!haveAllSongsBeenFetched) {
                GetPlaylistsItemsRequest getPlaylistsItemsRequest = spotifyApi
                        .getPlaylistsItems(spotifyPlaylistId)
                        .offset(tracks.size())
                        .build();
                Paging<PlaylistTrack> playlistTrackPaging = getPlaylistsItemsRequest.execute();
                tracks.addAll(Arrays.asList(playlistTrackPaging.getItems()));

                haveAllSongsBeenFetched = playlistTrackPaging.getNext() == null;
            }
        }

        return Playlist
                .builder()
                .title(spotifyPlaylist.getName())
                .description(spotifyPlaylist.getDescription())
                .tracks(getSongsFromSpotifyPlaylist(tracks))
                .build();
    }

    @Override
    public void createPlaylist(String userId, Playlist playlist) {
        GetCurrentUsersProfileRequest getCurrentUsersProfileRequest = spotifyApi
                .getCurrentUsersProfile()
                .build();
        User userInfo = (User) sendUserRequest(userId, getCurrentUsersProfileRequest);

        CreatePlaylistRequest createPlaylistRequest = spotifyApi
                .createPlaylist(userInfo.getId(), playlist.getTitle())
                .build();
        com.wrapper.spotify.model_objects.specification.Playlist spotifyPlaylist =
                (com.wrapper.spotify.model_objects.specification.Playlist) sendUserRequest(userId, createPlaylistRequest);
        String playlistId = spotifyPlaylist.getId();

        List<Song> tracks = playlist.getTracks();
        List<List<Song>> batches = createSongBatch(tracks); // This is needed because of API limitations
        for (List<Song> batch : batches) {
            String[] uris = (String[]) batch
                    .stream()
                    .map(song -> {
                        try {
                            return String.format(SPOTIFY_URI_TEMPLATE, getSongFromSongObject(song).getServiceId());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return null;
                    }).toArray();

            AddItemsToPlaylistRequest addItemsToPlaylistRequest = spotifyApi
                    .addItemsToPlaylist(playlistId, uris)
                    .build();
            sendUserRequest(userId, addItemsToPlaylistRequest);
        }
    }

    @Override
    public String getAuthenticationURL(String userId) {
        AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi
                .authorizationCodeUri()
                .scope("playlist-modify-private playlist-modify-public")
                .redirect_uri(URI.create(getRedirectUri(userId)))
                .build();

        return authorizationCodeUriRequest
                .execute()
                .toString();
    }

    @Override
    public UserCredentialsTableEntry getUserCredentials(String userId, String code) throws Exception {
        AuthorizationCodeRequest authorizationCodeRequest = spotifyApi
                .authorizationCode(code)
                .build();
        AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRequest.execute();

        return UserCredentialsTableEntry
                .builder()
                .id(String.format("%s-%s", userId, StreamingService.SPOTIFY.getDomainName()))
                .accessToken(authorizationCodeCredentials.getAccessToken())
                .refreshToken(authorizationCodeCredentials.getRefreshToken())
                .build();
    }

    @Override
    public void refreshCredentials() throws Exception {
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

    private Object sendUserRequest(String userId, AbstractRequest request) {
        UserCredentialsTableEntry userCredentialsTableEntry = userCredentialsTableDAO.getByUserIdAndService(userId, StreamingService.SPOTIFY);
        String previousAccessToken = spotifyApi.getAccessToken();
        Object response = null;

        try {
            UserCredentialsTableEntry newUserCredentials = getUserCredentials(userCredentialsTableEntry.getPrimaryKey(), userCredentialsTableEntry.getRefreshToken());
            userCredentialsTableDAO.updateEntry(newUserCredentials);

            spotifyApi.setAccessToken(newUserCredentials.getAccessToken());
            response = request.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            spotifyApi.setAccessToken(previousAccessToken);
        }

        return response;
    }

    private List<Song> getSongsFromSpotifyPlaylist(List<PlaylistTrack> spotifyPlaylist) throws Exception {
        return spotifyPlaylist
                .stream()
                .map(playlistTrack -> {
                    IPlaylistItem iPlaylistItem = playlistTrack.getTrack();

                    String spotifySongId = iPlaylistItem.getId();
                    try {
                        return getSongFromId(spotifySongId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public String getServiceDomain() {
        return StreamingService.SPOTIFY.getDomainName();
    }
}
