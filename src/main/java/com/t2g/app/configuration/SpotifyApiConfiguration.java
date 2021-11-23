package com.t2g.app.configuration;

import com.wrapper.spotify.SpotifyApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration
public class SpotifyApiConfiguration {
    private static final String CLIENT_ID = "1054b24476064d07be6671c399b8d816";
    private static final String CLIENT_SECRET = "5a24db2e6615413ea39980cc155baaf1";
    private static final String REDIRECT_URI = "http://localhost:8080/getToken"; // TODO: create redirect endpoint

    @Bean
    public SpotifyApi spotifyApi() {
        return SpotifyApi
                .builder()
                .setClientId(CLIENT_ID)
                .setClientSecret(CLIENT_SECRET)
                .setRedirectUri(URI.create(REDIRECT_URI))
                .build();
    }
}
