package cn.hc.tool.adapter.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hc.json.adapter.JsonAdapterConfigure;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/15 18:40
 */
public class HcJacksonAdapterConfigure  implements JsonAdapterConfigure {
    @Override
    public void config(Object o) {
        ObjectMapper mapper = (ObjectMapper) o;
        // 空对象
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        // 空属性
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // 忽略null
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // LocalDateTime
        mapper.findAndRegisterModules();
    }
}
