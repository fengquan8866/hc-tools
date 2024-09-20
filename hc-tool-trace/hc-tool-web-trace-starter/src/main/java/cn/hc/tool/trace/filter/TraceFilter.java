package cn.hc.tool.trace.filter;

import cn.hc.tool.trace.util.TraceFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/20 10:52
 */
public class TraceFilter extends OncePerRequestFilter  {

    public static final String TRACE_ID = "X-http-trace-id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 查询请求头，获取traceId
        String traceId = request.getHeader(TRACE_ID);
        if (traceId == null) {
            TraceFactory.initTrace();
        } else {
            TraceFactory.setTraceId(traceId);
        }
        // 返回带上traceId
        response.addHeader(TRACE_ID, TraceFactory.traceId());
        // 调用下一个过滤器
        filterChain.doFilter(request, response);
        // 清理
        TraceFactory.clearTraceId();
    }
}
