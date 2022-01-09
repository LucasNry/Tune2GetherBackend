package com.t2g.app.model;

import com.t2g.app.util.JSONUtils;
import lombok.AllArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.lang.reflect.Constructor;

@AllArgsConstructor
public abstract class FakeDBDAO<T extends FakeDBEntry> {

    private Class<T> entryClazz;

    private String fakeTableFilename;

    public T getByPrimaryKey(Comparable<?> primaryKey) {
        try {
            JSONArray lines = getAllLines();

            for (Object line : lines) {
                JSONObject jsonObject = (JSONObject) line;
                String universalId = (String) jsonObject.get(LinkTableEntry.PRIMARY_KEY);

                if (primaryKey.equals(universalId)) {
                    Constructor constructor = entryClazz.getConstructor(JSONObject.class);
                    return (T) constructor.newInstance(jsonObject);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void addEntry(FakeDBEntry entry) throws Exception {
        if (isEntryPresent(entry)) {
            return;
        }

        JSONObject jsonEntry = entry.toJSON();

        JSONObject document = JSONUtils.readJSONFile(fakeTableFilename);
        JSONArray jsonArray = (JSONArray) document.get(fakeTableFilename);
        jsonArray.add(jsonEntry);

        JSONUtils.writeToJSONFile(fakeTableFilename, document);
    }

    public void updateEntry(FakeDBEntry updatedEntry) {
        new Thread(() -> {
            if (isEntryPresent(updatedEntry)) {
                try {
                    FakeDBEntry fakeDBEntry = getByPrimaryKey(updatedEntry.getPrimaryKey());
                    deleteEntry(fakeDBEntry);
                    addEntry(updatedEntry);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void deleteEntry(FakeDBEntry entry) {
        new Thread(() -> {
            if (isEntryPresent(entry)) {
                try {
                    JSONObject document = JSONUtils.readJSONFile(fakeTableFilename);
                    JSONArray jsonArray = (JSONArray) document.get(fakeTableFilename);

                    jsonArray.remove(entry);
                    JSONUtils.writeToJSONFile(fakeTableFilename, document);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private boolean isEntryPresent(FakeDBEntry fakeDBEntry) {
        return getByPrimaryKey(fakeDBEntry.getPrimaryKey()) != null;
    }

    public JSONArray getAllLines() {
        try {
            return JSONUtils.getTableFromJSON(fakeTableFilename);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
