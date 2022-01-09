package com.t2g.app.controller;

import com.google.common.collect.ImmutableMap;
import com.t2g.app.dao.UserCredentialsTableDAO;
import com.t2g.app.facade.DeezerAPIFacade;
import com.t2g.app.facade.SpotifyAPIFacade;
import com.t2g.app.facade.StreamingServiceFacade;
import com.t2g.app.facade.StreamingServiceFacadeFactory;
import com.t2g.app.facade.YTAPIFacade;
import com.t2g.app.model.UserCredentialsTableEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@RestController
public class ConnectAccountController {

    @Autowired
    private StreamingServiceFacadeFactory streamingServiceFacadeFactory;

    @Autowired
    private UserCredentialsTableDAO userCredentialsTableDAO;

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @GetMapping("/connect/{serviceDomainName}")
    public Map<String, String> connectSpotifyAccount(
            @CookieValue(name = "user_id", defaultValue = "") String userId,
            @PathVariable String serviceDomainName
    ) throws Exception {
        StreamingServiceFacade streamingServiceFacade = streamingServiceFacadeFactory.getStreamingServiceFacade(serviceDomainName);
        return ImmutableMap.of("url", streamingServiceFacade.getAuthenticationURL(userId));
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/redirect/{serviceDomain}/{user_id}")
    public RedirectView redirect(
            @RequestParam("code") String code,
            @PathVariable String serviceDomain,
            @PathVariable String user_id
    ) throws Exception {
        StreamingServiceFacade streamingServiceFacade = streamingServiceFacadeFactory.getStreamingServiceFacade(serviceDomain);
        UserCredentialsTableEntry userCredentialsTableEntry = streamingServiceFacade.getUserCredentials(user_id, code);
        userCredentialsTableDAO.addEntry(userCredentialsTableEntry);

        return new RedirectView("http://localhost:3000/accounts");
    }
}
