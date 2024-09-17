package cn.hc.tool.cache.util;

import cn.hc.tool.cache.bean.CacheConf;
import cn.hc.tool.cache.exception.ToolCacheException;
import cn.hc.tool.common.exception.HcToolException;
import cn.hc.tool.trace.util.TraceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.Callable;

/**
 * 锁工具类
 *
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/16 19:47
 */
@Slf4j
public class ToolLockUtil {

    @Autowired
    private ToolDCSLock toolDCSLock;

    /**
     * {@link #execute(CacheConf, Callable, Callable, Object...)}
     */
    public <T> T execute(CacheConf cacheConf, Callable<T> callback, Object... keyParams) {
        return this.execute(cacheConf, callback, null, keyParams);
    }

    /**
     * 分布式锁执行方法
     *
     * @param cacheConf 缓存配置
     * @param callback  方法体
     * @param error     生成指定异常的方法体
     * @param keyParams 生成完整缓存key需要的参数
     * @param <T>       泛型
     * @return 方法体执行结果
     */
    public <T, E extends Exception> T execute(CacheConf cacheConf, Callable<T> callback, Callable<E> error, Object... keyParams) {
        return TraceFactory.trace(() -> {
            String traceId = TraceFactory.traceId();
            if (!toolDCSLock.lock(cacheConf, traceId, keyParams)) {
                if (error != null) {
                    throw error.call();
                } else {
                    throw new HcToolException("锁已存在，请勿重复操作");
                }
            }
            try {
                return callback.call();
            } catch (Exception e) {
                log.error("回调函数执行失败", e);
                throw new ToolCacheException(e);
            } finally {
                toolDCSLock.unLock(cacheConf, traceId, keyParams);
            }
        });
    }

}
