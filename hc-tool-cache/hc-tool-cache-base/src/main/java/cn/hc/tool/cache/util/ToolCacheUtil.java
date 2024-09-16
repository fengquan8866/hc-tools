package cn.hc.tool.cache.util;

import cn.hc.tool.cache.adapter.CacheAdapter;
import cn.hc.tool.cache.bean.CacheConf;
import cn.hc.tool.cache.bean.CacheData;
import cn.hc.tool.cache.enums.CacheStrategyEnum;
import cn.hc.tool.common.util.SystemClock;
import cn.hc.tool.config.util.ConfigUtil;
import com.hc.json.adapter.Json;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/15 20:21
 */
@Slf4j
public class ToolCacheUtil {
    private ConcurrentHashMap<String, FutureTask> cacheMap = new ConcurrentHashMap<>();

    @Setter
    private CacheAdapter cacheAdapter;

    @Value("${hc.cache.timeout:2000}")
    private long timeout = 2000;

    public <V, T extends CacheConf> V getWithCache(T cacheKey, Class<V> vClass, Function<T, String> getKeyFunc, Callable<V> reloadTask) throws Exception {
        return this.getWithCache(cacheKey, (Type) vClass, getKeyFunc, reloadTask);
    }

    public <V, T extends CacheConf> V getWithCache(T cacheKey, Type vClass, Function<T, String> getKeyFunc, Callable<V> reloadTask) throws Exception {
        CacheStrategyEnum cacheDegrade = getDegradeSwitch(cacheKey);
        String key = cacheKey.getKeyExp();
        if (getKeyFunc != null) {
            key = getKeyFunc.apply(cacheKey);
        }
        return getWithCache(cacheDegrade, vClass, key, cacheKey, reloadTask, null);
    }

    public <V> V getWithCache(CacheConf cacheKey, Class<V> vClass, Callable<V> reloadTask, Object... keyParams) throws Exception {
        return this.getWithCache(cacheKey, (Type) vClass, reloadTask, keyParams);
    }

    public <V> V getWithCache(CacheConf cacheKey, Type vClass, Callable<V> reloadTask, Object... keyParams) throws Exception {
        CacheStrategyEnum cacheDegrade = getDegradeSwitch(cacheKey);
        return getWithCache(cacheDegrade, vClass, cacheKey.getFullCacheKey(keyParams), cacheKey, reloadTask, null);
    }

    public <V, T extends CacheConf> V getWithCache(CacheStrategyEnum cacheDegrade, Type type, String key,
                                                        T cacheKey, Callable<V> reloadTask, Predicate<V> predicate) throws Exception {
        // 1、拒绝策略
        if (cacheDegrade == CacheStrategyEnum.REJECT) {
            return null;
        }
        int seconds = cacheKey.getExpireSeconds();
        int autoUpdateSeconds = cacheKey.getUpdateSeconds();
        // 2、RELOAD策略 或 缓存时间=0，重新加载
        if (cacheDegrade == CacheStrategyEnum.RELOAD_ONLY || seconds == 0) {
            return reloadTask.call();
        }
        // 3、获取缓存数据
        CacheData<V> cacheData = getCacheData(key, type);
        if (cacheData != null) {
            // 3.1非CACHE_ONLY策略，异步更新缓存
            if (cacheDegrade != CacheStrategyEnum.CACHE_ONLY) {
                updateCacheIfNeed(cacheData, key, seconds, autoUpdateSeconds, reloadTask, predicate);
            }
            return cacheData.getData();
        }
        // 4、未降级策略，执行reload任务，更新缓存
        if (cacheDegrade == CacheStrategyEnum.ALL_OPEN) {
            log.info("缓存穿透，开始重新加载数据，key：{}", key);
            V result = getFromTask(key, reloadTask, cacheKey);
            saveCache(result, key, seconds, predicate);
            return result;
        }
        log.info("缓存数据为空，key：{}", key);
        // 5、缓存读异步写 策略，异步更新缓存
        if (cacheDegrade == CacheStrategyEnum.CACHE_ONLY_AND_ASYNC_WRITE) {
            updateCacheIfNeed(null, key, seconds, autoUpdateSeconds, reloadTask, predicate);
        }
        return null;
    }

    /**
     * 从缓存获取，或者加载数据后更新缓存,序列化方式采用JSON
     *
     * @param <V>        缓存配置
     * @param key        缓存key
     * @param reloadTask 缓存未命中加装数据的任务
     * @param cacheKey   缓存key枚举
     */
    public <V, T extends CacheConf> V getFromTask(String key, Callable<V> reloadTask, T cacheKey) throws InterruptedException, ExecutionException, TimeoutException {
        V result;
        boolean isRemove = false;
        try {
            FutureTask<V> futureTask = new FutureTask<>(reloadTask);
            FutureTask<V> futureValue = cacheMap.putIfAbsent(key, futureTask);
            if (futureValue == null) {
                futureValue = futureTask;
                futureTask.run();
                isRemove = true;
            }
            result = futureValue.get(timeout, TimeUnit.MILLISECONDS);
            return result;
        } catch (Exception e) {
            printException(key, cacheKey, e);
            if (e instanceof ExecutionException) {
                throw e;
            }
            return null;
        } finally {
            if (isRemove) {
                cacheMap.remove(key);
            }
        }
    }

    /**
     * 如果缓存数据最后更新时间距离当前时间超过autoUpdateSeconds，
     * 使用后台线程池更新缓存数据至最新
     *
     * @param <V>               返回结果数据类型
     * @param cacheData         缓存数据
     * @param key               缓存的key
     * @param seconds           缓存时间
     * @param autoUpdateSeconds 更新时间
     * @param reloadTask        更新的任务
     * @param predicate         是否缓存的计算规则
     */
    private <V> void updateCacheIfNeed(final CacheData cacheData, String key, int seconds, int autoUpdateSeconds, Callable<V> reloadTask, Predicate<V> predicate) {
        long currentTs = SystemClock.millisClock().now();
        if (autoUpdateSeconds > 0 && (cacheData == null || (currentTs - cacheData.getLastUpdateTime()) > autoUpdateSeconds * 1000L)) {
//            log.warn("需要刷新key： {}", key);
            CompletableFuture.runAsync(() -> {
                String tempKey = key + ".seize";
                /**
                 * 除非同时有另外一个线程拿到相同的缓存数据等到当前线程更新完缓存，
                 * 并且tempKey对应缓存在当前线程拉取原始数据后10s(10s也许可以考虑改短)才正好执行到此处，
                 * 那么会导致重复更新缓存，但是这种几率几乎不存在。如果还需要彻底避免此现象，
                 * 可以在返回result为true的情况下，再次获取cacheData判断是否需要更新缓存，
                 * 只是会带来额外的开销。
                 */
                boolean result = cacheAdapter.setStrNxEx(tempKey, "1", 60);
                if (result) {
                    try {
                        saveCache(reloadTask.call(), key, seconds, predicate);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        cacheAdapter.expire(tempKey, 10, TimeUnit.SECONDS);
                    }
                }
            });
        }
    }

    private <V> void saveCache(V result, String key, int seconds, Predicate<V> predicate) {
        if (result == null) { // TODO 支持null缓存
            log.info("需缓存数据为空，清空缓存，key={}", key);
            cacheAdapter.del(key);
            return;
        }
        if (predicate == null || predicate.test(result)) {
            try {
                CacheData<V> cacheData = new CacheData<>(SystemClock.millisClock().now(), result);
                cacheAdapter.setStrEx(key, Json.toJson(cacheData), seconds);
                log.info("成功加载数据至缓存，key：{}", key);
            } catch (Exception e) {
                log.error("error in saveCache setRedis, k:{}, s:{}", key, seconds, e);
            }
        }
    }

    private <V> CacheData<V> getCacheData(String key, Type type) {
        if (type == null) {
            return cacheAdapter.get(key, CacheData.class);
        }
        String json = cacheAdapter.get(key);
        return json == null ? null : Json.fromJson(json, new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{type};
            }

            @Override
            public Type getRawType() {
                return CacheData.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        });
    }

    /**
     * 按接口名称配置的缓存降级开关
     * 1:未降级
     * 2:只从reloadTask获取数据
     * 3:只从缓存获取数据
     */
    private CacheStrategyEnum getDegradeSwitch(CacheConf cacheKey) {
        CacheStrategyEnum degrade = CacheStrategyEnum.getStrategy(ConfigUtil.getInteger("APPLICATION_CACHE_SWITCH"));
        if (degrade == CacheStrategyEnum.RELOAD_ONLY) {
            log.info("已全局关闭缓存开关");
            return degrade;
        } else if (degrade == CacheStrategyEnum.CACHE_ONLY) {
            log.info("已开启开关：全局只走缓存，不查rpc");
            return degrade;
        }
        String switchKey = cacheKey.getConfKey();
        Integer keyDegrade = ConfigUtil.getInteger(switchKey);
        if (keyDegrade == null) {
            return degrade;
        }
        degrade = CacheStrategyEnum.getStrategy(keyDegrade);
        if (degrade == CacheStrategyEnum.RELOAD_ONLY) {
            log.info("接口缓存开关已关闭：{}", switchKey);
        } else if (degrade == CacheStrategyEnum.CACHE_ONLY) {
            log.info("已开启开关: 接口{}只走缓存，不查rpc", switchKey);
        }
        return degrade;
    }

    /**
     * 异常打印
     *
     * @param key      异常的key
     * @param cacheKey 缓存key枚举
     * @param e        异常
     * @param <T>      缓存配置泛型
     */
    private <T extends CacheConf> void printException(String key, T cacheKey, Exception e) {
        Set<String> ignorePrintException = cacheKey.getIgnorePrintException();
        if (ignorePrintException != null &&
                (ignorePrintException.contains(e.getClass().getName()) || ignorePrintException.contains(e.getClass().getSimpleName())
                        || (e.getCause() != null && e.getCause() instanceof ExecutionException && (ignorePrintException.contains(e.getCause().getClass().getName()) || ignorePrintException.contains(e.getCause().getClass().getSimpleName()))))) {
            log.error("getFromTask error,key={},e={}", key, e.getMessage());
        } else {
            log.error("getFromTask error,key={}", key, e);
        }
    }

}
