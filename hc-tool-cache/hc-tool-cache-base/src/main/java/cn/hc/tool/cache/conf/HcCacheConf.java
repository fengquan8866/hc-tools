package cn.hc.tool.cache.conf;

import cn.hc.tool.cache.util.HcCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashMap;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/21 10:52
 */
@Configuration
@EnableCaching
public class HcCacheConf {
    @Bean(name="hcCacheManager")
    @Primary
    public CacheManager hcCacheManager(){
        HcCacheManager myCacheManager = new HcCacheManager();
        myCacheManager.setCaches(new HashMap<>());
        return myCacheManager;
    }
}
