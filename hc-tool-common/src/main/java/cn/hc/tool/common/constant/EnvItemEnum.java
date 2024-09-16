package cn.hc.tool.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/15 19:58
 */
@Getter
@AllArgsConstructor
public enum EnvItemEnum {
    /**
     * 线上环境
     */
    PRODUCTION("production"),
    ONLINE("online"),
    PROD("prod"),
    /**
     * 预发环境
     */
    PRE("pre"),
    PREVIEW("preview"),
    YF("yf"),
    YFB("yfb"),
    PREW("prew"),
    /**
     * 测试环境
     */
    TEST("test"),
    /**
     * 开发环境
     */
    DEV("dev"),
    DEVELOPMENT("development"),
    LOCALE("locale");

    /**
     * 环境名称
     */
    private final String name;
}
