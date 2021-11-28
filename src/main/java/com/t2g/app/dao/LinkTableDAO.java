package com.t2g.app.dao;

import com.t2g.app.model.LinkTableEntry;
import com.t2g.app.model.StreamingService;
import com.t2g.app.util.JSONUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

/*
Table schema:
{
    id : * Song id from Spotify + Song id from Youtube + Song id from Deezer *,
    spotifyId: * Song id from Spotify *,
    youtubeId: * Song id from Youtube *,
    deezerId: * Song id from Deezer *,
    url :  {
        spotify : * Spotify url *
        youtube : * Youtube url *
        deezer : * Deezer url *
    }
}
*/
@Component
public class LinkTableDAO {
    private static final String FAKE_TABLE_FILENAME = "LinkTable";

    private static final String SERVICE_ID_TEMPLATE = "%sId";

    public LinkTableEntry getLinksById(String providedUniversalId) {
        try {
            JSONArray lines = getAllLines();

            for (Object line : lines) {
                JSONObject jsonObject = (JSONObject) line;
                String universalId = (String) jsonObject.get(LinkTableEntry.UNIVERSAL_ID);

                if (providedUniversalId.equals(universalId)) {
                    return new LinkTableEntry(jsonObject);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public LinkTableEntry getLinkByServiceId(String providedServiceId, StreamingService streamingService) {
        try {
            JSONArray lines = getAllLines();

            for (Object line : lines) {
                JSONObject jsonObject = (JSONObject) line;
                String serviceId = (String) jsonObject.get(String.format(SERVICE_ID_TEMPLATE, streamingService.getDomainName()));

                if (providedServiceId.equals(serviceId)) {
                    return new LinkTableEntry(jsonObject);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void addLink(LinkTableEntry linkTableEntry) throws Exception {
        if (getLinksById(linkTableEntry.getUniversalId()) != null) {
            return;
        }

        JSONObject jsonEntry = linkTableEntry.toJSON();

        JSONObject document = JSONUtils.readJSONFile(FAKE_TABLE_FILENAME);
        JSONArray jsonArray = (JSONArray) document.get(FAKE_TABLE_FILENAME);
        jsonArray.add(jsonEntry);

        JSONUtils.writeToJSONFile(FAKE_TABLE_FILENAME, document);
    }

    public JSONArray getAllLines() {
        try {
            return JSONUtils.getTableFromJSON(FAKE_TABLE_FILENAME);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
