package cn.hc.tool.common.util;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;

/**
 * number工具类
 *
 * @author : cdhuangchao3
 * @Date : 2019/12/30 17:55
 * @Description :
 */
@Slf4j
public class NumberUtil {
    private NumberUtil() {
    }

    public static Long toLong(Integer num) {
        return num == null ? null : Long.valueOf(num);
    }

    public static Long toLong(String num) {
        return (num == null || num.isEmpty() || num.trim().isEmpty()) ? null : Long.valueOf(num);
    }

    public static Long toLong(Object num) {
        if (num == null) {
            return null;
        }
        if (num instanceof Long) {
            return (Long) num;
        }
        if (num instanceof Integer) {
            return (long) ((int) (Integer) num);
        }
        if (num instanceof String) {
            return toLong((String) num);
        }
        return null;
    }

    public static Integer toInteger(Long num) {
        return num == null ? null : num.intValue();
    }

    public static Integer toInteger(String num) {
        return (num == null || num.isEmpty() || num.trim().isEmpty()) ? null : Integer.valueOf(num);
    }

    public static Integer toInteger(Number num) {
        if (num == null) {
            return null;
        }
        if (num instanceof Integer) {
            return (Integer) num;
        }
        return num.intValue();
    }

    public static Integer toInteger(Object num) {
        if (num == null) {
            return null;
        }
        if (num instanceof Long) {
            return ((Long) num).intValue();
        }
        if (num instanceof Integer) {
            return (Integer) num;
        }
        if (num instanceof String) {
            return toInteger((String) num);
        }
        return null;
    }

    public static Integer toInteger(String num, int defaultValue) {
        Integer result = (num == null || num.isEmpty() || num.trim().isEmpty()) ? null : Integer.valueOf(num);
        if (result == null) {
            return defaultValue;
        }
        return result;
    }

    /**
     * 静默转换，不会抛出异常
     * {@link NumberSilentUtil#toInteger(String)}
     */
    @Deprecated
    public static Integer toIntegerSilent(String num) {
        try {
            return toInteger(num);
        } catch (NumberFormatException e) {
            log.error("error in parse: {}", num);
            return null;
        }
    }

    public static int toInt(Long num) {
        return num == null ? 0 : num.intValue();
    }

    public static int toInt(String num) {
        Integer n = toInteger(num);
        return n == null ? 0 : n;
    }

    public static BigDecimal toBigDecimal(String num) {
        return (num == null || num.isEmpty() || num.trim().isEmpty()) ? null : new BigDecimal(num);
    }

    public static BigDecimal toBigDecimal(Integer num) {
        return num == null ? null : new BigDecimal(num);
    }

    public static String toStr(Number num) {
        return num == null ? null : num.toString();
    }

    public static boolean equals(Number num1, Number num2) {
        return (num1 == null && num2 == null) || (num1 != null && num1.equals(num2));
    }

    /**
     * 是否为正数
     */
    public static boolean isPositiveNum(Number num) {
        return num != null && num.doubleValue() > 0;
    }

    public static boolean isPositiveNum(Integer num) {
        return num != null && num > 0;
    }

    public static boolean isPositiveNum(Long num) {
        return num != null && num > 0;
    }

    /**
     * 整数转化为二进制翻转后获取index位的值
     *
     * @param num
     * @param index
     * @return
     */
    public static Integer getIntToBinaryIndex(Integer num, int index) {
        if (num == null) {
            return null;
        }
        String skuMarkBinaryStr = Integer.toBinaryString(num);
        if (skuMarkBinaryStr != null && skuMarkBinaryStr.length() >= index) {
            StringBuilder stringBuilder = new StringBuilder(skuMarkBinaryStr);
            String skuMarkBinaryReverseStr = stringBuilder.reverse().toString();
            Character value = skuMarkBinaryReverseStr.charAt(index);
            return Integer.valueOf(value.toString());
        }
        return null;
    }

    public static void main(String[] args) {
        Integer aaa = getIntToBinaryIndex(32, 5);
        System.out.println(aaa);
        Object bbb = null;
        System.out.println(toLong(bbb));
        System.out.println(Arrays.toString(toIntArr("6.0.8", "\\.")));
    }

    public static Integer[] toIntegerArr(String jdVer, String exp) {
        if (StringUtil.isBlank(jdVer)) {
            return new Integer[0];
        }
        String[] vers = jdVer.split(exp);
        Integer[] res = new Integer[vers.length];
        for (int i = 0; i < vers.length; i++) {
            res[i] = NumberUtil.toInt(vers[i]);
        }
        return res;
    }

    public static int[] toIntArr(String jdVer, String exp) {
        if (StringUtil.isBlank(jdVer)) {
            return new int[0];
        }
        String[] vers = jdVer.split(exp);
        int[] res = new int[vers.length];
        for (int i = 0; i < vers.length; i++) {
            res[i] = NumberUtil.toInt(vers[i]);
        }
        return res;
    }

    /**
     * 天花板数（int）
     *
     * @param divided 除数
     * @param by      被除数
     */
    public static int cellInt(Long divided, Long by) {
        return (int) Math.ceil(divided * 1.0 / by);
    }

    /**
     * {@link  #cellInt(Long, Long)}
     */
    public static int cellInt(Long divided, Integer by) {
        return (int) Math.ceil(divided * 1.0 / by);
    }

    /**
     * {@link  #cellInt(Long, Long)}
     */
    public static int cellInt(Integer divided, Integer by) {
        return (int) Math.ceil(divided * 1.0 / by);
    }

    /**
     * 获取集合最小值
     *
     * @param col 集合
     * @param <T> 实现Comparable接口
     */
    public static <T extends Comparable<T>> T min(Collection<T> col) {
        T t = null;
        for (T c : col) {
            if (t == null || t.compareTo(c) > 0) {
                t = c;
            }
        }
        return t;
    }

    /**
     * 获取集合最小值
     *
     * @param col 集合
     * @param <T> 实现Comparable接口
     */
    public static <T extends Comparable<T>> T max(Collection<T> col) {
        T t = null;
        for (T c : col) {
            if (t == null || t.compareTo(c) < 0) {
                t = c;
            }
        }
        return t;
    }

    /**
     * 默认值
     *
     * @param num        原始数字
     * @param defaultNum 默认数字
     * @param <T>        数字
     * @return 非空的数字
     */
    public static <T extends Number> T defaultIfNull(T num, T defaultNum) {
        return num == null ? defaultNum : num;
    }

    /**
     * 判断是否为数字
     *
     * @param str 字符串
     */
    public static boolean isNum(String str) {
        if (str == null) return false;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) < '0' || str.charAt(i) > '9') return false;
        }
        return true;
    }
}
