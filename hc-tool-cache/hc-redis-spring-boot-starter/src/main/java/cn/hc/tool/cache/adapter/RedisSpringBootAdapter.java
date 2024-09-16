package cn.hc.tool.cache.adapter;

import cn.hc.tool.common.util.CollectionUtil;
import cn.hc.tool.common.util.StringUtil;
import com.hc.json.adapter.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/15 22:09
 */
@Component
public class RedisSpringBootAdapter implements CacheAdapter {

    @Value("${hc.redis.prefix:}")
    private String prefix;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public boolean setNx(String key, String value) {
        String fullKey = buildKey(key);
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(fullKey, value));
    }

    @Override
    public boolean setNx(String key, String value, long time, TimeUnit timeUnit) {
        String fullKey = buildKey(key);
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(fullKey, value, time, timeUnit));
    }

    @Override
    public Long getTtl(String key, String value) {
        String fullKey = buildKey(key);
        return redisTemplate.getExpire(fullKey);
    }

    @Override
    public Boolean setStrNxEx(String key, String value, int seconds) {
        String fullKey = buildKey(key);
        return redisTemplate.opsForValue().setIfAbsent(fullKey, value, seconds, TimeUnit.SECONDS);
    }

    @Override
    public String getSet(String key, String value) {
        String fullKey = buildKey(key);
        return redisTemplate.opsForValue().getAndSet(fullKey, value);
    }

    @Override
    public boolean setStrEx(String key, String value, int seconds) {
        String fullKey = buildKey(key);
        redisTemplate.opsForValue().set(fullKey, value, seconds, TimeUnit.SECONDS);
        return true;
    }

    @Override
    public boolean set(String key, String value) {
        String fullKey = buildKey(key);
        redisTemplate.opsForValue().set(fullKey, value);
        return true;
    }

    @Override
    public String get(String key) {
        String fullKey = buildKey(key);
        return redisTemplate.opsForValue().get(fullKey);
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        String fullKey = buildKey(key);
        String json = redisTemplate.opsForValue().get(fullKey);
        if (StringUtil.isEmpty(json)) {
            return null;
        }
        return Json.fromJson(json, clazz);
    }

    @Override
    public long lPush(String listName, String value) {
        String fullKey = buildKey(listName);
        return redisTemplate.opsForList().leftPush(fullKey, value);
    }

    @Override
    public String lPop(String listName) {
        String fullKey = buildKey(listName);
        return redisTemplate.opsForList().leftPop(fullKey);
    }

    @Override
    public long sAdd(String listName, String... values) {
        String fullKey = buildKey(listName);
        return redisTemplate.opsForSet().add(fullKey, values);
    }

    @Override
    public String sPop(String listName) {
        String fullKey = buildKey(listName);
        return redisTemplate.opsForSet().pop(fullKey);
    }

    @Override
    public Set<String> sMembers(String listName) {
        String fullKey = buildKey(listName);
        return redisTemplate.opsForSet().members(fullKey);
    }

    @Override
    public long sRem(String listName, String... values) {
        String fullKey = buildKey(listName);
        return redisTemplate.opsForSet().remove(fullKey, values);
    }

    @Override
    public <T> List<T> getList(String key, Class<T> clazz) {
        String json = this.get(key);
        return Json.fromJsonToList(json, clazz);
    }

    @Override
    public boolean hset(String key, String field, Object value) {
        String fullKey = buildKey(key);
        redisTemplate.opsForHash().put(fullKey, field, value);
        return true;
    }

    @Override
    public <T> T hGet(String key, String field, Class<T> clazz) {
        String fullKey = buildKey(key);
        String json = (String) redisTemplate.opsForHash().get(fullKey, field);
        return Json.fromJson(json, clazz);
    }

    @Override
    public Map<Object, Object> hGetAll(String key) {
        String fullKey = buildKey(key);
        return redisTemplate.opsForHash().entries(fullKey);
    }

    @Override
    public Long hDel(String key, String... fields) {
        String fullKey = buildKey(key);
        return redisTemplate.opsForHash().delete(fullKey, fields);
    }

    @Override
    public String hGet(String key, String field) {
        String fullKey = buildKey(key);
        return (String) redisTemplate.opsForHash().get(fullKey, field);
    }

    @Override
    public boolean setEx(String key, String value, long expireTime, TimeUnit timeUnit) {
        String fullKey = buildKey(key);
        redisTemplate.opsForValue().set(fullKey, value, expireTime, timeUnit);
        return true;
    }

    @Override
    public boolean setEx(String key, Object value, long expireTime, TimeUnit timeUnit) {
        return this.setEx(key, Json.toJson(value), expireTime, timeUnit);
    }

    @Override
    public boolean del(String key) {
        String fullKey = buildKey(key);
        return redisTemplate.opsForValue().getAndDelete(fullKey) != null;
    }

    @Override
    public boolean del(String... keys) {
        for (String key : keys) {
            String fullKey = buildKey(key);
            redisTemplate.opsForValue().getAndDelete(fullKey);
        }
        return true;
    }

    @Override
    public boolean exist(String key) {
        String fullKey = buildKey(key);
        return redisTemplate.hasKey(fullKey);
    }

    @Override
    public boolean expire(String key, int expireTime, TimeUnit timeUnit) {
        String fullKey = buildKey(key);
        return redisTemplate.expire(fullKey, expireTime, timeUnit);
    }

    @Override
    public <T> Map<String, T> hMGet(String key, String[] fields, Class<T> clazz) {
        if (fields == null || fields.length == 0) {
            return Collections.emptyMap();
        }
        String fullKey = buildKey(key);
        List<Object> objects = redisTemplate.opsForHash().multiGet(fullKey, Arrays.asList(fields));
        if (!CollectionUtil.isEmpty(objects) && fields.length == objects.size()) {
            Map<String, T> result = new HashMap<>();
            for (int i = 0; i < objects.size(); i++) {
                result.put(fields[i], Json.fromJson((String) objects.get(i), clazz));
            }
            return result;
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> hMGet(String key, String[] fields) {
        if (fields == null || fields.length == 0) {
            return Collections.emptyMap();
        }
        String fullKey = buildKey(key);
        List<Object> objects = redisTemplate.opsForHash().multiGet(fullKey, Arrays.asList(fields));
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < objects.size(); i++) {
            result.put(fields[i], (String) objects.get(i));
        }
        return result;
    }

    @Override
    public <T> void hMSet(String key, Map<String, T> keyVals) {
        String fullKey = buildKey(key);
        redisTemplate.opsForHash().putAll(fullKey, keyVals.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> Json.toJson(e.getValue()))));
    }

    @Override
    public <T> Map<String, T> mGet(String[] keys, Class<T> clazz) {
        String[] fullKeys = buildKeys(keys);
        List<String> list = redisTemplate.opsForValue().multiGet(Arrays.asList(fullKeys));
        if (CollectionUtil.isEmpty(list)) return Collections.emptyMap();
        Map<String, T> result = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            result.put(keys[i], Json.fromJson(list.get(i), clazz));
        }
        return result;
    }

    @Override
    public Map<String, String> mGet(String[] keys) {
        String[] fullKeys = buildKeys(keys);
        List<String> list = redisTemplate.opsForValue().multiGet(Arrays.asList(fullKeys));
        if (CollectionUtil.isEmpty(list)) return Collections.emptyMap();
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            result.put(keys[i], list.get(i));
        }
        return result;
    }

    @Override
    public long incr(String key) {
        return redisTemplate.opsForValue().increment(buildKey(key));
    }

    @Override
    public String scriptLoad(String script) throws Exception {
        return new DefaultRedisScript<>(script, String.class).getSha1();
    }

//    /**
//     * @param sha
//     * @param keys
//     * @param args
//     * @param readOnly
//     * @param scriptOutputType
//     * @return
//     */
//    @Override
//    public Object evalsha(String sha, List<String> keys, List<String> args, boolean readOnly, ScriptOutputType scriptOutputType) {
//        return redisTemplate.execute(new DefaultRedisScript<>(script, scriptOutputType), keys, args);
//        return null;
//    }

    @Override
    public <T> T eval(String script, List<String> keys, List<String> args, boolean readOnly, Class<T> rstType) {
        return redisTemplate.execute(new DefaultRedisScript<>(script, rstType), this.buildKeys(keys), args);
    }

    private List<String> buildKeys(List<String> keys) {
        if (StringUtil.isEmpty(prefix) || CollectionUtil.isEmpty(keys)) {
            return keys;
        }
        List<String> list = new ArrayList<>();
        for (String k : keys) {
            list.add(this.buildKey(k));
        }
        return list;
    }

    private String[] buildKeys(String[] keys) {
        if (StringUtil.isEmpty(prefix)) {
            return keys;
        }
        return Arrays.stream(keys)
                .map(this::buildKey)
                .toArray(String[]::new);
    }

    private String buildKey(String originalKey) {
        return Optional.ofNullable(prefix)
                .filter(StringUtil::isNotEmpty)
                .map(StringBuilder::new)
                .map(e -> e.append(originalKey))
                .map(StringBuilder::toString)
                .orElse(originalKey);
    }

}
