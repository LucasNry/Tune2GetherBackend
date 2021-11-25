package com.t2g.app.model;

import com.t2g.app.annotations.JSONField;
import org.json.simple.JSONObject;

import java.lang.reflect.Field;
import java.util.Map;

public abstract class FakeDBEntry {
    public JSONObject toJSON() throws Exception {
        JSONObject result = new JSONObject();

        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(JSONField.class)) {
                String key = field.getAnnotation(JSONField.class).value();

                field.setAccessible(true);
                Object value = field.get(this);

                if (value instanceof Map<?, ?>) {
                    value = new JSONObject((Map) value);
                }

                result.put(key, value);
            }
        }

        return result;
    }
}
