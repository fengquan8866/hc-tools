package cn.hc.tool.common.util;

import lombok.extern.slf4j.Slf4j;

/**
 * 静默转换，不会抛出异常
 * @author cdhuangchao3
 * @version 1.0
 * @date 2021/4/26 14:07
 */
@Slf4j
public class NumberSilentUtil {

    /**
     * 静默转换，不会抛出异常
     */
    public static Long toLong(String num) {
        try {
            return NumberUtil.toLong(num);
        } catch (NumberFormatException e) {
            log.error("error in parse: {}", num);
            return null;
        }
    }

    /**
     * 静默转换，不会抛出异常
     */
    public static Integer toInteger(String num) {
        try {
            return NumberUtil.toInteger(num);
        } catch (NumberFormatException e) {
            log.error("error in parse: {}", num);
            return null;
        }
    }

}
