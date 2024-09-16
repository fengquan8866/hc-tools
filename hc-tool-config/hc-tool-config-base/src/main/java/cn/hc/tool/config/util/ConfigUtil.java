package cn.hc.tool.config.util;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/15 19:49
 */
@Slf4j
public class ConfigUtil {

    private ConfigUtil() {}

    private static AbsConfiguration absConfiguration;

    /**
     * 获取字符串配置
     */
    public static String get(String key) {
        try {
            return absConfiguration == null ? null : absConfiguration.getStr(key);
        } catch (Exception e) {
            log.error("ConfigUtil.get出错 :{}", key, e);
        }
        return null;
    }

    /**
     * 获取bool配置，可能为null
     */
    public static Boolean getBool(String key) {
        try {
            return absConfiguration == null ? null : absConfiguration.getBool(key);
        } catch (Exception e) {
            log.error("ConfigUtil.getBool出错:{}", key, e);
        }
        return false;
    }

    /**
     * 获取bool配置，可能为null
     */
    public static Integer getInteger(String key) {
        try {
            return absConfiguration == null ? null : absConfiguration.getInteger(key);
        } catch (Exception e) {
            log.error("ConfigUtil.getInteger:{}", key, e);
        }
        return null;
    }

    /**
     * 获取bool配置
     */
    public static boolean bool(String key) {
        try {
            Boolean b = absConfiguration == null ? null : absConfiguration.getBool(key);
            return Boolean.TRUE.equals(b);
        } catch (Exception e) {
            log.error("ConfigUtil.bool出错: {}", key, e);
        }
        return false;
    }

    /**
     * 获取bool配置
     * @param defVal 默认值
     */
    public static boolean bool(String key, boolean defVal) {
        try {
            Boolean b = absConfiguration == null ? null : absConfiguration.getBool(key);
            return b == null ? defVal : b;
        } catch (Exception e) {
            log.error("ConfigUtil.bool出错: {}, defVal: {}", key, defVal, e);
        }
        return false;
    }

    /**
     * 获取ip维度配置
     */
    public static String getIpConf(String key) {
        return absConfiguration == null ? null : absConfiguration.getIpConf(key);
    }

    /**
     * 解析ip维度配置
     */
    public static String resolveIpConf(String value) {
        return absConfiguration == null ? null : absConfiguration.resolveIpConf(value);
    }

    public static void setAbsConfiguration(AbsConfiguration absConfiguration) {
        ConfigUtil.absConfiguration = absConfiguration;
    }

}
