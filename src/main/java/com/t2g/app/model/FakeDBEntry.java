package com.t2g.app.model;

import com.t2g.app.annotations.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import org.json.simple.JSONObject;

import javax.lang.model.type.PrimitiveType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public abstract class FakeDBEntry<T extends Comparable<?>> {
    protected static final String PRIMARY_KEY = "primaryKey";

    @JSONField(PRIMARY_KEY)
    private T primaryKey;

    public JSONObject toJSON() throws Exception {
        JSONObject result = new JSONObject();
        List<Field> fields = getFields();

        for (Field field : fields) {
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

    private List<Field> getFields() {
        List<Field> result = new ArrayList<>(Arrays.asList(this.getClass().getDeclaredFields()));

        Class<?> superClazz = this.getClass().getSuperclass();
        while (superClazz != Object.class) {
            List<Field> superClazzFields = Arrays.asList(superClazz.getDeclaredFields());
            result.addAll(superClazzFields);
            superClazz = superClazz.getSuperclass();
        }

        return result;
    }
}
