package cn.hc.tool.common.func;

/**
 * 无入参、返回Func
 *
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/22 16:48
 */
@FunctionalInterface
public interface NFunc {
    void invoke() throws Exception;
}
