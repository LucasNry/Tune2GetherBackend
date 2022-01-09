package com.t2g.app.dao;

import com.t2g.app.model.FakeDBDAO;
import com.t2g.app.model.StreamingService;
import com.t2g.app.model.UserCredentialsTableEntry;
import org.springframework.stereotype.Component;

@Component
public class UserCredentialsTableDAO extends FakeDBDAO<UserCredentialsTableEntry> {
    private static final String PRIMARY_KEY_TEMPLATE = "%s-%s";

    private static final String FAKE_TABLE_FILENAME = "UserCredentials";

    public UserCredentialsTableDAO() {
        super(UserCredentialsTableEntry.class, FAKE_TABLE_FILENAME);
    }

    public UserCredentialsTableEntry getByUserIdAndService(String userId, StreamingService streamingService) {
        return getByPrimaryKey(String.format(PRIMARY_KEY_TEMPLATE, userId, streamingService.getDomainName()));
    }
}
