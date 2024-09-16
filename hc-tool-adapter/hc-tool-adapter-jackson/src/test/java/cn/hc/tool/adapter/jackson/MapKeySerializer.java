package cn.hc.tool.adapter.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.lang.reflect.Modifier;

public class MapKeySerializer extends com.fasterxml.jackson.databind.JsonSerializer {


    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        String keyString = JacksonUtils.serialize(value);
        boolean isFinal = Modifier.isFinal(value.getClass().getModifiers());

        StringBuilder key = new StringBuilder();
        if (isFinal) {

            key.append("{\"").append(JacksonUtils.CLASS_KEY).append("\":\"").append(value.getClass().getName()).append("\",");

            if (value instanceof Integer || value instanceof Boolean
                    || value instanceof Byte || value instanceof Short
                    || value instanceof Long || value instanceof Float
                    || value instanceof Double || value instanceof String) {
                key.append(JacksonUtils.BASE_VALUE_KEY).append(":").append(keyString);
            } else if (value instanceof Enum) {
                key.append(JacksonUtils.BASE_VALUE_KEY).append(":").append(keyString);
            } else if(value.getClass().isArray()){
                key.append(JacksonUtils.BASE_VALUE_KEY).append(":").append(keyString);
            } else if (keyString.startsWith("{")) {
                key.append(keyString.substring(1, keyString.length() - 1));
            } else {
                key.append(keyString);
            }

            key.append("}");
            keyString = key.toString();
        }

        gen.writeFieldName(keyString);
    }
}