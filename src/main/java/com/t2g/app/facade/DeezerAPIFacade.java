package com.t2g.app.facade;

import com.t2g.app.model.DeezerSong;
import com.t2g.app.model.Song;
import com.t2g.app.util.JSONUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DeezerAPIFacade extends StreamingServiceFacade<DeezerSong> {
    private static final String SONG_ID_SEPARATOR = "/";
    private static final String QUERY_PARAMETER_SEPARATOR = "\\?";

    private static final String BASE_API_ADDRESS = "https://api.deezer.com";

    private static final String TRACK_ENDPOINT_TEMPLATE = "/track/%s";
    private static final String SEARCH_ENDPOINT_TEMPLATE = "/search/?q=%s";

    private HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public DeezerSong getSongFromId(String id) throws Exception {
        HttpRequest getRequest = HttpRequest
                .newBuilder()
                .GET()
                .uri(URI.create(BASE_API_ADDRESS + String.format(TRACK_ENDPOINT_TEMPLATE, id)))
                .build();

        HttpResponse<String> response = httpClient.send(getRequest, BodyHandlers.ofString());
        JSONObject jsonObject = JSONUtils.parseJSON(response.body());

        return new DeezerSong(jsonObject);
    }

    @Override
    public List<DeezerSong> getSongFromTitle(String title) throws Exception {
        DeezerSong deezerSong = DeezerSong
                .builder()
                .title(title)
                .build();
        HttpRequest getRequest = HttpRequest
                .newBuilder()
                .GET()
                .uri(URI.create(BASE_API_ADDRESS + String.format(SEARCH_ENDPOINT_TEMPLATE, deezerSong.getQueryString())))
                .build();

        HttpResponse<String> response = httpClient.send(getRequest, BodyHandlers.ofString());
        JSONObject jsonObject = JSONUtils.parseJSON(response.body());

        return (List<DeezerSong>) ((JSONArray) jsonObject.get("data"))
                .stream()
                .map(object -> new DeezerSong((JSONObject) object))
                .collect(Collectors.toList());
    }

    @Override
    public DeezerSong getSongFromSongObject(Song object) throws Exception {
        DeezerSong deezerSong = DeezerSong
                .builder()
                .title(object.getTitle())
                .artists(object.getArtists())
                .build();

        HttpRequest getRequest = HttpRequest
                .newBuilder()
                .GET()
                .uri(URI.create(BASE_API_ADDRESS + String.format(SEARCH_ENDPOINT_TEMPLATE, deezerSong.getQueryString())))
                .build();

        HttpResponse<String> response = httpClient.send(getRequest, BodyHandlers.ofString());

        JSONObject jsonResponse = JSONUtils.parseJSON(response.body());
        JSONArray jsonArray = (JSONArray) jsonResponse.get("data");
        JSONObject jsonObject = (JSONObject) jsonArray.get(0);

        return new DeezerSong(jsonObject);
    }

    @Override
    public String getSongIdFromURL(String serviceURL) throws MalformedURLException {
        URL url = new URL(serviceURL);
        String[] splitURL = url.getPath().split(SONG_ID_SEPARATOR);
        return splitURL[splitURL.length - 1]
                .split(QUERY_PARAMETER_SEPARATOR)[0];
    }

    @Override
    public void refreshCredentials() throws Exception {

    }
}
