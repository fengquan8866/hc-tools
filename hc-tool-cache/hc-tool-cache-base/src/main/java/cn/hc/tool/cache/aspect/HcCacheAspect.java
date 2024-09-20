package cn.hc.tool.cache.aspect;

import cn.hc.tool.cache.bean.CacheConf;
import cn.hc.tool.cache.exception.ToolCacheException;
import cn.hc.tool.cache.util.ToolCacheUtil;
import cn.hc.tool.common.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/20 15:01
 */
@Aspect
@Slf4j
@Component
public class HcCacheAspect {

    private final SpelExpressionParser spelExpressionParser = new SpelExpressionParser();

    private final DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    private static final Set<String> SET = new HashSet<String>() {
        private static final long serialVersionUID = 2915055647346469550L;

        {
            add(Byte.class.getName());
            add(Short.class.getName());
            add(Integer.class.getName());
            add(Long.class.getName());
            add(Float.class.getName());
            add(Double.class.getName());
            add(Character.class.getName());
            add(Boolean.class.getName());
            add(String.class.getName());
        }
    };

    @Autowired(required = false)
    private ToolCacheUtil toolCacheUtil;

    public Object xmlPoint(ProceedingJoinPoint jp) throws Throwable {
        return exec(jp);
    }

    /**
     * 类注解切面
     *
     * @param jp 切点
     */
    @Around("@within(cn.hc.tool.cache.aspect.HcCache)")
    public Object cacheWithinPoint(ProceedingJoinPoint jp) throws Throwable {
        return exec(jp);
    }

    /**
     * 注解监控拦截处理方法
     */
    @Around("@annotation(cn.hc.tool.cache.aspect.HcCache)")
    public Object cacheAnnoPoint(ProceedingJoinPoint jp) throws Throwable {
        return exec(jp);
    }

    private Object exec(ProceedingJoinPoint jp) throws Throwable {
        Method method = getMethod(jp);
        if (method == null) return jp.proceed();
        HcCache hcCache = method.getAnnotation(HcCache.class);
        if (hcCache == null) return jp.proceed();
        CacheConf cacheConf = CacheConf.confMap.get(hcCache.conf());
        if (cacheConf == null) return jp.proceed();
        Parameter[] parameters = method.getParameters();
        // 获取方法入参数据
        Object[] args = jp.getArgs();
        String key = null;
        if (parameters.length > 0) {
            // 如果key为空，有一个级别类型，默认key=该基本类型
            if (StringUtil.isEmpty(hcCache.key()) && parameters.length == 1
                    && (parameters[0].getType().isPrimitive() || SET.contains(parameters[0].getType().getName()))) {
                key = Objects.toString(args[0]);
            } else if (!StringUtil.isEmpty(hcCache.key())) {
                key = evaluateExpression(hcCache.key(), jp);
            }
        }

        Callable<Object> callable = () -> {
            try {
                return jp.proceed();
            } catch (Throwable e) {
                throw new ToolCacheException(e);
            }
        };
        if (key == null) {
            return toolCacheUtil.get(cacheConf, method.getGenericReturnType(), callable);
        }
        return toolCacheUtil.get(cacheConf, method.getGenericReturnType(), callable, key);
    }

    /**
     * 解析el表达式
     *
     * @param expression
     * @param point
     * @return
     */
    private String evaluateExpression(String expression, ProceedingJoinPoint point) {
        // 获取目标对象
        Object target = point.getTarget();
        // 获取方法参数
        Object[] args = point.getArgs();
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Method method = methodSignature.getMethod();

        EvaluationContext context = new MethodBasedEvaluationContext(target, method, args, parameterNameDiscoverer);
        Expression exp = spelExpressionParser.parseExpression(expression);
        return exp.getValue(context, String.class);
    }

    private Method getMethod(JoinPoint jp) {
        Signature signature = jp.getSignature();
        if (signature instanceof MethodSignature) {
            MethodSignature msig = (MethodSignature) signature;
            return msig.getMethod();
        }
        return null;
    }

}
