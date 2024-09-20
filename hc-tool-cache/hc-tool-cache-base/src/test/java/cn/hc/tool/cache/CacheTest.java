package cn.hc.tool.cache;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/20 20:34
 */
@Slf4j
public class CacheTest {

    @Test
    public void isPrimitive() {
        log.info("int: {}", int.class.isPrimitive());
        log.info("Integer: {}", Integer.class.isPrimitive());
    }
}
