package com.t2g.app.model;

import com.google.gson.JsonObject;
import com.t2g.app.annotations.QueryName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

@AllArgsConstructor
@Data
public abstract class Song {

    @QueryName("track")
    private String title;

    @QueryName("artist")
    private List<String> artists;

    private String album;

    private List<TrackCover> images;

    private String url;

    private String queryParameterSeparator;

    public abstract String serializeField(String key, Object value);

    public abstract String serializeCollection(String key, Collection<?> value);

    public String getQueryString() throws Exception {
        StringBuilder sb = new StringBuilder();

        for (Field field : Song.class.getDeclaredFields()) {
            if (field.isAnnotationPresent(QueryName.class)) {
                String key = field.getAnnotation(QueryName.class).value();
                Object value = field.get(this);

                if (value != null) {
                    if (sb.length() > 0) {
                        sb.append(queryParameterSeparator);
                    }

                    if (value instanceof Collection<?>) {
                        sb.append(serializeCollection(key, (Collection<?>) value));
                    } else {
                        sb.append(serializeField(key, value));
                    }
                }
            }
        }

        return sb.toString();
    }
}
