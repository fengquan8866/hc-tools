package cn.hc.tool.trace.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 链路跟踪切面
 *
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/16 20:37
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface HcTrace {
    /**
     * traceKey
     */
    String value() default "";
}
