package com.t2g.app.model;

import com.t2g.app.annotations.JSONField;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

/*
Table schema:
{
    id : * [User Id]-[Service domain name] *,
    accessToken: * Song id from Spotify *,
    refreshToken: * Song id from Youtube *
}
*/
@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
public class UserCredentialsTableEntry extends FakeDBEntry<String> {
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String REFRESH_TOKEN = "refreshToken";

    @JSONField(ACCESS_TOKEN)
    private String accessToken;

    @JSONField(REFRESH_TOKEN)
    private String refreshToken;

    @Builder
    public UserCredentialsTableEntry(
            String id,
            String accessToken,
            String refreshToken
    ) {
        super(id);
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public UserCredentialsTableEntry(JSONObject jsonEntry) {
        super((String) jsonEntry.get(PRIMARY_KEY));
        this.accessToken = (String) jsonEntry.get(ACCESS_TOKEN);
        this.refreshToken = (String) jsonEntry.get(REFRESH_TOKEN);
    }
}
