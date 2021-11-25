package com.t2g.app.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StreamingService {
    SPOTIFY("spotify"),
    YOUTUBE("youtube"),
    DEEZER("deezer");

    private static final String SERVICE_NOT_FOUND_FOR_DOMAIN_ERROR_MESSAGE = "Streaming Service not found for domain name %s";

    private String domainName;

    public static StreamingService getServiceFromDomainName(String domainName) {
        for (StreamingService streamingService : StreamingService.values()) {
            if (domainName.equals(streamingService.getDomainName())) {
                return streamingService;
            }
        }

        throw new IllegalArgumentException(String.format(SERVICE_NOT_FOUND_FOR_DOMAIN_ERROR_MESSAGE, domainName));
    }
}
