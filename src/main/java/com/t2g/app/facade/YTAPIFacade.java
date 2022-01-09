package com.t2g.app.facade;

import com.google.gson.*;
import com.t2g.app.dao.UserCredentialsTableDAO;
import com.t2g.app.model.Playlist;
import com.t2g.app.model.Song;
import com.t2g.app.model.StreamingService;
import com.t2g.app.model.UserCredentialsTableEntry;
import com.t2g.app.model.YTSong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class YTAPIFacade extends StreamingServiceFacade<YTSong>{
    private static final String API_KEY = "&key=AIzaSyBXbztZDjnuO4h1VLo76WS0ca-jNeL49s4";
    private static final String API_BASE_URL = "https://youtube.googleapis.com/youtube/v3/";
    private static final String SEARCH_ENDPOINT_TEMPLATE = "search?part=snippet&maxResults=5&type=video&videoCategoryId=10&q=%s";
    private static final String VIDEO_BY_ID_ENDPOINT_TEMPLATE = "videos?part=snippet&id=%s";
    private static final String PLAYLIST_BY_ID_ENDPOINT_TEMPLATE = "playlists?part=snippet%2CcontentDetails&id=%s";
    private static final String PLAYLIST_CONTENT_BY_ID_ENDPOINT_TEMPLATE = "playlistItems?part=snippet%2CcontentDetails&maxResults=50&playlistId=%s";
    private static final String PLAYLIST_CREATE_ENDPOINT_TEMPLATE = "playlists?part=snippet";
    private static final String PLAYLIST_CREATE_CONTENT_ENDPOINT_TEMPLATE = "playlistItems?part=snippet";

    @Autowired
    private UserCredentialsTableDAO userCredentialsTableDAO;

    public YTAPIFacade() {
        super(100);
    }

    @Override
    public YTSong getSongFromId(String id) throws Exception {
        String endpoint = String.format(VIDEO_BY_ID_ENDPOINT_TEMPLATE, id);
        JsonObject jsonResponse = getJsonResponse(endpoint);

        JsonObject trackInfo = jsonResponse
                .getAsJsonArray("items")
                .get(0)
                .getAsJsonObject();

        return new YTSong(trackInfo);
    }

    @Override
    public List<YTSong> getSongFromTitle(String title) throws Exception {
        List<YTSong> songArrayList = new ArrayList<>();
        YTSong ytSong = YTSong
                .builder()
                .title(title)
                .build();

        String endpoint = String.format(SEARCH_ENDPOINT_TEMPLATE, ytSong.getQueryString());
        JsonObject jsonResponse = getJsonResponse(endpoint);

        JsonArray songs = jsonResponse.getAsJsonArray("items");
        for (JsonElement song: songs) {
            JsonObject trackInfo = song.getAsJsonObject();
            songArrayList.add(new YTSong(trackInfo));
        }

        return songArrayList;
    }

    @Override
    public YTSong getSongFromSongObject(Song songObject) throws Exception {
        YTSong ytSong = YTSong
                .builder()
                .title(songObject.getTitle().split("-")[0])
                .artists(songObject.getArtists())
                .build();

        String endpoint = String.format(SEARCH_ENDPOINT_TEMPLATE, ytSong.getQueryString());
        JsonObject jsonResponse = getJsonResponse(endpoint);

        JsonObject trackInfo = jsonResponse
                .getAsJsonArray("items")
                .get(0)
                .getAsJsonObject();
        return new YTSong(trackInfo);
    }

    @Override
    public String getAssetIdFromURL(String serviceURL) throws Exception {
        return serviceURL.split("v=")[1];
    }

    @Override
    public Playlist getPlaylist(String playlistUrl) throws Exception {
        String listId = playlistUrl.split("list=")[1];

        String endpoint = String.format(PLAYLIST_BY_ID_ENDPOINT_TEMPLATE, listId);
        JsonObject jsonResponse = getJsonResponse(endpoint);

        JsonObject playlistInfo = jsonResponse
                .getAsJsonArray("items")
                .get(0)
                .getAsJsonObject()
                .getAsJsonObject("snippet");

        String playlistTitle = playlistInfo
                .getAsJsonPrimitive("title")
                .getAsString();

        String playlistDescription = playlistInfo
                .getAsJsonPrimitive("description")
                .getAsString();

        return Playlist.builder()
                .title(playlistTitle)
                .description(playlistDescription)
                .tracks(getPlaylistContent(listId))
                .build();
    }

    @Override
    public void createPlaylist(String userId, Playlist playlist) throws Exception {
        UserCredentialsTableEntry userCredentialsTableEntry = userCredentialsTableDAO.
                getByUserIdAndService(userId, StreamingService.YOUTUBE);

        String body = String
                .format("{\"snippet\": {\"title\": \"%s\",\"description\": \"%s\"}}",
                        playlist.getTitle(),
                        playlist.getDescription()
                );

        JsonObject createdList = this.postContent(
                PLAYLIST_CREATE_ENDPOINT_TEMPLATE,
                body,
                userCredentialsTableEntry.getAccessToken()
        );

        String createdListId = createdList
                .getAsJsonPrimitive("id")
                .getAsString();

        this.createPlaylistContent(createdListId, playlist.getTracks(), userCredentialsTableEntry.getAccessToken());
    }

    @Override
    public String getAuthenticationURL(String userId) {
        return String.format(
                "http://localhost:8080/redirect/%s/%s",
                this.getServiceDomain(),
                userId
        );
    }

    @Override
    public UserCredentialsTableEntry getUserCredentials(String userId, String code) throws Exception {
        String urlRequest = "https://oauth2.googleapis.com/token";

        String url = "code=%s&" +
                "client_id=837535089878-9d7cck9l04pu9r23r0r06tt9lh2hsquv.apps.googleusercontent.com&" +
                "client_secret=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&" +
                "redirect_uri=http%3A//localhost/redirect/youtube&" +
                "grant_type=authorization_code";

        String body = String.format(url, code);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlRequest))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        JsonObject object = JsonParser
                .parseString(response.body())
                .getAsJsonObject();

        String accessToken = object
                .getAsJsonPrimitive("access_token")
                .getAsString();

        String refreshToken = object
                .getAsJsonPrimitive("refresh_token")
                .getAsString();

        return new UserCredentialsTableEntry(userId, accessToken, refreshToken);
    }

    @Override
    public void refreshCredentials() {
    }

    @Override
    public String getServiceDomain() {
        return StreamingService.YOUTUBE.getDomainName();
    }


    private JsonObject getJsonResponse(String endpoint) throws java.io.IOException, InterruptedException {
        String urlRequest = API_BASE_URL + endpoint + API_KEY;

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(urlRequest))
                .header("Accept", "application/json")
                .build();

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return JsonParser
                .parseString(response.body())
                .getAsJsonObject();
    }


    private JsonObject postContent(String endpoint, String body, String token) throws java.io.IOException, InterruptedException {
        String urlRequest = API_BASE_URL + endpoint + API_KEY;

        String bearerToken = String.format("Bearer %s", token);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlRequest))
                .header("Authorization", bearerToken)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return JsonParser
                .parseString(response.body())
                .getAsJsonObject();
    }

    private List<Song> getPlaylistContent(String listId) throws Exception {
        List<Song> playlistSongs = new ArrayList<>();

        String endpoint = String.format(PLAYLIST_CONTENT_BY_ID_ENDPOINT_TEMPLATE, listId);
        JsonObject jsonResponse = getJsonResponse(endpoint);

        JsonArray playlistContent = jsonResponse
                .getAsJsonArray("items");

        for (int i = 0; i < playlistContent.size(); i++) {
            playlistSongs.add(
                    this.getSongFromId(
                            playlistContent
                                    .get(i)
                                    .getAsJsonObject()
                                    .getAsJsonObject("contentDetails")
                                    .getAsJsonPrimitive("videoId")
                                    .getAsString()
                    )
            );
        }

        return playlistSongs;
    }

    private void createPlaylistContent(String listId, List<Song> songs, String token) throws Exception {
        for (Song song: songs) {
            String body = String.format(
                    "{\"snippet\": {\"playlistId\": \"%s\",\"resourceId\": {\"kind\": \"youtube#video\",\"videoId\": \"%s\"}}}",
                    listId,
                    this.getAssetIdFromURL(this.getSongFromSongObject(song).getUrl())
            );

            this.postContent(PLAYLIST_CREATE_CONTENT_ENDPOINT_TEMPLATE, body, token);
        }
    }
}
