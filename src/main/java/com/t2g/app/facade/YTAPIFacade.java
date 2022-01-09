package com.t2g.app.facade;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.t2g.app.model.Playlist;
import com.t2g.app.model.Song;
import com.t2g.app.model.StreamingService;
import com.t2g.app.model.UserCredentialsTableEntry;
import com.t2g.app.model.YTSong;
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
    public Playlist getPlaylist(String playlistUrl) {
        return null;
    }

    @Override
    public void createPlaylist(String userId, Playlist playlist) {

    }

    @Override
    public String getAuthenticationURL(String userId) throws Exception {
        return null;
    }

    @Override
    public UserCredentialsTableEntry getUserCredentials(String userId, String code) throws Exception {
        return null;
    }

    @Override
    public void refreshCredentials() throws Exception {
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
}
