package cn.hc.tool.common.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cdhuangchao3
 * @date 2022/4/18 5:55 PM
 */
@Slf4j
public class NumberUtilTest {

    @Test
    public void cellInt() {
        log.info("天花板数：3L除以2L：{}", NumberUtil.cellInt(3L, 2L));
        log.info("天花板数：3L除以2：{}", NumberUtil.cellInt(3L, 2));
        log.info("天花板数：2L除以2：{}", NumberUtil.cellInt(2L, 2));
        log.info("天花板数：3除以2：{}", NumberUtil.cellInt(3, 2));
    }

    @Test
    public void min() {
        List<Long> l = new ArrayList<Long>();
        l.add(1L);
        l.add(2L);
        l.add(0L);
        log.info("min: {}", NumberUtil.min(l));
        log.info("max: {}", NumberUtil.max(l));
    }

    @Test
    public void toInteger() {
        Long a = 1L;
        log.info("toInteger(Long)={}", NumberUtil.toInteger(a));
        String b = "2";
        log.info("toInteger(Long)={}", NumberUtil.toInteger(b));
        Number c = 3;
        log.info("toInteger(Long)={}", NumberUtil.toInteger(c));
        Object d = 4;
        log.info("toInteger(Long)={}", NumberUtil.toInteger(d));
        String e = "";
        log.info("toInteger(Long)={}", NumberUtil.toInteger(e, -1));
    }
}
