package com.t2g.app.dao;

import com.t2g.app.model.FakeDBEntry;
import com.t2g.app.model.LinkTableEntry;
import com.t2g.app.model.StreamingService;
import com.t2g.app.model.FakeDBDAO;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class LinkTableDAO extends FakeDBDAO<LinkTableEntry> {
    private static final String FAKE_TABLE_FILENAME = "LinkTable";

    private static final String SERVICE_ID_TEMPLATE = "%sId";

    public LinkTableDAO() {
        super(LinkTableEntry.class, FAKE_TABLE_FILENAME);
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
}
