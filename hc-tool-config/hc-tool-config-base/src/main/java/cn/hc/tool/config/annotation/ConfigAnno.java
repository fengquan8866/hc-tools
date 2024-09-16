package cn.hc.tool.config.annotation;

import java.lang.annotation.*;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/15 19:55
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigAnno {
    /**
     * backup类，配置中心调不通时的默认数据
     */
    String[] backup() default {};
}
