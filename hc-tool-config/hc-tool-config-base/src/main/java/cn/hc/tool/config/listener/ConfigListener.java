package cn.hc.tool.config.listener;

/**
 * 配置监听器
 *
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/15 19:51
 */
public interface ConfigListener {
    /**
     * 监听更新
     *
     * @param key    监控key
     * @param oldVal 老值（可能为null）
     * @param newVal 新值
     * @throws Exception 异常
     */
    void onUpdate(String key, Object oldVal, Object newVal) throws Exception;
}
