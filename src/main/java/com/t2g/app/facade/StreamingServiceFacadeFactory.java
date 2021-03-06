package com.t2g.app.facade;

import com.t2g.app.exception.StreamingServiceNotSupported;
import com.t2g.app.model.StreamingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StreamingServiceFacadeFactory {
    private static final String FACADE_NOT_FOUND_FOR_SERVICE_ERROR_MESSAGE = "Facade not found for %s service";

    @Autowired
    private SpotifyAPIFacade spotifyAPIFacade;

    @Autowired
    private YTAPIFacade ytAPIFacade;

    @Autowired
    private DeezerAPIFacade deezerAPIFacade;

    public StreamingServiceFacade getStreamingServiceFacade(StreamingService streamingService) throws StreamingServiceNotSupported {
        return getStreamingServiceFacade(streamingService.getDomainName());
    }

    public StreamingServiceFacade getStreamingServiceFacade(String domainName) throws StreamingServiceNotSupported {
        StreamingService streamingService = StreamingService.getServiceFromDomainName(domainName);

        switch (streamingService) {
            case SPOTIFY:
                return spotifyAPIFacade;
            case DEEZER:
                return deezerAPIFacade;
            case YOUTUBE:
                return ytAPIFacade;
            default:
                throw new IllegalArgumentException(String.format(FACADE_NOT_FOUND_FOR_SERVICE_ERROR_MESSAGE, streamingService.name()));
        }
    }
}
