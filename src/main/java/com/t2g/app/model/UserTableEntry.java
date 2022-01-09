package com.t2g.app.model;

import com.t2g.app.annotations.JSONField;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

import java.util.UUID;

/*
Table schema:
{
    id : * Song id from Spotify + Song id from Youtube + Song id from Deezer *,
    username: * User name *,
    email: * User email *,
    password: * User password *,
    preferredService: * Preferred streaming service *
}
*/
@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
public class UserTableEntry extends FakeDBEntry<String> {
    public static final String USERNAME = "username";
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    public static final String PREFERRED_SERVICE = "preferredService";

    @JSONField(USERNAME)
    private String username;

    @JSONField(EMAIL)
    private String email;

    @JSONField(PASSWORD)
    private String password;

    @JSONField(PREFERRED_SERVICE)
    private String preferredService;

    @Builder
    public UserTableEntry(
            String username,
            String email,
            String password,
            String preferredService
    ) {
        super(
                UUID
                        .randomUUID()
                        .toString()
        );
        this.username = username;
        this.email = email;
        this.password = password;
        this.preferredService = preferredService;
    }

    public UserTableEntry(JSONObject jsonEntry) {
        super((String) jsonEntry.get(PRIMARY_KEY));
        this.username = (String) jsonEntry.get(USERNAME);
        this.email = (String) jsonEntry.get(EMAIL);
        this.password = (String) jsonEntry.get(PASSWORD);
        this.preferredService = (String) jsonEntry.get(PREFERRED_SERVICE);
    }
}
