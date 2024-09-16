package cn.hc.tool.trace.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/16 20:05
 */
@Component
public class HcTraceConst {
    /**
     * 内部流转trace_id
     */
    public static String TRACE_ID = "traceId";

    @Value("${hc.tool.trace.key:traceId}")
    public void setTraceKey(String traceKey) {
        HcTraceConst.TRACE_ID = traceKey;
    }
}
