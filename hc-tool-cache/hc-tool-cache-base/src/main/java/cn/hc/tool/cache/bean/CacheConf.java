package cn.hc.tool.cache.bean;

import cn.hc.tool.common.util.NumberUtil;
import cn.hc.tool.config.util.ConfigUtil;
import com.hc.json.adapter.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.*;

/**
 * 缓存接口类
 * 统一接口字段
 *
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/15 20:37
 */
public interface CacheConf {
    Logger log = LoggerFactory.getLogger(CacheConf.class);

    Map<String, CacheConf> confMap = new HashMap<>();

    /**
     * 加载数据配置名称，也是动态配置中缓存开关
     */
    String getConfKey();

    /**
     * 缓存key表达式，支持类型{0}占位符
     * 注：为空则以 switchKey + params 做缓存key
     */
    String getKeyExp();

    /**
     * 缓存数据过期时间（秒）
     */
    int getExpire();

    /**
     * 缓存数据后台自动刷新时间间隔，小于等于0表示不自动刷新（秒）
     */
    int getUpdate();

    /**
     * 随机时间范围，随机时间范围大于0，则每次获取缓存key时，随机增加0-randomRange秒
     */
    int getRandomRange();

    /**
     * 初始化
     */
    default void init() {
        log.info("init k:{}, {}", getConfKey(), this);
        confMap.put(getConfKey(), this);
        log.info("confMap: {}", confMap);
    }

    /**
     * 生成缓存key
     *
     * @param keyParams 入参
     * @return 缓存key
     */
    default String getFullCacheKey(Object... keyParams) {
        return MessageFormat.format(getKeyExp(), Arrays.stream(keyParams).map(Objects::toString).toArray());
    }

    /**
     * 缓存时间（支持配置中心动态配置）
     */
    default int getExpireSeconds() {
        int expire;
        try {
            String expireStr = ConfigUtil.get(getConfKey() + "-expire");
            Integer expireSeconds = NumberUtil.toInteger(expireStr);
            expire = expireSeconds == null ? getExpire() : expireSeconds;
        } catch (Exception e) {
            log.error("error in get expire from config: {}", getConfKey(), e);
            expire = getExpire();
        }
        if (getRandomRange() == 0) return expire;
        return expire + (int) (Math.random() * getRandomRange());
    }

    /**
     * 更新时间（支持配置中心配置）
     */
    default int getUpdateSeconds() {
        int update;
        try {
            String updateStr = ConfigUtil.get(getConfKey() + "-update");
            Integer updateSeconds = NumberUtil.toInteger(updateStr);
            update = updateSeconds == null ? getUpdate() : updateSeconds;
        } catch (Exception e) {
            log.error("error in get update from config: {}", getConfKey(), e);
            update = getUpdate();
        }
        if (getRandomRange() == 0) return update;
        return update + (int) (Math.random() * getRandomRange());
    }

    /**
     * 获取需要忽略打印的异常
     *
     * @return 异常列表
     */
    default Set<String> getIgnorePrintException() {
        return null;
    }
}
