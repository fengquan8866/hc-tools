package cn.hc.tool.trace.util;

import cn.hc.tool.common.util.NumberUtil;
import cn.hc.tool.common.util.SilentUtil;
import cn.hc.tool.common.util.StringUtil;
import cn.hc.tool.trace.common.HcTraceConst;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * 链路跟踪工具类
 *
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/16 20:10
 */
@Slf4j
public class TraceFactory {

    /**
     * 为exec生成链路id
     */
    public static void trace(Runnable exec) {
        trace(exec, HcTraceConst.TRACE_ID);
    }

    /**
     * 生成链路id，当前生成的，会在当前结束销毁
     *
     * @param exec      任务
     * @param traceKeys 链路key集合
     */
    public static void trace(Runnable exec, String... traceKeys) {
        Set<String> initTraceKeys = initTrace(traceKeys);
        try {
            exec.run();
        } catch (Exception e) {
            throw SilentUtil.throwRuntimeException(e);
        } finally {
            clearTraceId(initTraceKeys);
        }
    }

    /**
     * 为exec生成链路id
     */
    public static <T> T trace(Callable<T> exec) {
        return trace(exec, HcTraceConst.TRACE_ID);
    }

    /**
     * 生成链路id，当前生成的，会在当前结束销毁
     *
     * @param exec      任务
     * @param traceKeys 链路key集合
     */
    public static <T> T trace(Callable<T> exec, String... traceKeys) {
        Set<String> initTraceKeys = initTrace(traceKeys);
        try {
            return exec.call();
        } catch (Exception e) {
            throw SilentUtil.throwRuntimeException(e);
        } finally {
            clearTraceId(initTraceKeys);
        }
    }

    /**
     * 设置traceId
     */
    public static void setTraceId(String traceId) {
        MDC.put(HcTraceConst.TRACE_ID, traceId);
    }

    /**
     * 递增traceId尾部数字，如果traceId尾部数字为空，则不处理
     * 示例：入参 traceId = aaaaaa-1，则返回aaaaaa-2
     */
    public static String incrIfExistPart(String traceId) {
        int idx = traceId.lastIndexOf('-');
        if (idx >= traceId.length() - 1) return traceId;
        String part = traceId.substring(idx + 1);
        if (!NumberUtil.isNum(part)) return traceId;
        return traceId.substring(0, idx + 1) + (NumberUtil.toInt(part) + 1);
    }

    /**
     * trace保持，执行exec之后，恢复链路id
     *
     * @param exec      执行体
     * @param traceKeys 链路跟踪key列表
     */
    public static void keep(Runnable exec, String... traceKeys) {
        if (traceKeys == null || traceKeys.length == 0) {
            return;
        }
        Map<String, String> traceMap = new HashMap<String, String>();
        for (String k : traceKeys) {
            String s = MDC.get(k);
            traceMap.put(k, s == null ? initTraceId(k) : s);
        }

        try {
            exec.run();
        } catch (Exception e) {
            throw SilentUtil.throwRuntimeException(e);
        } finally {
            for (Map.Entry<String, String> e : traceMap.entrySet()) {
                MDC.put(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * 当前的traceId
     */
    public static String traceId() {
        return MDC.get(HcTraceConst.TRACE_ID);
    }

    /**
     * 初始化traceId
     */
    public static String initTrace() {
        return initTraceId(HcTraceConst.TRACE_ID);
    }

    /**
     * 初始化traceId
     */
    public static Set<String> initTrace(String... keys) {
        Set<String> set = new HashSet<String>();
        for (String key : keys) {
            String traceId = MDC.get(key);
            if (traceId != null) {
                continue;
            }

            traceId = createTraceId();
            if (log.isDebugEnabled()) {
                log.debug("TraceFactory生成traceId：{}", traceId);
            }
            MDC.put(key, traceId);
            set.add(key);
        }
        return set;
    }

    public static Set<String> initTrace(Set<String> keySet) {
        return initTrace(keySet.toArray(new String[0]));
    }

    /**
     * 清理traceId
     *
     * @param initTraceKeys 当前初始化了traceId的key
     */
    public static void clearTraceId(Set<String> initTraceKeys) {
        if (initTraceKeys == null || initTraceKeys.isEmpty()) {
            return;
        }
        for (String key : initTraceKeys) {
            MDC.remove(key);
        }
    }

    /**
     * 生成链路id
     */
    public static String createTraceId() {
        String uuid = UUID.randomUUID().toString();
        return uuid.replace("-", "");
    }

    /**
     * 追加链路跟踪id
     * @param func 方法体
     * @param traceKey 链路跟踪key
     * @param traceVal 值
     */
    public static void appendTraceId(final Runnable func, final String traceKey, final Object... traceVal) {
        keep(() -> {
            MDC.put(traceKey, StringUtil.join(MDC.get(traceKey), "-", traceVal));
            func.run();
        }, traceKey);
    }

    /**
     * {@link #appendTraceId(Runnable, String, Object...)}
     */
    public static void appendTrace(final Runnable func, final Object... traceVal) {
        appendTraceId(func, HcTraceConst.TRACE_ID, traceVal);
    }

    /**
     * 清空traceId
     */
    public static void clearTraceId() {
        MDC.remove(HcTraceConst.TRACE_ID);
    }

    /**
     * 初始化traceId，并返回
     * @param traceKey 链路key
     */
    private static String initTraceId(String traceKey) {
        String traceId = createTraceId();
        MDC.put(traceKey, traceId);
        return traceId;
    }
}
