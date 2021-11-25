package com.t2g.app.util;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

public class JSONUtils {
    private static final String JSON_EXTENSION = ".json"; // Related to jar
    private static final String FILENAME_TEMPLATE = "resources/%s%s"; // Related to jar

    private static JSONParser jsonParser = new JSONParser();

    public static JSONObject readJSONFile(String fileName) throws Exception {
        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(
                        String.format(FILENAME_TEMPLATE, fileName, JSON_EXTENSION)
                )
        )) {
            return (JSONObject) jsonParser.parse(reader);
        }
    }

    public static JSONArray getTableFromJSON(String fileName) throws Exception {
        return (JSONArray) readJSONFile(fileName).get(fileName);
    }

    public static void writeToJSONFile(String fileName, JSONObject jsonObject) throws Exception {
        try (FileWriter fileOutputStream = new FileWriter(
                String.format(FILENAME_TEMPLATE, fileName, JSON_EXTENSION)
        )) {
            fileOutputStream.write(jsonObject.toJSONString());
        }
    }
}
