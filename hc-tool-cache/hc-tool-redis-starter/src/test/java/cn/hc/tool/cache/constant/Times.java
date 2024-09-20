package cn.hc.tool.cache.constant;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/16 18:31
 */
public class Times {
    private Times() {
    }

    /**
     * 1天的毫秒
     */
    public static final int DAY_OF_MILLIS = 24 * 60 * 60 * 1000;
    /**
     * 7天的秒
     */
    public static final int WEEK = 7 * 24 * 60 * 60;
    /**
     * 1天的秒
     */
    public static final int DAY = 24 * 60 * 60;
    /**
     * 30分的毫秒
     */
    public static final int HALF_HOUR_OF_MILLIS = 30 * 60 * 1000;
    /**
     * 30分的秒
     */
    public static final int HALF_HOUR = 30 * 60;
    /**
     * 1分的秒
     */
    public static final int MINUTE = 60;
    /**
     * 5分的秒
     */
    public static final int FIVE_MINUTE = 5 * MINUTE;
}
