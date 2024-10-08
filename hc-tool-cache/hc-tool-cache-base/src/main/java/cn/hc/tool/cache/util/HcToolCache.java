package cn.hc.tool.cache.util;

import cn.hc.tool.cache.bean.CacheConf;
import cn.hc.tool.cache.exception.ToolCacheException;
import cn.hc.tool.common.util.CollectionUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/21 11:02
 */
@Getter
@Slf4j
public class HcToolCache extends AbstractValueAdaptingCache {

    private final String name;

    private final ToolCacheUtil toolCacheUtil;

    private final Type returnType;

    public HcToolCache(boolean allowNullValues, String name, ToolCacheUtil toolCacheUtil, Type returnType) {
        super(allowNullValues);
        log.info("HcToolCache({}, {}, util)", allowNullValues, name);
        this.name = name;
        this.toolCacheUtil = toolCacheUtil;
        this.returnType = returnType;
    }

    // 根据key获取缓存,如果返回null，则要读取持久层
    @Override
    protected Object lookup(Object key) {
        log.info("HcToolCache.lookup: {}, {}", name, key);
        CacheConf cacheConf = CacheConf.confMap.get(name);
        if (cacheConf == null) throw new ToolCacheException("缓存配置不存在");
        return toolCacheUtil.get(cacheConf, returnType, getKey(key));
    }

    @Override
    public Object getNativeCache() {
        return this.toolCacheUtil;
    }

    @Override
    public synchronized <T> T get(Object key, Callable<T> valueLoader) {
        log.info("HcToolCache.get: {}, {}", name, key);
        ValueWrapper result = get(key);

        if (result != null) {
            return (T) result.get();
        }

        T value = valueFromLoader(key, valueLoader);
        put(key, value);
        return value;
    }

    /**
     * 从持久层读取value，然后存入缓存。允许value = null
     */
    @Override
    public void put(Object key, Object value) {
        log.info("HcToolCache.put: {}, {}, {}", name, key, value);
        Object cacheValue = value;
        if (!isAllowNullValues() && cacheValue == null) {
            throw new IllegalArgumentException(String.format(
                    "Cache '%s' does not allow 'null' values. Avoid storing null via '@Cacheable(unless=\"#result == null\")' or configure RedisCache to allow 'null' via RedisCacheConfiguration.",
                    name));
        }
        CacheConf cacheConf = CacheConf.confMap.get(name);
        toolCacheUtil.update(cacheConf, value, getKey(key));
    }

    //如果传入key对应的value已经存在，就返回存在的value，不进行替换。如果不存在，就添加key和value，返回null.
    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        log.info("HcToolCache.putIfAbsent: {}, {}, {}", name, key, value);
        Object cacheValue = value;

        if (!isAllowNullValues() && cacheValue == null) {
            return get(key);
        }

        return get(key);
    }

    @Override
    public void evict(Object key) {
        log.info("HcToolCache.evict: {}, {}", name, key);
        CacheConf cacheConf = CacheConf.confMap.get(name);
        toolCacheUtil.remove(cacheConf, getKey(key));
    }

    private static String getKey(Object key) {
        if (key instanceof String) return (String) key;
        if (key instanceof SimpleKey) return "";
        if (key instanceof ArrayList) {
            ArrayList<String> list = (ArrayList<String>) key;
            if (CollectionUtil.isEmpty(list)) return "";
            return list.get(0);
        }
        throw new ToolCacheException("key[" + key + "]类型不正确：" + key.getClass());
    }

    private String convertKey(Object key) {
        log.info("HcToolCache.convertKey: {}, {}", name, key);

        TypeDescriptor source = TypeDescriptor.forObject(key);

        Method toString = ReflectionUtils.findMethod(key.getClass(), "toString");

        if (toString != null && !Object.class.equals(toString.getDeclaringClass())) {
            return key.toString();
        }

        throw new IllegalStateException(
                String.format("Cannot convert %s to String. Register a Converter or override toString().", source));
    }

    @Override
    public void clear() {
        log.info("HcToolCache.clear: {}", name);
    }

    private static <T> T valueFromLoader(Object key, Callable<T> valueLoader) {
        try {
            return valueLoader.call();
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e);
        }
    }

}
