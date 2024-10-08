package cn.hc.tool.common.exception;

import lombok.NoArgsConstructor;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/16 20:00
 */
@NoArgsConstructor
public class HcToolException extends RuntimeException {
    private static final long serialVersionUID = -4703633192360797667L;

    public HcToolException(String message) {
        super(message);
    }

    public HcToolException(Throwable cause) {
        super(cause);
    }

}
