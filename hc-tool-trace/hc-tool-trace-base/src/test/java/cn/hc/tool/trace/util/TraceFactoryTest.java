package cn.hc.tool.trace.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/16 20:21
 */
@Slf4j
public class TraceFactoryTest {

    @Test
    public void createTraceId() {
        log.info("-----{}", TraceFactory.createTraceId());
    }

    @Test
    public void appendTraceId() {
        log.info("1----{}", TraceFactory.traceId());
        TraceFactory.appendTrace(() -> log.info("trace: {}", TraceFactory.traceId()), "aa");
        TraceFactory.appendTrace(() -> log.info("trace: {}", TraceFactory.traceId()), "bb");
        log.info("2----{}", TraceFactory.traceId());
    }

    @Test
    public void incrIfExistPart() {
        String traceId = "aaaa-1";
        TraceFactory.setTraceId(traceId);
        log.info("1-----{}", TraceFactory.traceId());
        TraceFactory.setTraceId(TraceFactory.incrIfExistPart(traceId));
        log.info("2-----{}", TraceFactory.traceId());
    }
}
