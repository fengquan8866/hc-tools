package cn.hc.tool.cache.bean;

import cn.hc.tool.cache.constant.Times;
import lombok.Getter;
import lombok.ToString;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/16 18:29
 */
@Getter
@ToString
public enum CacheKey4 implements CacheConf {
    SKU_INFO("sku_info", "sku_info_{0}", Times.FIVE_MINUTE, Times.MINUTE),
    SKU_INFO2("sku_info2", "sku_info2_{0}", Times.FIVE_MINUTE, Times.MINUTE, Times.MINUTE),
    ;

    CacheKey4(String confKey, String keyExp, int expire) {
        this(confKey, keyExp, expire, expire, 0);
    }

    CacheKey4(String confKey, String keyExp, int expire, int update) {
        this(confKey, keyExp, expire, update, 0);
    }

    CacheKey4(String confKey, String keyExp, int expire, int update, int random) {
        this.confKey = confKey;
        this.keyExp = keyExp;
        this.expire = expire;
        this.update = update;
        this.random = random;
        this.init();
    }

    /**
     * 加载数据配置名称，也是动态配置中缓存开关
     */
    private final String confKey;
    /**
     * 缓存key表达式，支持类型{0}占位符
     */
    private final String keyExp;
    /**
     * 缓存数据过期时间（秒）
     */
    private final int expire;
    /**
     * 缓存数据后台自动刷新时间间隔，小于等于0表示不自动刷新（秒）
     */
    private final int update;

    /**
     * 随机时间范围
     */
    private final int random;

}
