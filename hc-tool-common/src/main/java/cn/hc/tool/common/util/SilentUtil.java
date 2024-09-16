package cn.hc.tool.common.util;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/16 19:56
 */
public class SilentUtil {

    /**
     * 转换RuntimeException
     *
     * @param exception 待转换异常
     */
    public static RuntimeException throwRuntimeException(Exception exception) {
        if (exception instanceof RuntimeException) {
            return (RuntimeException) exception;
        } else {
            return new RuntimeException(exception);
        }
    }

}
