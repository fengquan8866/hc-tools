package cn.hc.tool.cache.util;

import cn.hc.tool.cache.adapter.CacheAdapter;
import cn.hc.tool.cache.bean.CacheConf;
import cn.hc.tool.cache.bean.CacheData;
import cn.hc.tool.cache.enums.CacheStrategyEnum;
import cn.hc.tool.cache.exception.ToolCacheException;
import cn.hc.tool.common.util.CollectionUtil;
import cn.hc.tool.common.util.MapUtil;
import cn.hc.tool.common.util.SystemClock;
import cn.hc.tool.config.util.ConfigUtil;
import com.hc.json.adapter.Json;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;
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

    public String get(CacheConf cacheConf, Callable<String> reloadTask, Object... keyParams) {
        return this.get(cacheConf, String.class, reloadTask, keyParams);
    }

    /**
     * @param cacheConf  缓存key枚举
     * @param vClass     返回类型
     * @param reloadTask 缓存未命中加装数据的任务
     * @param keyParams  生成完整缓存key需要的参数
     * @param <V>        返回结果数据类型
     * @return 结果
     */
    public <V> V get(CacheConf cacheConf, Class<V> vClass, Callable<V> reloadTask, Object... keyParams) {
        return this.get(cacheConf, (Type) vClass, reloadTask, keyParams);
    }

    public <V> V get(CacheConf cacheConf, Type vClass, Callable<V> reloadTask, Object... keyParams) {
        CacheStrategyEnum cacheDegrade = getDegradeSwitch(cacheConf);
        return this.get(cacheDegrade, vClass, cacheConf.getFullCacheKey(keyParams), cacheConf, reloadTask, null);
    }

    public <V> V get(CacheConf cacheConf, Class<V> vClass, Callable<V> reloadTask, Predicate<V> predicate, Object... keyParams) {
        return this.get(cacheConf, (Type) vClass, reloadTask, predicate, keyParams);
    }

    /**
     * @param cacheConf  缓存key枚举
     * @param vClass     返回类型
     * @param reloadTask 缓存未命中加装数据的任务
     * @param predicate  是否缓存子句
     * @param keyParams  生成完整缓存key需要的参数
     * @param <V>        返回结果数据类型
     * @return 结果
     */
    public <V> V get(CacheConf cacheConf, Type vClass, Callable<V> reloadTask, Predicate<V> predicate, Object... keyParams) {
        CacheStrategyEnum cacheDegrade = getDegradeSwitch(cacheConf);
        return this.get(cacheDegrade, vClass, cacheConf.getFullCacheKey(keyParams), cacheConf, reloadTask, predicate);
    }

    /**
     * 获取缓存数据
     *
     * @param cacheConf 缓存key枚举
     * @param keyParams 生成完整缓存key需要的参数
     * @return 结果
     */
    public String get(CacheConf cacheConf, Object... keyParams) {
        String key = cacheConf.getFullCacheKey(keyParams);
        CacheData<String> cacheData = this.getCacheData(key, String.class);
        return cacheData == null ? null : cacheData.getData();
    }

    /**
     * 获取缓存数据
     *
     * @param cacheConf 缓存key枚举
     * @param vClass    返回类型
     * @param keyParams 生成完整缓存key需要的参数
     * @return 结果
     */
    public <V> V get(CacheConf cacheConf, Type vClass, Object... keyParams) {
        String key = cacheConf.getFullCacheKey(keyParams);
        CacheData<V> cacheData = this.getCacheData(key, vClass);
        return cacheData == null ? null : cacheData.getData();
    }

    public <V, T extends CacheConf> V get(CacheStrategyEnum cacheDegrade, Type type, String key,
                                          T cacheConf, Callable<V> reloadTask, Predicate<V> predicate) {
        // 1、拒绝策略
        if (cacheDegrade == CacheStrategyEnum.REJECT) {
            return null;
        }
        int seconds = cacheConf.getExpireSeconds();
        int autoUpdateSeconds = cacheConf.getUpdateSeconds();
        // 2、RELOAD策略 或 缓存时间=0，重新加载
        if (cacheDegrade == CacheStrategyEnum.RELOAD_ONLY || seconds == 0) {
            return this.call(reloadTask);
        }
        // 3、获取缓存数据
        CacheData<V> cacheData = this.getCacheData(key, type);
        if (cacheData != null) {
            // 3.1非CACHE_ONLY策略，异步更新缓存
            if (cacheDegrade != CacheStrategyEnum.CACHE_ONLY) {
                updateCacheIfNeed(cacheData, key, seconds, autoUpdateSeconds, cacheConf.getNullExpireSeconds(), reloadTask, predicate);
            }
            return cacheData.getData();
        }
        // 4、未降级策略，执行reload任务，更新缓存
        if (cacheDegrade == CacheStrategyEnum.ALL_OPEN) {
            log.info("缓存穿透，开始重新加载数据，key：{}", key);
            V result = getFromTask(key, reloadTask, cacheConf);
            this.saveCache(result, key, seconds, cacheConf.getNullExpireSeconds(), predicate);
            return result;
        }
        log.info("缓存数据为空，key：{}", key);
        // 5、缓存读异步写 策略，异步更新缓存
        if (cacheDegrade == CacheStrategyEnum.CACHE_ONLY_AND_ASYNC_WRITE) {
            updateCacheIfNeed(null, key, seconds, autoUpdateSeconds, cacheConf.getNullExpireSeconds(), reloadTask, predicate);
        }
        return null;
    }

    /**
     * 删除缓存
     *
     * @param cacheConf 缓存key枚举
     * @param keyParams 生成完整缓存key需要的参数
     * @return 结果
     */
    public boolean remove(CacheConf cacheConf, Object... keyParams) {
        String fullCacheKey = cacheConf.getFullCacheKey(keyParams);
        return cacheAdapter.del(fullCacheKey);
    }

    /**
     * 更新缓存
     *
     * @param cacheConf 缓存key枚举
     * @param data      数据
     * @param keyParams 生成完整缓存key需要的参数
     * @return 结果
     */
    public boolean update(CacheConf cacheConf, Object data, Object... keyParams) {
        String key = cacheConf.getFullCacheKey(keyParams);
        this.saveCache(data, key, cacheConf.getExpireSeconds(), cacheConf.getNullExpireSeconds(), null);
        return true;
    }

    /**
     * 批量缓存
     *
     * @param cacheConf  缓存key枚举
     * @param vClass     返回类型
     * @param reloadTask 缓存未命中加装数据的任务
     * @param keyParams  生成完整缓存key需要的参数
     * @param <K>        加载任务的入参类型
     * @param <V>        返回结果数据类型
     * @return List结果集
     */
    public <K, V> List<V> getFromList(CacheConf cacheConf, Class<V> vClass, Function<List<K>, List<V>> reloadTask, List<K> keyParams) {
        return this.getFromList(cacheConf, vClass, reloadTask, (k, v) -> new Object[]{k}, keyParams);
    }

    /**
     * 批量缓存
     *
     * @param cacheConf  缓存key枚举
     * @param vClass     返回类型
     * @param reloadTask 缓存未命中加装数据的任务
     * @param keyFunc    生成缓存key的构造参数
     * @param keyParams  生成完整缓存key需要的参数
     * @param <K>        加载任务的入参类型
     * @param <V>        返回结果数据类型
     * @return List结果集
     */
    public <K, V> List<V> getFromList(CacheConf cacheConf, Class<V> vClass, Function<List<K>, List<V>> reloadTask, BiFunction<K, V, Object[]> keyFunc, List<K> keyParams) {
        return this.getFromList(cacheConf, (Type) vClass, reloadTask, keyFunc, keyParams);
    }

    public <K, V> List<V> getFromList(CacheConf cacheConf, Type vClass, Function<List<K>, List<V>> reloadTask, List<K> keyParams) {
        return getFromList(cacheConf, vClass, reloadTask, (k, v) -> new Object[]{k}, null, keyParams);
    }

    public <K, V> List<V> getFromList(CacheConf cacheConf, Type vClass, Function<List<K>, List<V>> reloadTask, BiFunction<K, V, Object[]> keyFunc, List<K> keyParams) {
        return getFromList(cacheConf, vClass, reloadTask, keyFunc, null, keyParams);
    }

    public <K, V> List<V> getFromList(CacheConf cacheConf, Type vClass, Function<List<K>, List<V>> reloadTask, BiFunction<K, V, Object[]> keyFunc, Predicate<V> predicate, List<K> keyParams) {
        CacheStrategyEnum cacheDegrade = getDegradeSwitch(cacheConf);
        if (cacheDegrade == CacheStrategyEnum.RELOAD_ONLY || cacheConf.getExpireSeconds() == 0) {
            return reloadTask.apply(keyParams);
        }
        List<V> res = new ArrayList<>(); // 结果集
        List<K> ks = new ArrayList<>(); // 未缓存的数据
//        List<T> reloadList = new ArrayList<>(); // TODO 批量更新
        for (final K k : keyParams) {
            String key = cacheConf.getFullCacheKey(k);
            CacheData<V> cacheData = getCacheData(key, vClass);
            if (cacheData == null) {
                ks.add(k);
            } else {
                if (cacheDegrade != CacheStrategyEnum.CACHE_ONLY) {
//                    reloadList.add(k);
                    try {
                        List<K> reloadParam = newList(keyParams);
                        reloadParam.add(k);
                        this.updateCacheIfNeed(cacheData, key, cacheConf.getExpireSeconds(), cacheConf.getUpdateSeconds(), cacheConf.getNullExpireSeconds(), () -> {
//                            log.warn("刷新数据： {}", k);
                            List<V> resList = reloadTask.apply(reloadParam);
                            return CollectionUtil.isEmpty(resList) ? null : resList.get(0);
                        }, predicate);
                    } catch (Exception e) {
                        log.error("error in getWithCacheList add reloadParam", e);
                    }
                }
                res.add(cacheData.getData());
            }
        }
        if (!ks.isEmpty()) {
            if (cacheDegrade != CacheStrategyEnum.ALL_OPEN) {
                return res;
            }
            log.info("缓存穿透，开始从新加载数据，ks：{}", ks);
            List<V> loadList = reloadTask.apply(ks);
            res.addAll(loadList);
            int i = 0;
            for (V e : loadList) {
                String key = cacheConf.getFullCacheKey(keyFunc.apply(ks.get(i++), e));
                this.saveCache(e, key, cacheConf.getExpireSeconds(), cacheConf.getNullExpireSeconds(), predicate);
            }
        }
        return res;
    }

    public <V, P> List<V> getListFromSet(CacheConf cacheConf, Class<V> vClass, Function<Set<P>, List<V>> reloadTask, Set<P> keyParams) {
        return this.getListFromSet(cacheConf, vClass, reloadTask, (k, v) -> new Object[]{k}, keyParams);
    }

    /**
     * 批量缓存
     *
     * @param cacheConf  缓存key枚举
     * @param vClass     返回类型
     * @param reloadTask 缓存未命中加装数据的任务
     * @param keyFunc    生成缓存key的方法
     * @param keyParams  生成完整缓存key需要的参数
     * @param <K>        加载任务的入参类型
     * @param <V>        返回结果数据类型
     */
    public <K, V> List<V> getListFromSet(CacheConf cacheConf, Class<V> vClass, Function<Set<K>, List<V>> reloadTask, BiFunction<K, V, Object[]> keyFunc, Set<K> keyParams) {
        return this.getListFromSet(cacheConf, (Type) vClass, reloadTask, keyFunc, keyParams);
    }

    public <K, V> List<V> getListFromSet(CacheConf cacheConf, Type vClass, Function<Set<K>, List<V>> reloadTask, Set<K> keyParams) {
        return getListFromSet(cacheConf, vClass, reloadTask, (k, v) -> new Object[]{k}, null, keyParams);
    }

    public <K, V> List<V> getListFromSet(CacheConf cacheConf, Type vClass, Function<Set<K>, List<V>> reloadTask, BiFunction<K, V, Object[]> keyFunc, Set<K> keyParams) {
        return getListFromSet(cacheConf, vClass, reloadTask, keyFunc, null, keyParams);
    }

    public <K, V> List<V> getListFromSet(CacheConf cacheConf, Class<V> vClass, Function<Set<K>, List<V>> reloadTask, BiFunction<K, V, Object[]> keyFunc, Predicate<V> predicate, Set<K> keyParams) {
        return this.getListFromSet(cacheConf, (Type) vClass, reloadTask, keyFunc, predicate, keyParams);
    }

    public <K, V> List<V> getListFromSet(CacheConf cacheConf, Type vClass, Function<Set<K>, List<V>> reloadTask, BiFunction<K, V, Object[]> keyFunc, Predicate<V> predicate, Set<K> keyParams) {
        CacheStrategyEnum cacheDegrade = getDegradeSwitch(cacheConf);
        if (cacheDegrade == CacheStrategyEnum.RELOAD_ONLY || cacheConf.getExpireSeconds() == 0) {
            return reloadTask.apply(keyParams);
        }
        List<V> res = new ArrayList<>(); // 结果集
        Set<K> ks = new LinkedHashSet<>(); // 未缓存的数据
//        List<T> reloadList = new ArrayList<>(); // TODO 批量更新
        for (K k : keyParams) {
            String key = cacheConf.getFullCacheKey(k);
            CacheData<V> cacheData = getCacheData(key, vClass);
            if (cacheData == null) {
                ks.add(k);
            } else {
                if (cacheDegrade != CacheStrategyEnum.CACHE_ONLY) {
//                    reloadList.add(k);
                    try {
                        Set<K> reloadParam = newSet(keyParams);
                        reloadParam.add(k);
                        this.updateCacheIfNeed(cacheData, key, cacheConf.getExpireSeconds(), cacheConf.getUpdateSeconds(), cacheConf.getNullExpireSeconds(), () -> {
                            List<V> resList = reloadTask.apply(reloadParam);
                            return CollectionUtil.isEmpty(resList) ? null : resList.get(0);
                        }, predicate);
                    } catch (Exception e) {
                        log.error("error in getListWithCacheSet add reloadParam", e);
                    }
                }
                res.add(cacheData.getData());
            }
        }
        if (!ks.isEmpty()) {
            if (cacheDegrade != CacheStrategyEnum.ALL_OPEN) {
                return res;
            }
            log.info("缓存穿透，开始从新加载数据，ks：{}", ks);
            List<V> loadList = reloadTask.apply(ks);
            res.addAll(loadList);
            int i = 0;
            for (K p : ks) {
                V e = loadList.get(i++);
                String key = cacheConf.getFullCacheKey(keyFunc.apply(p, e));
                this.saveCache(e, key, cacheConf.getExpireSeconds(), cacheConf.getNullExpireSeconds(), predicate);
            }
        }
        return res;
    }

    /**
     * 批量缓存
     *
     * @param cacheConf  缓存key枚举
     * @param vClass     返回类型
     * @param reloadTask 缓存未命中加装数据的任务
     * @param keyParams  生成完整缓存key需要的参数
     * @param <V>        返回结果数据类型
     * @param <K>        加载任务的入参类型
     * @return Map<K, V>
     */
    public <K, V> Map<K, V> getMapFromList(CacheConf cacheConf, Class<V> vClass, Function<List<K>, Map<K, V>> reloadTask,
                                           List<K> keyParams) {
        return this.getMapFromList(cacheConf, (Type) vClass, reloadTask, keyParams);
    }

    public <K, V> Map<K, V> getMapFromList(CacheConf cacheConf, Type vClass, Function<List<K>, Map<K, V>> reloadTask,
                                           List<K> keyParams) {
        return this.getMapFromList(cacheConf, vClass, reloadTask, (k, v) -> k, (k, v) -> new Object[]{k}, keyParams);
    }

    public <K, V, P> Map<K, V> getMapFromList(CacheConf cacheConf, Class<V> vClass, Function<List<P>, Map<K, V>> reloadTask,
                                              BiFunction<P, V, K> mapKeyFunc, BiFunction<K, V, Object[]> cacheKeyFunc,
                                              List<P> keyParams) {
        return this.getMapFromList(cacheConf, (Type) vClass, reloadTask, mapKeyFunc, cacheKeyFunc, keyParams);
    }

    /**
     * 批量缓存
     *
     * @param cacheConf    缓存key枚举
     * @param vClass       返回类型
     * @param reloadTask   缓存未命中加装数据的任务
     * @param mapKeyFunc   生成map key的方法
     * @param cacheKeyFunc 生成缓存key的方法
     * @param keyParams    生成完整缓存key需要的参数
     * @param <V>          返回结果数据类型
     * @param <P>          加载任务的入参类型
     * @param <K>          map的key类型
     * @return Map<K, V>
     */
    public <K, V, P> Map<K, V> getMapFromList(CacheConf cacheConf, Type vClass, Function<List<P>, Map<K, V>> reloadTask,
                                              BiFunction<P, V, K> mapKeyFunc, BiFunction<K, V, Object[]> cacheKeyFunc,
                                              List<P> keyParams) {
        return getMapFromList(cacheConf, vClass, reloadTask, mapKeyFunc, cacheKeyFunc, null, keyParams);
    }

    /**
     * 批量缓存
     *
     * @param cacheConf    缓存key枚举
     * @param vClass       返回类型
     * @param reloadTask   缓存未命中加装数据的任务
     * @param mapKeyFunc   生成map key的方法
     * @param cacheKeyFunc 生成缓存key的方法
     * @param predicate    是否缓存子句
     * @param keyParams    生成完整缓存key需要的参数
     * @param <V>          返回结果数据类型
     * @param <P>          加载任务的入参类型
     * @param <K>          map的key类型
     * @return Map<K, V>
     */
    public <K, V, P> Map<K, V> getMapFromList(CacheConf cacheConf, Class<V> vClass, Function<List<P>, Map<K, V>> reloadTask,
                                              BiFunction<P, V, K> mapKeyFunc, BiFunction<K, V, Object[]> cacheKeyFunc,
                                              Predicate<V> predicate, List<P> keyParams) {
        return this.getMapFromList(cacheConf, (Type) vClass, reloadTask, mapKeyFunc, cacheKeyFunc, predicate, keyParams);
    }

    public <K, V, P> Map<K, V> getMapFromList(CacheConf cacheConf, Type vClass, Function<List<P>, Map<K, V>> reloadTask,
                                              BiFunction<P, V, K> mapKeyFunc, BiFunction<K, V, Object[]> cacheKeyFunc,
                                              Predicate<V> predicate, List<P> keyParams) {
        CacheStrategyEnum cacheDegrade = getDegradeSwitch(cacheConf);
        if (cacheDegrade == CacheStrategyEnum.RELOAD_ONLY || cacheConf.getExpireSeconds() == 0) {
            return reloadTask.apply(keyParams);
        }
        Map<K, V> res = new LinkedHashMap<>(); // 结果集
        List<P> ks = new LinkedList<>(); // 未缓存的数据
        for (P p : keyParams) {
            String cacheK = cacheConf.getFullCacheKey(p);
            CacheData<V> cacheData = getCacheData(cacheK, vClass);
            if (cacheData == null) {
                ks.add(p);
            } else {
                if (cacheDegrade != CacheStrategyEnum.CACHE_ONLY) {
                    try {
                        List<P> reloadParam = newList(keyParams);
                        reloadParam.add(p);
                        this.updateCacheIfNeed(cacheData, cacheK, cacheConf.getExpireSeconds(), cacheConf.getUpdateSeconds(), cacheConf.getNullExpireSeconds(), () -> {
                            Map<K, V> resMap = reloadTask.apply(reloadParam);
                            return MapUtil.isEmpty(resMap) ? null : new ArrayList<>(resMap.values()).get(0);
                        }, predicate);
                    } catch (Exception e) {
                        log.error("error in getMapWithCacheList add reloadParam", e);
                    }
                }
                res.put(mapKeyFunc.apply(p, cacheData.getData()), cacheData.getData());
            }
        }
        if (!ks.isEmpty()) {
            if (cacheDegrade != CacheStrategyEnum.ALL_OPEN) {
                return res;
            }
            log.info("缓存穿透，开始从新加载数据，ks：{}", ks);
            Map<K, V> loadMap = reloadTask.apply(ks);
            if (MapUtil.isEmpty(loadMap)) {
                return res;
            }
            res.putAll(loadMap);
            for (Map.Entry<K, V> e : loadMap.entrySet()) {
                String key = cacheConf.getFullCacheKey(cacheKeyFunc.apply(e.getKey(), e.getValue()));
                this.saveCache(e.getValue(), key, cacheConf.getExpireSeconds(), cacheConf.getNullExpireSeconds(), predicate);
            }
        }
        return res;
    }

    /**
     * 批量缓存
     *
     * @param cacheConf  缓存key枚举
     * @param vClass     返回类型
     * @param reloadTask 缓存未命中加装数据的任务
     * @param keyParams  生成完整缓存key需要的参数
     * @param <V>        返回结果数据类型
     * @param <K>        map的key类型
     * @return Map<K, V>
     */
    public <K, V> Map<K, V> getMapFromSet(CacheConf cacheConf, Class<V> vClass, Function<Set<K>, Map<K, V>> reloadTask,
                                          Set<K> keyParams) {
        return this.getMapFromSet(cacheConf, (Type) vClass, reloadTask, keyParams);
    }

    public <K, V, P> Map<K, V> getMapFromSet(CacheConf cacheConf, Class<V> vClass, Function<Set<P>, Map<K, V>> reloadTask,
                                             BiFunction<P, V, K> mapKeyFunc, BiFunction<K, V, Object[]> cacheKeyFunc,
                                             Set<P> keyParams) {
        return this.getMapFromSet(cacheConf, (Type) vClass, reloadTask, mapKeyFunc, cacheKeyFunc, keyParams);
    }

    public <V, K> Map<K, V> getMapFromSet(CacheConf cacheConf, Type vClass, Function<Set<K>, Map<K, V>> reloadTask,
                                          Set<K> keyParams) {
        return getMapFromSet(cacheConf, vClass, reloadTask, (k, v) -> k, (k, v) -> new Object[]{k}, null, keyParams);
    }

    public <K, V, P> Map<K, V> getMapFromSet(CacheConf cacheConf, Type vClass, Function<Set<P>, Map<K, V>> reloadTask,
                                             BiFunction<P, V, K> mapKeyFunc, BiFunction<K, V, Object[]> cacheKeyFunc,
                                             Set<P> keyParams) {
        return getMapFromSet(cacheConf, vClass, reloadTask, mapKeyFunc, cacheKeyFunc, null, keyParams);
    }

    public <K, V, P> Map<K, V> getMapFromSet(CacheConf cacheConf, Class<V> vClass, Function<Set<P>, Map<K, V>> reloadTask,
                                             BiFunction<P, V, K> mapKeyFunc, BiFunction<K, V, Object[]> cacheKeyFunc,
                                             Predicate<V> predicate, Set<P> keyParams) {
        return this.getMapFromSet(cacheConf, (Type) vClass, reloadTask, mapKeyFunc, cacheKeyFunc, predicate, keyParams);
    }

    /**
     * 批量缓存
     *
     * @param cacheConf    缓存key枚举
     * @param vClass       返回类型
     * @param reloadTask   缓存未命中加装数据的任务
     * @param mapKeyFunc   生成map key的方法
     * @param cacheKeyFunc 生成缓存key的方法
     * @param predicate    是否缓存子句
     * @param keyParams    生成完整缓存key需要的参数
     * @param <V>          返回结果数据类型
     * @param <P>          加载任务的入参类型
     * @param <K>          map的key类型
     * @return Map<K, V>
     */
    public <K, V, P> Map<K, V> getMapFromSet(CacheConf cacheConf, Type vClass, Function<Set<P>, Map<K, V>> reloadTask,
                                             BiFunction<P, V, K> mapKeyFunc, BiFunction<K, V, Object[]> cacheKeyFunc,
                                             Predicate<V> predicate, Set<P> keyParams) {
        CacheStrategyEnum cacheDegrade = getDegradeSwitch(cacheConf);
        if (cacheDegrade == CacheStrategyEnum.RELOAD_ONLY || cacheConf.getExpireSeconds() == 0) {
            return reloadTask.apply(keyParams);
        }
        Map<K, V> res = new LinkedHashMap<>(); // 结果集
        Set<P> ks = new LinkedHashSet<>(); // 未缓存的数据
        for (P p : keyParams) {
            String cacheK = cacheConf.getFullCacheKey(p);
            CacheData<V> cacheData = getCacheData(cacheK, vClass);
            if (cacheData == null) {
                ks.add(p);
            } else {
                if (cacheDegrade != CacheStrategyEnum.CACHE_ONLY) {
                    try {
                        Set<P> reloadParam = newSet(keyParams);
                        reloadParam.add(p);
                        this.updateCacheIfNeed(cacheData, cacheK, cacheConf.getExpireSeconds(), cacheConf.getUpdateSeconds(), cacheConf.getNullExpireSeconds(), () -> {
                            Map<K, V> resMap = reloadTask.apply(reloadParam);
                            return MapUtil.isEmpty(resMap) ? null : new ArrayList<>(resMap.values()).get(0);
                        }, predicate);
                    } catch (Exception e) {
                        log.error("error in getMapWithCacheSet add reloadParam", e);
                    }
                }
                res.put(mapKeyFunc.apply(p, cacheData.getData()), cacheData.getData());
            }
        }
        if (!ks.isEmpty()) {
            if (cacheDegrade != CacheStrategyEnum.ALL_OPEN) {
                return res;
            }
            log.info("缓存穿透，开始从新加载数据，ks：{}", ks);
            Map<K, V> loadMap = reloadTask.apply(ks);
            if (MapUtil.isEmpty(loadMap)) {
                return res;
            }
            res.putAll(loadMap);
            for (Map.Entry<K, V> e : loadMap.entrySet()) {
                String key = cacheConf.getFullCacheKey(cacheKeyFunc.apply(e.getKey(), e.getValue()));
                this.saveCache(e.getValue(), key, cacheConf.getExpireSeconds(), cacheConf.getNullExpireSeconds(), predicate);
            }
        }
        return res;
    }

    /**
     * 从缓存获取，或者加载数据后更新缓存,序列化方式采用JSON
     *
     * @param <V>        缓存配置
     * @param key        缓存key
     * @param reloadTask 缓存未命中加装数据的任务
     * @param cacheConf  缓存key枚举
     */
    public <V, T extends CacheConf> V getFromTask(String key, Callable<V> reloadTask, T cacheConf) {
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
            printException(key, cacheConf, e);
            if (e instanceof ExecutionException) {
                throw new ToolCacheException(e);
            }
            return null;
        } finally {
            if (isRemove) {
                cacheMap.remove(key);
            }
        }
    }

    private <V> V call(Callable<V> reloadTask) {
        try {
            return reloadTask.call();
        } catch (Exception e) {
            throw new ToolCacheException(e);
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
     * @param nullExpireSeconds null缓存时间
     * @param reloadTask        更新的任务
     * @param predicate         是否缓存的计算规则
     */
    private <V> void updateCacheIfNeed(final CacheData cacheData, String key, int seconds, int autoUpdateSeconds, int nullExpireSeconds, Callable<V> reloadTask, Predicate<V> predicate) {
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
                        this.saveCache(reloadTask.call(), key, seconds, nullExpireSeconds, predicate);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        cacheAdapter.expire(tempKey, 10, TimeUnit.SECONDS);
                    }
                }
            });
        }
    }

    private <V> void saveCache(V result, String key, int seconds, int nullExpireSeconds, Predicate<V> predicate) {
        if (result == null && nullExpireSeconds <= 0) {
            log.info("需缓存数据为空，清空缓存，key={}", key);
            cacheAdapter.del(key);
            return;
        }
        if (predicate == null || predicate.test(result)) {
            try {
                CacheData<V> cacheData = new CacheData<>(SystemClock.millisClock().now(), result);
                cacheAdapter.setStrEx(key, Json.toJson(cacheData), result == null ? nullExpireSeconds : seconds);
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
    private CacheStrategyEnum getDegradeSwitch(CacheConf cacheConf) {
        CacheStrategyEnum degrade = CacheStrategyEnum.getStrategy(ConfigUtil.getInteger("APPLICATION_CACHE_SWITCH"));
        if (degrade == CacheStrategyEnum.RELOAD_ONLY) {
            log.info("已全局关闭缓存开关");
            return degrade;
        } else if (degrade == CacheStrategyEnum.CACHE_ONLY) {
            log.info("已开启开关：全局只走缓存，不查rpc");
            return degrade;
        }
        String switchKey = cacheConf.getConfKey();
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
     * @param key       异常的key
     * @param cacheConf 缓存key枚举
     * @param e         异常
     * @param <T>       缓存配置泛型
     */
    private <T extends CacheConf> void printException(String key, T cacheConf, Exception e) {
        Set<String> ignorePrintException = cacheConf.getIgnorePrintException();
        if (ignorePrintException != null &&
                (ignorePrintException.contains(e.getClass().getName()) || ignorePrintException.contains(e.getClass().getSimpleName())
                        || (e.getCause() != null && e.getCause() instanceof ExecutionException && (ignorePrintException.contains(e.getCause().getClass().getName()) || ignorePrintException.contains(e.getCause().getClass().getSimpleName()))))) {
            log.error("getFromTask error,key={},e={}", key, e.getMessage());
        } else {
            log.error("getFromTask error,key={}", key, e);
        }
    }

    private <P> List<P> newList(List<P> params) {
        try {
            return params.getClass().newInstance();
        } catch (Exception e) {
            log.error("error in newList: {}, {}", params.getClass(), e.getMessage());
            return new ArrayList<>();
        }
    }

    private <P> Set<P> newSet(Set<P> params) {
        try {
            return params.getClass().newInstance();
        } catch (Exception e) {
            log.error("error in newList: {}, {}", params.getClass(), e.getMessage());
            return new LinkedHashSet<>();
        }
    }

}
