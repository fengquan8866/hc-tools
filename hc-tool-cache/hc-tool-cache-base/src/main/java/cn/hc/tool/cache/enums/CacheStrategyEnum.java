package cn.hc.tool.cache.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 缓存策略枚举
 *
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/15 20:51
 */
@Getter
public enum CacheStrategyEnum {
    /**
     * 未降级
     */
    ALL_OPEN(1, "未降级"),
    /**
     * 只从reloadTask获取数据
     */
    RELOAD_ONLY(2, "只从reloadTask获取数据"),
    /**
     * 只从缓存获取数据
     */
    CACHE_ONLY(3, "只从缓存获取数据"),
    /**
     * 逻辑过期：只从缓存获取数据，异步写数据
     */
    CACHE_ONLY_AND_ASYNC_WRITE(4, "逻辑过期：只从缓存获取数据，异步写数据"),
    /**
     * 直接返回
     */
    REJECT(5, "直接返回");

    /**
     * 构造器
     */
    CacheStrategyEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    /**
     * 配置值
     */
    private final int value;

    /**
     *
     */
    private final String name;

    /**
     * 获取策略
     * 支持 默认-ALL_OPEN
     */
    public static CacheStrategyEnum getStrategy(Integer val) {
        for (CacheStrategyEnum degrade : CacheStrategyEnum.values()) {
            if (Objects.equals(degrade.value, val)) {
                return degrade;
            }
        }
        return ALL_OPEN;
    }
}
