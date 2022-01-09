package com.t2g.app.model;

import com.t2g.app.exception.StreamingServiceNotSupported;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@AllArgsConstructor
public enum StreamingService {
    SPOTIFY("spotify"),
    YOUTUBE("youtube"),
    DEEZER("deezer");

    private static final String SERVICE_NOT_FOUND_FOR_DOMAIN_ERROR_MESSAGE = "Streaming Service not found for domain name %s";
    private static final String BETWEEN_DOTS_REGEX = "(?=\\.).+(?<=\\.)";

    private String domainName;

    public static StreamingService getServiceFromDomainName(String domainName) throws StreamingServiceNotSupported {
        for (StreamingService streamingService : StreamingService.values()) {
            if (streamingService.getDomainName().equals(domainName)) {
                return streamingService;
            }
        }

        throw new StreamingServiceNotSupported();
    }

    public static StreamingService getServiceFromUrl(String url) throws StreamingServiceNotSupported {
        String domainName = getDomainNameFromURL(url);
        return getServiceFromDomainName(domainName);
    }

    private static String getDomainNameFromURL(String sanitizedURL) {
        Pattern pattern = Pattern.compile(BETWEEN_DOTS_REGEX, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(sanitizedURL);
        if (matcher.find()) {
            String unformattedServiceName = matcher.group(0);
            return unformattedServiceName.replaceAll("\\.", "");
        }

        return null;
    }
}
