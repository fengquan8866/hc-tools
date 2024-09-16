package cn.hc.tool.trace.aspect;

import cn.hc.tool.trace.common.HcTraceConst;
import cn.hc.tool.trace.util.TraceFactory;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/16 20:38
 */
@Aspect
@Slf4j
@Component
public class HcTraceAspect {

    public Object xmlPoint(ProceedingJoinPoint jp) throws Throwable {
        return exec(jp);
    }

    /**
     * 类注解切面
     *
     * @param jp 切点
     */
    @Around("@within(cn.hc.tool.trace.aspect.HcTrace)")
    public Object epxTraceWithinPoint(ProceedingJoinPoint jp) throws Throwable {
        return exec(jp);
    }

    /**
     * 注解监控拦截处理方法
     */
    @Around("@annotation(cn.hc.tool.trace.aspect.HcTrace)")
    public Object epxTraceAnnoPoint(ProceedingJoinPoint jp) throws Throwable {
        return exec(jp);
    }

    private Object exec(ProceedingJoinPoint jp) throws Throwable {
        String traceId = MDC.get(HcTraceConst.TRACE_ID);
        boolean noTrace = traceId == null;
        try {
            if (traceId == null) {
                traceId = TraceFactory.createTraceId();
                if (log.isDebugEnabled()) {
                    log.debug("HcTraceAspect生成traceId：{}", traceId);
                }
                MDC.put(HcTraceConst.TRACE_ID, traceId);
            }
            //运行目标方法
            return jp.proceed();
        } finally {
            // 当前生成的，就当前删除
            if (noTrace) {
                MDC.remove(HcTraceConst.TRACE_ID);
            }
        }
    }

}
