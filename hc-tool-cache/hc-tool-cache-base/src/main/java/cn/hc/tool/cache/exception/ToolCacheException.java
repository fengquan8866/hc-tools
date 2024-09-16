package cn.hc.tool.cache.exception;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/16 21:14
 */
public class ToolCacheException extends RuntimeException {
    private static final long serialVersionUID = 6464888434532746184L;

    public ToolCacheException(String message) {
        super(message);
    }

    public ToolCacheException(Throwable cause) {
        super(cause);
    }

    public ToolCacheException(String message, Throwable cause) {
        super(message, cause);
    }
}
