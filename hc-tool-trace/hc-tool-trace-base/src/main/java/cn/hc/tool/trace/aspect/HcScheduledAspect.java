package cn.hc.tool.trace.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/22 21:14
 */
@Aspect
@Slf4j
@Component
@ConditionalOnClass(name = "org.springframework.scheduling.annotation.Scheduled")
public class HcScheduledAspect {

    @Autowired
    private HcTraceAspect hcTraceAspect;

    /**
     * 注解切面
     *
     * @param jp 切点
     */
    @Around("@annotation(org.springframework.scheduling.annotation.Scheduled)")
    public Object traceAroundPoint(ProceedingJoinPoint jp) throws Throwable {
        return hcTraceAspect.exec(jp);
    }
}
