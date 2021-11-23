package com.t2g.app.controller;

import com.t2g.app.facade.StreamingServiceFacade;
import com.t2g.app.facade.StreamingServiceFacadeFactory;
import com.t2g.app.model.Song;
import com.t2g.app.model.StreamingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class GetMusicController {
    private static final String BETWEEN_DOTS_REGEX = "(?=\\.).+(?<=\\.)";

    @Autowired
    private StreamingServiceFacadeFactory streamingServiceFacadeFactory;

    @GetMapping("getMusic")
    public Map<String, String> getMusic(@RequestParam("l") String sanitizedUrl) throws Exception {
        Map<String, String> songURLsByDomainName = new HashMap<>();

        StreamingService streamingService = StreamingService.getServiceFromDomainName(getDomainNameFromURL(sanitizedUrl));
        Song requestedSong = getSongInformation(sanitizedUrl, streamingService);

        songURLsByDomainName.put(streamingService.getDomainName(), sanitizedUrl); // TODO: de-sanitize the url before adding it to the map

        //TODO: Save track id on fake DB

        for (StreamingService service : StreamingService.values()) {
            if (service != streamingService) {
                StreamingServiceFacade streamingServiceFacade = streamingServiceFacadeFactory.getStreamingServiceFacade(service);
                Song song = streamingServiceFacade.getSongFromSongObject(requestedSong);

                songURLsByDomainName.put(service.getDomainName(), song.getUri());
            }
        }

        return songURLsByDomainName;
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

    private Song getSongInformation(String sanitizedURL, StreamingService streamingService) throws Exception {
        StreamingServiceFacade originServiceFacade = streamingServiceFacadeFactory.getStreamingServiceFacade(streamingService);

        String songId = originServiceFacade.getSongIdFromURL(sanitizedURL);
        return originServiceFacade.getSongFromId(songId);
    }
}
