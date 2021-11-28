package com.t2g.app.facade;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.t2g.app.model.Song;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class YTMusicAPIFacade extends StreamingServiceFacade{
    private static final String KEY = "&key=AIzaSyBXbztZDjnuO4h1VLo76WS0ca-jNeL49s4";

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private static final String APIBASEURL = "https://youtube.googleapis.com/youtube/v3/";

    @Override
    public Song getSongFromId(String id) throws Exception {
        String url = "videos?part=snippet&id=";

        JsonObject jsonResponse = getJsonResponse(id, url);

        JsonObject trackInfo = jsonResponse
                .getAsJsonArray("items")
                .get(0)
                .getAsJsonObject();

        return new Song(trackInfo);
    }

    @Override
    public List<Song> getSongFromTitle(String title) throws Exception {
        List<Song> songArrayList = new ArrayList<>();
        String url = "search?part=snippet&maxResults=1&type=video&videoCategoryId=10&q=";

        JsonObject jsonResponse = getJsonResponse(title, url);

        JsonArray songs = jsonResponse.getAsJsonArray("items");

        for (JsonElement song: songs) {
            JsonObject trackInfo = song.getAsJsonObject();
            songArrayList.add(new Song(trackInfo));
        }

        return songArrayList;
    }

    @Override
    public Song getSongFromSongObject(Song object) throws Exception {
        String title = object.getTitle();

        return getSongFromTitle(title).get(0);
    }

    @Override
    public String getSongIdFromURL(String serviceURL) throws Exception {
        return serviceURL.split("v=")[1];
    }

    @Override
    public void refreshCredentials() throws Exception {

    }

    private JsonObject getJsonResponse(String id, String url) throws java.io.IOException, InterruptedException {
        String urlRequest = APIBASEURL + url + id + KEY;

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
