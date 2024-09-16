package cn.hc.tool.adapter.jackson;

import com.hc.json.adapter.Json;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/15 18:44
 */
@Slf4j
public class JsonTest {
    @Test
    public void testJson() {
        Shell shell = new Shell(new Child(13).setName("zs").setList(new ArrayList<String>()));
        String json = Json.toJson(shell);
        log.info("json: {}", json);
        Shell after = Json.fromJson(json, Shell.class);
        log.info("after json:{}", JacksonUtils.serialize(after));
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Shell {
    private Parent parent;
}

@Data@Accessors(chain = true)
class Parent {
    private String name;
    private List<String> list;
    public Parent setName(String name) {
        this.name = name;
        return this;
    }
}

@Data@EqualsAndHashCode(callSuper = false)@AllArgsConstructor@NoArgsConstructor
class Child extends Parent {
    private Integer age;
}