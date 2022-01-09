package com.t2g.app.controller;

import com.t2g.app.dao.UserCredentialsTableDAO;
import com.t2g.app.dao.UserTableDAO;
import com.t2g.app.exception.InvalidEmailException;
import com.t2g.app.exception.InvalidPasswordException;
import com.t2g.app.exception.MissingInformationException;
import com.t2g.app.exception.UserAlreadyExistsException;
import com.t2g.app.exception.UserNotFound;
import com.t2g.app.model.LoginRequest;
import com.t2g.app.model.LoginResponse;
import com.t2g.app.model.StreamingService;
import com.t2g.app.model.UserTableEntry;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {
    private static final String USER_ID = "user_id";

    @Autowired
    private UserTableDAO userTableDAO;

    @Autowired
    private UserCredentialsTableDAO userCredentialsTableDAO;

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @PostMapping("/login")
    public LoginResponse authenticate(
            @CookieValue(name = USER_ID, defaultValue = "") String userId,
            @RequestBody LoginRequest loginRequest,
            HttpServletResponse httpServletResponse
    ) throws InvalidEmailException, InvalidPasswordException {
        if (!userId.isBlank()) {
            UserTableEntry userTableEntry = userTableDAO.getByPrimaryKey(userId);
            return LoginResponse
                    .builder()
                    .username(userTableEntry.getUsername())
                    .build();
        }

        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        UserTableEntry user = userTableDAO.getUserByLoginCredentials(email, password);
        Cookie userCookie = createCookie(USER_ID, user.getPrimaryKey());
        httpServletResponse.addCookie(userCookie);

        return LoginResponse
                .builder()
                .username(user.getUsername())
                .build();
    }

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @GetMapping("/logout")
    public ResponseEntity<String> authenticate(
            HttpServletResponse httpServletResponse
    ) {
        Cookie resetCookie = createCookie(USER_ID, "");
        resetCookie.setMaxAge(0);
        httpServletResponse.addCookie(resetCookie);

        return ResponseEntity
                .ok()
                .build();
    }

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @PostMapping("/signup")
    public ResponseEntity<String> signUserUp(@RequestBody UserTableEntry userTableEntry) throws Exception {
        try {
            Validate.notNull(userTableEntry.getUsername());
            Validate.notNull(userTableEntry.getEmail());
            Validate.notNull(userTableEntry.getPassword());
        } catch (Exception e) {
            throw new MissingInformationException();
        }

        if (userTableDAO.isUserAlreadyRegistered(userTableEntry.getEmail())) {
            throw new UserAlreadyExistsException();
        }

        userTableDAO.addEntry(userTableEntry);
        return ResponseEntity
                .ok()
                .build();
    }

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @GetMapping("/user")
    public UserTableEntry getUserInfo(@CookieValue(name = USER_ID, defaultValue = "") String userId) throws UserNotFound {
        if (!userId.isBlank()) {
            return userTableDAO.getByPrimaryKey(userId);
        }

        throw new UserNotFound();
    }

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @GetMapping("/user/links")
    public Map<String, Boolean> getUserLinks(@CookieValue(name = USER_ID, defaultValue = "") String userId) throws UserNotFound {
        if (!userId.isBlank()) {
            Map<String, Boolean> response = new HashMap<>();

            for (StreamingService streamingService : StreamingService.values()) {
                response.put(
                        streamingService.getDomainName(),
                        userCredentialsTableDAO.getByPrimaryKey(String.format("%s-%s", userId, streamingService.getDomainName())) != null
                );
            }

            return response;
        }

        throw new UserNotFound();
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setDomain("");

        return cookie;
    }
}
