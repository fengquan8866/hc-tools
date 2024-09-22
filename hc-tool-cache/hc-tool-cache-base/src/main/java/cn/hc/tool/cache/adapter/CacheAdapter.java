package cn.hc.tool.cache.adapter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/15 20:31
 */
public interface CacheAdapter {

    public boolean setNx(String key, String value);

    public boolean setNx(String key, String value, long time, TimeUnit timeUnit);

    public Long getTtl(String key, String value);

    /**
     * 如果key不存在，就能成功设置缓存值,同时设定缓存过期时间；否则设置缓存值失败
     *
     * @param key
     * @param value
     * @param seconds
     * @return 缓存是否设置成功
     */
    public Boolean setStrNxEx(String key, String value, int seconds);

    public String getSet(String key, String value);

    public boolean setStrEx(String key, String value, int seconds);

    public boolean set(String key, String value);

    public String get(String key);

    public <T> T get(String key, Class<T> clazz);

    public long lPush(String listName, String value);

    public String lPop(String listName);

    public long sAdd(String listName, String... values);

    public String sPop(String listName);

    public Set<String> sMembers(String listName);


    public long sRem(String listName, String... values);

    public <T> List<T> getList(String key, Class<T> clazz);

    public boolean hset(String key, String field, Object value);

    public <T> T hGet(String key, String field, Class<T> clazz);

    public Map<Object, Object> hGetAll(final String key);

    public Long hDel(final String key, final String... fields);

    public String hGet(String key, String field);

    public boolean setEx(String key, String value, long expireTime, TimeUnit timeUnit);

    public boolean setEx(String key, Object value, long expireTime, TimeUnit timeUnit);

    public boolean del(String key);

    /**
     * Delete given keys
     */
    public boolean del(String... keys);

    public boolean exist(String key);

    public boolean expire(String key, int expireTime, TimeUnit timeUnit);

    public <T> Map<String, T> hMGet(String key, String[] fields, Class<T> clazz);

    public Map<String, String> hMGet(String key, String[] fields);

    public <T> void hMSet(String key, Map<String, T> keyVals);

    public <T> Map<String, T> mGet(String[] keys, Class<T> clazz);

    public Map<String, String> mGet(String[] keys);

    public long incr(String key);

    public String scriptLoad(String script) throws Exception;

    /**
     * scriptLoad + evalsha
     *
     * @param script  脚本
     * @param keys    脚本的key列表
     * @param args    脚本的参数列表
     * @param rstType 返回结果类型
     * @return 返回结果
     */
    public <T> T eval(String script, List<String> keys, Class<T> rstType, Object... args);

    default <T> T eval(String script, List<String> keys, @Deprecated boolean readOnly, Class<T> rstType, Object... args) {
        return eval(script, keys, rstType, args);
    }

}
