package com.t2g.app.controller;

import com.t2g.app.exception.LinkNotFoundException;
import com.t2g.app.dao.LinkTableDAO;
import com.t2g.app.facade.StreamingServiceFacade;
import com.t2g.app.facade.StreamingServiceFacadeFactory;
import com.t2g.app.model.LinkTableEntry;
import com.t2g.app.model.Song;
import com.t2g.app.model.StreamingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class GetMusicController {
    @Autowired
    private StreamingServiceFacadeFactory streamingServiceFacadeFactory;

    @Autowired
    private LinkTableDAO linkTableDAO;

    @CrossOrigin(origins = "*")
    @GetMapping("/link")
    public Map<String, String> getUniversalLink(@RequestParam("id") String id) throws Exception {
        Optional<LinkTableEntry> linkTableEntry = Optional.ofNullable(linkTableDAO.getByPrimaryKey(id));

        if (!linkTableEntry.isPresent()) {
            throw new LinkNotFoundException();
        }

        return linkTableEntry
                .get()
                .getUrl();
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/music")
    public Map<String, String> getMusic(@RequestParam("l") String sanitizedUrl) throws Exception {
        Map<String, String> songIdByServiceDomain = new HashMap<>();
        Map<String, String> songURLsByDomainName = new HashMap<>();

        StreamingService streamingService = StreamingService.getServiceFromUrl(sanitizedUrl);
        StreamingServiceFacade originServiceFacade = streamingServiceFacadeFactory.getStreamingServiceFacade(streamingService);

        String originSongId = originServiceFacade.getAssetIdFromURL(sanitizedUrl);
        LinkTableEntry existingLink = linkTableDAO.getLinkByServiceId(originSongId, streamingService);
        if (
                existingLink !=  null &&
                existingLink.getUrl().size() == StreamingService.values().length
        ) {
            Map<String, String> response = new HashMap<>();
            response.put("id", existingLink.getPrimaryKey());

            return response;
        }

        Song requestedSong = getSongInformation(originServiceFacade, sanitizedUrl);
        songIdByServiceDomain.put(streamingService.getDomainName(), originSongId);
        songURLsByDomainName.put(streamingService.getDomainName(), requestedSong.getUrl());

        for (StreamingService service : StreamingService.values()) {
            if (service != streamingService) {
                try {
                    StreamingServiceFacade streamingServiceFacade = streamingServiceFacadeFactory.getStreamingServiceFacade(service);
                    Song song = streamingServiceFacade.getSongFromSongObject(requestedSong);

                    String songUrl = song.getUrl();
                    songIdByServiceDomain.put(service.getDomainName(), streamingServiceFacade.getAssetIdFromURL(songUrl));
                    songURLsByDomainName.put(service.getDomainName(), songUrl);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        LinkTableEntry linkTableEntry = new LinkTableEntry(songIdByServiceDomain, songURLsByDomainName);
        linkTableDAO.addEntry(linkTableEntry);

        Map<String, String> response = new HashMap<>();
        response.put("id", linkTableEntry.getPrimaryKey());

        return response;
    }

    private Song getSongInformation(StreamingServiceFacade originServiceFacade, String sanitizedURL) throws Exception {
        String songId = originServiceFacade.getAssetIdFromURL(sanitizedURL);
        return originServiceFacade.getSongFromId(songId);
    }
}
