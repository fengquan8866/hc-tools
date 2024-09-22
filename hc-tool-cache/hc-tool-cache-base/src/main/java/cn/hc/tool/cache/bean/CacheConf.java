package cn.hc.tool.cache.bean;

import cn.hc.tool.config.util.ConfigUtil;
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
    default int getUpdate() {
        return this.getExpire();
    }

    /**
     * 随机时间范围，随机时间范围。如果大于0，则每次获取缓存key时，随机增加0-randomRange秒
     */
    default int getRandom() {
        return 0;
    }

    /**
     * null缓存时间（秒），默认0，不缓存
     */
    default int getNullExpire() {
        return 0;
    }

    @Deprecated
    default int getRandomRange() {
        return this.getRandom();
    }

    /**
     * 初始化
     */
    default void init() {
        log.info("init k:{}, {}", this.getConfKey(), this);
        confMap.put(this.getConfKey(), this);
    }

    /**
     * 生成缓存key
     *
     * @param keyParams 入参
     * @return 缓存key
     */
    default String getFullCacheKey(Object... keyParams) {
        return MessageFormat.format(this.getKeyExp(), Arrays.stream(keyParams).map(Objects::toString).toArray());
    }

    /**
     * 缓存时间（支持配置中心动态配置）
     */
    default int getExpireSeconds() {
        int expire;
        try {
            Integer expireSeconds = ConfigUtil.getInteger(this.getConfKey() + "-expire");
            expire = expireSeconds == null ? this.getExpire() : expireSeconds;
        } catch (Exception e) {
            log.error("error in get expire from config: {}", this.getConfKey(), e);
            expire = this.getExpire();
        }
        int randomSeconds = this.getRandomSeconds();
        if (randomSeconds == 0) return expire;
        return expire + (int) (Math.random() * randomSeconds);
    }

    /**
     * 更新时间（支持配置中心配置）
     */
    default int getUpdateSeconds() {
        try {
            Integer updateSeconds = ConfigUtil.getInteger(this.getConfKey() + "-update");
            return updateSeconds == null ? this.getUpdate() : updateSeconds;
        } catch (Exception e) {
            log.error("error in get update from config: {}", getConfKey(), e);
            return this.getUpdate();
        }
    }

    /**
     * 随机时间（支持配置中心动态配置）
     */
    default int getRandomSeconds() {
        try {
            Integer randomSeconds = ConfigUtil.getInteger(this.getConfKey() + "-random");
            return randomSeconds == null ? this.getRandom() : randomSeconds;
        } catch (Exception e) {
            log.error("error in get random from config: {}", this.getConfKey(), e);
            return this.getRandom();
        }
    }

    /**
     * null缓存时间（支持配置中心动态配置）
     */
    default int getNullExpireSeconds() {
        try {
            Integer nullExpireSeconds = ConfigUtil.getInteger(this.getConfKey() + "-null-expire");
            return nullExpireSeconds == null ? this.getNullExpire() : nullExpireSeconds;
        } catch (Exception e) {
            log.error("error in get null expire from config: {}", this.getConfKey(), e);
            return this.getNullExpire();
        }
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
