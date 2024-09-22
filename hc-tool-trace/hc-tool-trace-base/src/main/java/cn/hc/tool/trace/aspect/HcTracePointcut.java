package cn.hc.tool.trace.aspect;

import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/22 20:24
 */
@Configuration
public class HcTracePointcut {

    /**
     * 切面设置，直接和 spring 的配置对应 ${}，可以从 properties 或者配置中心读取。更加灵活
     */
    @Value("${hc.trace.pointcut:}")
    private String pointcut;

    @Bean("hcTracePointcutAdvisor")
    @ConditionalOnProperty("hc.trace.pointcut")
    public AspectJExpressionPointcutAdvisor hcTracePointcutAdvisor() {
        AspectJExpressionPointcutAdvisor advisor = new AspectJExpressionPointcutAdvisor();
        advisor.setExpression(pointcut);
        advisor.setAdvice(new HcTraceAdvice());
        return advisor;
    }

}
