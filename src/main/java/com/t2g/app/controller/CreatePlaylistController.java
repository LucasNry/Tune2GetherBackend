package com.t2g.app.controller;

import com.t2g.app.dao.UserTableDAO;
import com.t2g.app.facade.StreamingServiceFacade;
import com.t2g.app.facade.StreamingServiceFacadeFactory;
import com.t2g.app.model.Playlist;
import com.t2g.app.model.StreamingService;
import com.t2g.app.model.UserTableEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@Component
@RestController
public class CreatePlaylistController {

    @Autowired
    private StreamingServiceFacadeFactory streamingServiceFacadeFactory;

    @Autowired
    private UserTableDAO userTableDAO;

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @GetMapping("/playlist")
    public ResponseEntity<String> replicatePlaylist(
            @CookieValue(name = "user_id", defaultValue = "") String userId,
            @RequestParam("l") String playlistUrl
    ) throws Exception {
        StreamingService streamingService = StreamingService.getServiceFromUrl(playlistUrl);
        StreamingServiceFacade streamingServiceFacade = streamingServiceFacadeFactory.getStreamingServiceFacade(streamingService);
        Playlist playlist = streamingServiceFacade.getPlaylist(playlistUrl);

        UserTableEntry userTableEntry = userTableDAO.getByPrimaryKey(userId);
        StreamingServiceFacade userPreferredStreamingServiceFacade = streamingServiceFacadeFactory.getStreamingServiceFacade(userTableEntry.getPreferredService());
        userPreferredStreamingServiceFacade.createPlaylist(userId, playlist);

        return ResponseEntity
                .ok()
                .build();
    }
}
