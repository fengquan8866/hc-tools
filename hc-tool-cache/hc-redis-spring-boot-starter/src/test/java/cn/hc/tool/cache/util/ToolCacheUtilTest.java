package cn.hc.tool.cache.util;

import cn.hc.tool.cache.bean.CacheKey;
import cn.hc.tool.cache.conf.HcRedisComponentScanner;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/16 18:27
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ToolCacheUtilTest.class, HcRedisComponentScanner.class})
@Slf4j
@EnableAutoConfiguration
public class ToolCacheUtilTest {

    @Autowired
    private ToolCacheUtil toolCacheUtil;

    @Test
    public void getWithCache() throws Exception {
        Integer sku = 1;
        Integer val = toolCacheUtil.getWithCache(CacheKey.SKU_INFO, Integer.class, () -> {
            if (sku > 0) {
                log.info("sku:{}", sku);
            }
            return sku + 1;
        }, sku);
        log.info("val:{}", val);
    }
}
