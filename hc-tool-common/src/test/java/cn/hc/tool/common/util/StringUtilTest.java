package cn.hc.tool.common.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author cdhuangchao3
 * @date 2022/4/19 7:41 PM
 */
@SpringBootTest(classes = StringUtilTest.class)
@Slf4j
public class StringUtilTest {

    @Test
    public void means() {
        log.info("1={}", StringUtil.means(1));
        log.info("1={}", StringUtil.means("1"));
        log.info("TRUE={}", StringUtil.means("TRUE"));
        log.info("true={}", StringUtil.means(true));
        log.info("FALSE={}", StringUtil.means("FALSE"));
        log.info("false={}", StringUtil.means(false));
        log.info("false={}", StringUtil.means(false));
    }

    @Test
    public void containsAny() {
        log.info("str contains s={}", StringUtil.containsAny("str", "s"));
        log.info("str contains s={}", StringUtil.containsAny("str", "s", "ss"));
        log.info("str contains s={}", StringUtil.containsAny("str", new String[]{"s", "ss"}));
    }

    @Test
    public void join() {
        log.info("init-1-aa={}", StringUtil.join("init", "-", 1, "aa"));
    }
}
