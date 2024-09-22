package cn.hc.tool.cache.conf;

import cn.hc.tool.cache.adapter.RedisSpringBootAdapter;
import cn.hc.tool.cache.util.ToolCacheUtil;
import cn.hc.tool.cache.util.ToolDCSLock;
import cn.hc.tool.cache.util.ToolLockUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/16 17:44
 */
@Configuration
public class HcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(value = {ToolCacheUtil.class})
    public ToolCacheUtil hcCacheUtil(RedisSpringBootAdapter redisAdapter) {
        ToolCacheUtil toolCacheUtil = new ToolCacheUtil();
        toolCacheUtil.setCacheAdapter(redisAdapter);
        return toolCacheUtil;
    }

    @Bean
    public ToolDCSLock hcDcsLock(RedisSpringBootAdapter redisAdapter) {
        ToolDCSLock toolDCSLock = new ToolDCSLock();
        toolDCSLock.setCacheAdapter(redisAdapter);
        return toolDCSLock;
    }

    @Bean
    @ConditionalOnMissingBean(value = {ToolLockUtil.class})
    public ToolLockUtil hcLockUtil() {
        return new ToolLockUtil();
    }
}
