package com.t2g.app.facade;

import com.t2g.app.dao.UserCredentialsTableDAO;
import com.t2g.app.model.DeezerSong;
import com.t2g.app.model.Playlist;
import com.t2g.app.model.Song;
import com.t2g.app.model.StreamingService;
import com.t2g.app.model.UserCredentialsTableEntry;
import com.t2g.app.util.JSONUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class DeezerAPIFacade extends StreamingServiceFacade<DeezerSong> {
    private static final String APP_ID = "518362";
    private static final String APP_SECRET_KEY = "9a65d23ba758a60924f132dd315588a3";

    private static final String BODY_PARAM_TEMPLATE = "%s=%s";
    private static final String SONG_ID_SEPARATOR = "/";
    private static final String QUERY_PARAMETER_SEPARATOR = "\\?";

    private static final String BASE_API_ADDRESS = "https://api.deezer.com";
    private static final String CONNECT_API_GET_CODE_ENDPOINT_TEMPLATE = "https://connect.deezer.com/oauth/auth.php?app_id=%s&redirect_uri=%s&perms=manage_library,offline_access";
    private static final String CONNECT_API_GET_TOKEN_ENDPOINT_TEMPLATE = "https://connect.deezer.com/oauth/access_token.php?app_id=%s&secret=%s&code=%s&output=json";

    private static final String TRACK_ENDPOINT_TEMPLATE = "/track/%s";
    private static final String PLAYLIST_ENDPOINT_TEMPLATE = "/playlist/%s";
    private static final String CREATE_PLAYLIST_ENDPOINT_TEMPLATE = "/user/me/playlists?access_token=%s";
    private static final String ADD_TO_PLAYLIST_ENDPOINT_TEMPLATE = "/playlist/%s/tracks?access_token=%s";
    private static final String SEARCH_ENDPOINT_TEMPLATE = "/search/?q=%s";

    @Autowired
    private UserCredentialsTableDAO userCredentialsTableDAO;

    public DeezerAPIFacade() {
        super(100);
    }

    @Override
    public DeezerSong getSongFromId(String id) throws Exception {
        HttpRequest getRequest = HttpRequest
                .newBuilder()
                .GET()
                .uri(URI.create(BASE_API_ADDRESS + String.format(TRACK_ENDPOINT_TEMPLATE, id)))
                .build();

        HttpResponse<String> response = sendRequest(getRequest);
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

        HttpResponse<String> response = sendRequest(getRequest);
        JSONObject jsonObject = JSONUtils.parseJSON(response.body());

        return (List<DeezerSong>) ((JSONArray) jsonObject.get("data"))
                .stream()
                .map(object -> new DeezerSong((JSONObject) object))
                .collect(Collectors.toList());
    }

    @Override
    public DeezerSong getSongFromSongObject(Song songObject) throws Exception {
        DeezerSong deezerSong = DeezerSong
                .builder()
                .title(songObject.getTitle().split("-")[0])
                .artists(songObject.getArtists())
                .build();

        HttpRequest getRequest = HttpRequest
                .newBuilder()
                .GET()
                .uri(URI.create(BASE_API_ADDRESS + String.format(SEARCH_ENDPOINT_TEMPLATE, deezerSong.getQueryString())))
                .build();

        HttpResponse<String> response = sendRequest(getRequest);

        JSONObject jsonResponse = JSONUtils.parseJSON(response.body());
        JSONArray jsonArray = (JSONArray) jsonResponse.get("data");
        JSONObject jsonObject = (JSONObject) jsonArray.get(0);

        return new DeezerSong(jsonObject);
    }

    @Override
    public String getAssetIdFromURL(String serviceURL) throws MalformedURLException {
        URL url = new URL(serviceURL);
        String[] splitURL = url.getPath().split(SONG_ID_SEPARATOR);
        return splitURL[splitURL.length - 1]
                .split(QUERY_PARAMETER_SEPARATOR)[0];
    }

    @Override
    public Playlist getPlaylist(String playlistUrl) throws Exception {
        HttpRequest getRequest = HttpRequest
                .newBuilder()
                .GET()
                .uri(URI.create(BASE_API_ADDRESS + String.format(PLAYLIST_ENDPOINT_TEMPLATE, playlistUrl)))
                .build();

        HttpResponse<String> response = sendRequest(getRequest);
        JSONObject jsonObject = JSONUtils.parseJSON(response.body());

        String title = (String) jsonObject.get("title");
        String description = (String) jsonObject.get("description");
        JSONArray tracks = (JSONArray) jsonObject.get("tracks");

        return Playlist
                .builder()
                .title(title)
                .description(description)
                .tracks(
                        (List<Song>) tracks
                                .parallelStream()
                                .map(track -> new DeezerSong((JSONObject) track))
                                .collect(Collectors.toList())
                )
                .build();
    }

    @Override
    public void createPlaylist(String userId, Playlist playlist) throws Exception {
        UserCredentialsTableEntry userCredentialsTableEntry = userCredentialsTableDAO.getByUserIdAndService(userId, StreamingService.DEEZER);

        HttpRequest createNewPlaylistRequest = HttpRequest
                .newBuilder()
                .POST(BodyPublishers.ofString(String.format(BODY_PARAM_TEMPLATE, "title", playlist.getTitle())))
                .header("Content-type", "application/x-www-form-urlencoded")
                .uri(URI.create(BASE_API_ADDRESS + String.format(CREATE_PLAYLIST_ENDPOINT_TEMPLATE, userCredentialsTableEntry.getAccessToken())))
                .build();
        HttpResponse<String> createNewPlaylistResponse = sendRequest(createNewPlaylistRequest);
        Long deezerPlaylistId = (Long) JSONUtils
                .parseJSON(createNewPlaylistResponse.body())
                .get("id");

        List<List<Song>> batches = createSongBatch(playlist.getTracks()); // This is needed because of API limitations
        for (List<Song> batch : batches) {
            String addToPlaylistRequestBody = String.format(
                    BODY_PARAM_TEMPLATE,
                    "songs",
                    String.join(",",
                            batch
                                    .stream()
                                    .map(song -> {
                                                try {
                                                    return getSongFromSongObject(song).getServiceId();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                                return null;
                                            }
                                    )
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toSet()))
            );
            HttpRequest addToPlaylistRequest = HttpRequest
                    .newBuilder()
                    .POST(BodyPublishers.ofString(addToPlaylistRequestBody))
                    .header("Content-type", "application/x-www-form-urlencoded")
                    .uri(URI.create(BASE_API_ADDRESS + String.format(ADD_TO_PLAYLIST_ENDPOINT_TEMPLATE, deezerPlaylistId, userCredentialsTableEntry.getAccessToken())))
                    .build();
            HttpResponse<String> addToPlaylistResponse = sendRequest(addToPlaylistRequest);
        }
    }

    @Override
    public String getAuthenticationURL(String userId) throws Exception {
        return String.format(CONNECT_API_GET_CODE_ENDPOINT_TEMPLATE, APP_ID, getRedirectUri(userId));
    }

    @Override
    public UserCredentialsTableEntry getUserCredentials(String userId, String code) throws Exception {
        URI uri = URI.create(
                String.format(
                        CONNECT_API_GET_TOKEN_ENDPOINT_TEMPLATE,
                        APP_ID,
                        APP_SECRET_KEY,
                        code
                )
        );

        HttpRequest getRequest = HttpRequest
                .newBuilder()
                .GET()
                .uri(uri)
                .build();

        HttpResponse<String> response = sendRequest(getRequest);
        JSONObject jsonResponse = JSONUtils.parseJSON(response.body());
        return UserCredentialsTableEntry
                .builder()
                .id(String.format("%s-%s", userId, StreamingService.DEEZER.getDomainName()))
                .accessToken((String) jsonResponse.get("access_token"))
                .build();
    }

    @Override
    public void refreshCredentials() throws Exception {
    }

    @Override
    public String getServiceDomain() {
        return StreamingService.DEEZER.getDomainName();
    }

    private HttpResponse<String> sendRequest(HttpRequest httpRequest) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        return httpClient.send(httpRequest, BodyHandlers.ofString());
    }
}
