package cn.hc.tool.common.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/16 19:56
 */
@Slf4j
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

    /**
     * 执行有参函数
     */
    public static <R, T extends RuntimeException> R doWithThrow(Callable<R> func, Function<Exception, T> errorFunc) {
        try {
            return func.call();
        } catch (Exception e) {
            log.error("error in func：{}", e.getMessage());
            throw errorFunc.apply(e);
        }
    }

}
