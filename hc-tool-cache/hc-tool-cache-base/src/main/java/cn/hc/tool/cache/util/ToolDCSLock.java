package cn.hc.tool.cache.util;

import cn.hc.tool.cache.adapter.CacheAdapter;
import cn.hc.tool.cache.bean.CacheConf;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁
 *
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/16 19:48
 */
@Slf4j
public class ToolDCSLock {

    private CacheAdapter cacheAdapter;

    /**
     * 添加分布式锁
     *
     * @param lockKey
     * @param keyParams
     * @return
     */
    boolean lock(CacheConf lockKey, String lockVal, Object... keyParams) {
        return cacheAdapter.setNx(lockKey.getFullCacheKey(keyParams), lockVal, lockKey.getExpireSeconds(), TimeUnit.SECONDS);
    }

    /**
     * 删除分布式锁
     *
     * @param lockKey
     * @param keyParams
     * @return
     */
    public boolean unLock(CacheConf lockKey, String lockVal, Object... keyParams) {
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        try {
            return cacheAdapter.eval(script, Collections.singletonList(lockKey.getFullCacheKey(keyParams)),
                    Collections.singletonList(lockVal), true, Boolean.class);
        } catch (Exception e) {
            log.error("分布式锁释放失败", e);
            return false;
        }
    }
}
