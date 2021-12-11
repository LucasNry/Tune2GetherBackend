package com.t2g.app.facade;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.t2g.app.model.Song;
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
    private static final String KEY = "&key=AIzaSyBXbztZDjnuO4h1VLo76WS0ca-jNeL49s4";

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private static final String API_BASE_URL = "https://youtube.googleapis.com/youtube/v3/";

    @Override
    public YTSong getSongFromId(String id) throws Exception {
        final String url = "videos?part=snippet&id=";

        JsonObject jsonResponse = getJsonResponse(id, url);

        JsonObject trackInfo = jsonResponse
                .getAsJsonArray("items")
                .get(0)
                .getAsJsonObject();

        return new YTSong(trackInfo);
    }

    @Override
    public List<YTSong> getSongFromTitle(String title) throws Exception {
        List<YTSong> songArrayList = new ArrayList<>();
        final String url = "search?part=snippet&maxResults=5&type=video&videoCategoryId=10&q=";

        JsonObject jsonResponse = getJsonResponse(title, url);

        JsonArray songs = jsonResponse.getAsJsonArray("items");

        for (JsonElement song: songs) {
            JsonObject trackInfo = song.getAsJsonObject();
            songArrayList.add(new YTSong(trackInfo));
        }

        return songArrayList;
    }

    @Override
    public YTSong getSongFromSongObject(Song object) throws Exception {
        String title = object.getTitle();
        String artist = object.getArtists().get(0);

        String song = title + '+' + artist;

        return getSongFromTitle(song).get(0);
    }

    @Override
    public String getSongIdFromURL(String serviceURL) throws Exception {
        return serviceURL.split("v=")[1];
    }

    @Override
    public void refreshCredentials() throws Exception {

    }

    private JsonObject getJsonResponse(String id, String url) throws java.io.IOException, InterruptedException {
        String urlRequest = API_BASE_URL + url + id + KEY;

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(urlRequest))
                .header("Accept", "application/json")
                .build();

        var response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        return JsonParser
                .parseString(response.body())
                .getAsJsonObject();
    }
}
