package cn.hc.tool.config.util;

import cn.hc.tool.config.listener.ConfigListener;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * config工厂
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/15 19:51
 */
@Slf4j
public class ConfigFactory {

    private ConfigFactory() {
    }

    /**
     * 监听器缓存
     */
    private static Map<String, List<ConfigListener>> cache = new ConcurrentHashMap<String, List<ConfigListener>>();

    /**
     * 正则监听器缓存
     */
    private static Map<String, List<ConfigListener>> cacheRegex = new ConcurrentHashMap<String, List<ConfigListener>>();

    /**
     * 注册
     */
    public static void register(String key, ConfigListener listener) {
        if (!cache.containsKey(key)) {
            cache.put(key, new ArrayList<ConfigListener>());
        }
        cache.get(key).add(listener);
    }

    /**
     * 注册正则监听
     */
    public static void registerRegex(String key, ConfigListener listener) {
        if (!cacheRegex.containsKey(key)) {
            cacheRegex.put(key, new ArrayList<ConfigListener>());
        }
        cacheRegex.get(key).add(listener);
    }

    /**
     * 监听执行
     *
     * @param key    监听key
     * @param oldVal 老值
     * @param newVal 新值
     */
    protected static void exec(String key, Object oldVal, Object newVal) {
        if (cache.containsKey(key)) {
            for (ConfigListener listener : cache.get(key)) {
                try {
                    listener.onUpdate(key, oldVal, newVal);
                } catch (Exception e) {
                    log.error("error in listener.onUpdate, key:{}, v:{}", key, newVal, e);
                }
            }
        } else if (!cacheRegex.isEmpty()) {
            List<ConfigListener> listeners = getRegexListeners(key);
            if (listeners != null && !listeners.isEmpty()) {
                for (ConfigListener listener : listeners) {
                    try {
                        listener.onUpdate(key, oldVal, newVal);
                    } catch (Exception e) {
                        log.error("error in listener.onUpdate, key:{}, v:{}", key, newVal, e);
                    }
                }
            }
        }
    }

    /**
     * 获取正则监听器
     * @param key 监控key
     * @return 监听器列表
     */
    private static List<ConfigListener> getRegexListeners(String key) {
        if (cacheRegex.isEmpty()) {
            return null;
        }
        for (Map.Entry<String, List<ConfigListener>> e : cacheRegex.entrySet()) {
            Pattern p = Pattern.compile(e.getKey());
            Matcher matcher = p.matcher(key);
            if (matcher.find()) {
                return e.getValue();
            }
        }
        return null;
    }

}
