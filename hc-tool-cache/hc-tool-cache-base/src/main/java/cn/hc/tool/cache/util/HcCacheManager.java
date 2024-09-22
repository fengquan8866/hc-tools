package cn.hc.tool.cache.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractCacheManager;

import java.util.Collection;
import java.util.Map;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/21 10:51
 */
@Setter
@Getter
public class HcCacheManager extends AbstractCacheManager {
    private Map<String, HcToolCache> caches;

    @Override
    protected Collection<? extends Cache> loadCaches() {
        return this.caches.values();
    }
}
