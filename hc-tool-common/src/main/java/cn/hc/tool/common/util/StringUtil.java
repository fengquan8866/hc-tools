package cn.hc.tool.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * <p>
 * StringUtil
 * </p>
 *
 * @author cdhuangchao3
 * @version 1.0
 * @date 2021/11/26 17:27
 * @since 2021/11/26 17:27
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StringUtil {

    public static final String EMPTY = "";

    /**
     * 判断空
     */
    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    /**
     * 判断空
     */
    public static boolean isNotEmpty(CharSequence cs) {
        return !isEmpty(cs);
    }

    /**
     * 判断空
     */
    public static boolean isBlank(String value) {
        return isEmpty(value) || value.trim().isEmpty();
    }

    /**
     * 判断一个对象是否意味着true/false/null。
     *
     * <p>尤其是针对字符串形式的：不为0的数字、true、yes、y都表示真；0、false、no、n都表示假；空、undefined或null一般表示什么都不是。<br/>
     * 这种判断一般出现在某些配置上，所以用了means这个名称。</p>
     *
     * @param obj          需要判断的对象
     * @param nullVal      当obj为null、或表示空的字符串（''、null、undefined）时的返回值，默认为 null
     * @param unmatchedVal 当obj是字符串，但不是真(1/true/yes/y等)、但也不是假(0/false/no/n)的时候，要返回的值，默认为 Boolean.TRUE
     * @return 一般为 Boolean.TRUE/Boolean.FALSE/null，除非参数 nullVal、unmatchedVal 要求返回特定类型的值
     */
    public static boolean means(Object obj, boolean nullVal, boolean unmatchedVal) {
        if (obj == null) {
            return nullVal;
        }
        if (obj instanceof String) {
            String str = (String) obj;
            if (str.isEmpty() || "null".equalsIgnoreCase(str) || "undefined".equalsIgnoreCase(str)) {
                return nullVal;
            } else if ("1".equals(str) || "true".equalsIgnoreCase(str) || "yes".equalsIgnoreCase(str) || "y".equalsIgnoreCase(str)) {
                return Boolean.TRUE;
            } else if ("0".equals(str) || "false".equalsIgnoreCase(str) || "no".equalsIgnoreCase(str) || "n".equalsIgnoreCase(str)) {
                return Boolean.FALSE;
            }
            return unmatchedVal;
        } else {
            return Boolean.TRUE;
        }
    }

    public static boolean means(Object obj, boolean nullVal) {
        return means(obj, nullVal, true);
    }

    public static boolean means(Object obj) {
        return means(obj, false);
    }

    /**
     * source中包含任一候选
     *
     * @param source     源
     * @param candidates 候选
     */
    public static boolean containsAny(String source, String... candidates) {
        if (isBlank(source) || candidates == null || candidates.length == 0) {
            return false;
        }
        for (String c : candidates) {
            if (source.contains(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 连接
     *
     * @param init 初始化
     * @param sp   分隔符
     * @param arr  连接的内容
     */
    public static String join(String init, String sp, Object... arr) {
        StringBuilder sb = new StringBuilder(init);
        for (Object t : arr) {
            sb.append(sp).append(t);
        }
        return sb.toString();
    }
}
