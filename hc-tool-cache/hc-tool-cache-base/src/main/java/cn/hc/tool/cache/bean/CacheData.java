package cn.hc.tool.cache.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/15 21:06
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CacheData<V> {
    private long lastUpdateTime;
    private V data;
}
