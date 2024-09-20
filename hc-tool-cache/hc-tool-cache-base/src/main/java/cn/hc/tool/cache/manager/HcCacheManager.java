package cn.hc.tool.cache.manager;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.Collections;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/20 20:21
 */
public class HcCacheManager implements CacheManager {
    @Override
    public Cache getCache(String name) {
        return null;
    }

    @Override
    public Collection<String> getCacheNames() {
        return Collections.emptyList();
    }
}
