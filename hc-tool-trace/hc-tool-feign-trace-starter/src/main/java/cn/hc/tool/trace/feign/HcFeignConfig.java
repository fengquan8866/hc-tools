package cn.hc.tool.trace.feign;

import cn.hc.tool.trace.filter.TraceFilter;
import cn.hc.tool.trace.util.TraceFactory;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/20 22:13
 */
@Configuration
@EnableFeignClients(basePackages = {""})
@Slf4j
public class HcFeignConfig {
    @Bean
    public RequestInterceptor hcFeignRequestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 在这里添加请求头等
                log.info("HcFeignConfig-------------apply, url: {}, body:{}", template.url(), template.bodyTemplate());

                // 添加一个随机的请求ID，用于跟踪请求
                template.header(TraceFilter.TRACE_ID, TraceFactory.incrIfExistPart(TraceFactory.traceId()));
            }
        };
    }
}
