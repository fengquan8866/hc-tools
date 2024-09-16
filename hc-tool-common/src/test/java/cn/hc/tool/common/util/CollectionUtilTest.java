package cn.hc.tool.common.util;

import com.hc.json.adapter.Json;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author cdhuangchao3
 * @date 2023/5/16 5:14 PM
 */
@Slf4j
public class CollectionUtilTest {

    @Test
    public void subList() {
        List<Integer> list = Json.fromJsonToList("[1,2,3,4,5]", Integer.class);
        log.info("2到3：{}", CollectionUtil.subList(list, 2, 3));
        log.info("2到10：{}", CollectionUtil.subList(list, 2, 10));
        log.info("5到10：{}", CollectionUtil.subList(list, 5, 10));
    }
}
