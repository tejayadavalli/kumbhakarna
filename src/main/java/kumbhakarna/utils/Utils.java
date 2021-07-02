package kumbhakarna.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.gson.JsonObject;
import org.postgresql.util.PGobject;

import java.io.IOException;

public class Utils {
    private static JsonMapper mapper = new JsonMapper();
    public static Object convertToDatabaseColumn(JsonObject eventData) {
        try {
            PGobject out = new PGobject();
            out.setType("json");
            out.setValue(eventData.toString());
            return out;
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to serialize to json field ", e);
        }
    }

    public static JsonObject convertToEntityAttribute(Object eventData) {
        try {
            if (eventData instanceof PGobject && ((PGobject) eventData).getType().equals("json")) {
                return mapper.reader(new TypeReference<JsonObject>() {
                }).readValue(((PGobject) eventData).getValue());
            }
            return new JsonObject();
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to deserialize to json field ", e);
        }
    }
}
