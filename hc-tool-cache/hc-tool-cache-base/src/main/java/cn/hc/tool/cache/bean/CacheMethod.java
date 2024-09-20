package cn.hc.tool.cache.bean;

import lombok.NoArgsConstructor;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/20 18:34
 */
@NoArgsConstructor
public enum CacheMethod {
    get(),
    getFromList(),
    getFromSet(),
    getMapFromList(),
    getMapFromSet(),
    ;
}
