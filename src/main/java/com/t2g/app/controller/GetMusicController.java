package com.t2g.app.controller;

import com.t2g.app.LinkNotFoundException;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class GetMusicController {
    private static final String BETWEEN_DOTS_REGEX = "(?=\\.).+(?<=\\.)";

    @Autowired
    private StreamingServiceFacadeFactory streamingServiceFacadeFactory;

    @Autowired
    private LinkTableDAO linkTableDAO;

    @CrossOrigin(origins = "*")
    @GetMapping("/link")
    public Map<String, String> getUniversalLink(@RequestParam("id") String id) throws Exception {
        Optional<LinkTableEntry> linkTableEntry = Optional.ofNullable(linkTableDAO.getLinksById(id));

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

        StreamingService streamingService = StreamingService.getServiceFromDomainName(getDomainNameFromURL(sanitizedUrl));
        StreamingServiceFacade originServiceFacade = streamingServiceFacadeFactory.getStreamingServiceFacade(streamingService);

        String originSongId = originServiceFacade.getSongIdFromURL(sanitizedUrl);
        LinkTableEntry existingLink = linkTableDAO.getLinkByServiceId(originSongId, streamingService);
        if (
                existingLink !=  null &&
                existingLink.getUrl().size() == StreamingService.values().length
        ) {
            Map<String, String> response = new HashMap<>();
            response.put(LinkTableEntry.UNIVERSAL_ID, existingLink.getUniversalId());

            return response;
        }

        Song requestedSong = getSongInformation(originServiceFacade, sanitizedUrl);
        songIdByServiceDomain.put(streamingService.getDomainName(), originSongId);
        songURLsByDomainName.put(streamingService.getDomainName(), requestedSong.getUrl()); // TODO: de-sanitize the url before adding it to the map

        for (StreamingService service : StreamingService.values()) {
            if (service != streamingService) {
                try {
                    StreamingServiceFacade streamingServiceFacade = streamingServiceFacadeFactory.getStreamingServiceFacade(service);
                    Song song = streamingServiceFacade.getSongFromSongObject(requestedSong);

                    String songUrl = song.getUrl();
                    songIdByServiceDomain.put(service.getDomainName(), streamingServiceFacade.getSongIdFromURL(songUrl));
                    songURLsByDomainName.put(service.getDomainName(), songUrl);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        LinkTableEntry linkTableEntry = new LinkTableEntry(songIdByServiceDomain, songURLsByDomainName);
        linkTableDAO.addLink(linkTableEntry);

        Map<String, String> response = new HashMap<>();
        response.put(LinkTableEntry.UNIVERSAL_ID, linkTableEntry.getUniversalId());

        return response;
    }

    private String getDomainNameFromURL(String sanitizedURL) {
        Pattern pattern = Pattern.compile(BETWEEN_DOTS_REGEX, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(sanitizedURL);
        if (matcher.find()) {
            String unformattedServiceName = matcher.group(0);
            return unformattedServiceName.replaceAll("\\.", "");
        }

        return null;
    }

    private Song getSongInformation(StreamingServiceFacade originServiceFacade, String sanitizedURL) throws Exception {
        String songId = originServiceFacade.getSongIdFromURL(sanitizedURL);
        return originServiceFacade.getSongFromId(songId);
    }
}
