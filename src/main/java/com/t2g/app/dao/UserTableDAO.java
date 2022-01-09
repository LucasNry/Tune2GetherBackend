package com.t2g.app.dao;

import com.t2g.app.exception.InvalidEmailException;
import com.t2g.app.exception.InvalidPasswordException;
import com.t2g.app.model.FakeDBDAO;
import com.t2g.app.model.UserTableEntry;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class UserTableDAO extends FakeDBDAO<UserTableEntry> {
    private static final String FAKE_TABLE_FILENAME = "User";

    public UserTableDAO() {
        super(UserTableEntry.class, FAKE_TABLE_FILENAME);
    }

    public UserTableEntry getUserByLoginCredentials(String userEmail, String userPassword) throws InvalidEmailException, InvalidPasswordException {
        JSONArray lines = getAllLines();

        for (Object line : lines) {
            JSONObject jsonObject = (JSONObject) line;
            String email = (String) jsonObject.get(UserTableEntry.EMAIL);
            String password = (String) jsonObject.get(UserTableEntry.PASSWORD);

            if (email.equals(userEmail)) {
                if (password.equals(userPassword)) {
                    return new UserTableEntry(jsonObject);
                } else {
                    throw new InvalidPasswordException();
                }
            }
        }

        throw new InvalidEmailException();
    }

    public boolean isUserAlreadyRegistered(String userEmail) {
        JSONArray lines = getAllLines();

        for (Object line : lines) {
            JSONObject jsonObject = (JSONObject) line;
            String email = (String) jsonObject.get(UserTableEntry.EMAIL);

            if (email.equals(userEmail)) {
                return true;
            }
        }

        return false;
    }
}
