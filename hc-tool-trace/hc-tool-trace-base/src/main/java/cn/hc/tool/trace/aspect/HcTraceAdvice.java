package cn.hc.tool.trace.aspect;

import cn.hc.tool.trace.common.HcTraceConst;
import cn.hc.tool.trace.util.TraceFactory;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.MDC;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/22 20:25
 */
@Slf4j
public class HcTraceAdvice implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        String traceId = MDC.get(HcTraceConst.TRACE_ID);
        // 有traceId，直接执行
        if (traceId != null) return invocation.proceed();
        try {
            traceId = TraceFactory.createTraceId();
            if (log.isDebugEnabled()) {
                log.debug("HcTraceAdvice生成traceId：{}", traceId);
            }
            MDC.put(HcTraceConst.TRACE_ID, traceId);
            // 运行目标方法
            return invocation.proceed();
        } finally {
            // 当前生成的，就当前删除
            MDC.remove(HcTraceConst.TRACE_ID);
        }
    }
}
