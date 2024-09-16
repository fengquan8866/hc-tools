package cn.hc.tool.common.exception;

import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/16 20:00
 */
@NoArgsConstructor
public class HcToolException extends RuntimeException implements Serializable {
    private static final long serialVersionUID = -4703633192360797667L;

    public HcToolException(String message) {
        super(message);
    }
}
