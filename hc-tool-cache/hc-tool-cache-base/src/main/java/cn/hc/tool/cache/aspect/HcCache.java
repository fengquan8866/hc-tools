package cn.hc.tool.cache.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解缓存
 *
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/20 14:40
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface HcCache {
    /**
     * 缓存配置，对应 {@link cn.hc.tool.cache.bean.CacheConf#getConfKey()} 的值
     */
    String conf();

//    Enum cf();

//    /**
//     * 缓存方法
//     */
//    CacheMethod method() default CacheMethod.get;

    /**
     * 缓存key参数，springEL表达式
     */
    String key() default "";
}
