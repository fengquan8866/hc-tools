package cn.hc.tool.adapter.jackson;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class MapKeyDeserializer extends com.fasterxml.jackson.databind.KeyDeserializer {

    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
        JsonNode jsonNode = JacksonUtils.readNode(key);
        JsonNode classNode = jsonNode.get(JacksonUtils.CLASS_KEY);

        Class<?> clz = null;
        try {
            clz = Class.forName(classNode.asText());
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }

        JsonNode valueNode = jsonNode.get(JacksonUtils.BASE_VALUE_NODE_KEY);

        if (clz.equals(Integer.class)) {
            return Integer.parseInt(valueNode.asText());
        } else if (clz.equals(Boolean.class)) {
            return Boolean.parseBoolean(valueNode.asText());
        } else if (clz.equals(Byte.class)) {
            return Byte.parseByte(valueNode.asText());
        } else if (clz.equals(Short.class)) {
            return Short.parseShort(valueNode.asText());
        } else if (clz.equals(Long.class)) {
            return Long.parseLong(valueNode.asText());
        } else if (clz.equals(Float.class)) {
            return Float.parseFloat(valueNode.asText());
        } else if (clz.equals(Double.class)) {
            return Double.parseDouble(valueNode.asText());
        } else if (clz.equals(String.class)) {
            return valueNode.asText();
        } else if (clz.isEnum()) {
            return JacksonUtils.deserialize(valueNode.toString(), clz);
        } else if (clz.isArray()) {
            return JacksonUtils.deserialize(valueNode.toString(), clz);
        }


        return JacksonUtils.deserialize(key, clz);
    }
}