package cn.hc.tool.adapter.jackson;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hc.json.adapter.JsonException;

import static com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/15 18:46
 */
public class JacksonUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static final String CLASS_KEY = "@class";

    public static final String BASE_VALUE_KEY = "\"value\"";
    public static final String BASE_VALUE_NODE_KEY = "value";

    static {
        SimpleModule module = new SimpleModule();
        mapper.enableDefaultTypingAsProperty(NON_FINAL, CLASS_KEY);
        module.addKeyDeserializer(Object.class, new MapKeyDeserializer());
        module.addKeySerializer(Object.class, new MapKeySerializer());
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.registerModule(module);
    }

    public static String serialize(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Throwable t) {
            throw new JsonException(t);
        }
    }

    public static <T> T deserialize(String jsonString, Class<T> clazz) {
        try {
            return mapper.readValue(jsonString, clazz);
        } catch (Throwable t) {
            throw new JsonException(t);
        }
    }

    public static JsonNode readNode(String jsonString) {
        try {
            return mapper.readTree(jsonString);
        } catch (Throwable t) {
            throw new JsonException(t);
        }
    }

}
