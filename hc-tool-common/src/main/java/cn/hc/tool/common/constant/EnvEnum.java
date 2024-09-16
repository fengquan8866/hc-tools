package cn.hc.tool.common.constant;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

/**
 * 环境枚举
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/15 19:58
 */
public enum EnvEnum {
    /**
     * 线上环境
     */
    PRODUCTION(EnvItemEnum.PRODUCTION, EnvItemEnum.ONLINE, EnvItemEnum.PROD),
    /**
     * 预发环境
     */
    PRE(EnvItemEnum.PRE, EnvItemEnum.PREVIEW, EnvItemEnum.YF, EnvItemEnum.YFB, EnvItemEnum.PREW),
    /**
     * 测试环境
     */
    TEST(EnvItemEnum.TEST),
    /**
     * 开发环境
     */
    DEV(EnvItemEnum.DEV, EnvItemEnum.DEVELOPMENT, EnvItemEnum.LOCALE);

    /**
     * 环境名称
     */
    @Getter
    private final String name;

    /**
     * 别名集
     */
    private final Set<String> alias;

    /**
     * 构造器
     */
    EnvEnum(EnvItemEnum... alias) {
        this.name = alias[0].getName();
        this.alias = new HashSet<String>();
        for (EnvItemEnum e : alias) {
            this.alias.add(e.getName());
        }
    }

    /**
     * 获取环境名称
     * @param alias 环境别名
     */
    public static String getEnvName(String alias) {
        String[] as = alias.split(",");
        for (EnvEnum e : EnvEnum.values()) {
            for (String a : as) {
                if (e.alias.contains(a)) {
                    return e.name;
                }
            }
        }
        return alias;
    }

}
