总览见 [epx-tools](../README.MD)

[TOC]

# hc-tool-config 配置中心

## 1、hc-tool-config 配置组件
>注：当前版本仅作为防腐层，后续版本可能会支持各配置中心
>> 待完善功能：backup、数据写入、数据错误处理

### 1.1 引入依赖
```xml
<dependency>
  <groupId>cn.hc</groupId>
  <artifactId>hc-tool-config</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

### 1.2 初始化
>> 常量类属性支持字符串、数字、布尔值、自定义类

>> final不支持类型：byte, char, short, int, long, float, double, boolean; 再加上 Literal String 类型(直接双引号字符串)
* 2种初始化方式
```text
1、常量类上添加注解 cn.hc.tool.config.annotation.ConfigAnno
2、调用方法 AbsConfiguration.addConstant() 添加常量类
```

### 1.3 公共开关（组件中已包含该常量类）
```java
package com.jd.epx.tool.config.constant;
public class CommonConfig {
    /**
     * 开关-是否走地址转换，默认开启
     * 开启-走地址转换, 关闭-不走地址转换
     */
    public static boolean ADDRESS_RPC_SWITCH = true;
    /**
     * 开关-入参是否转新地址（出参与之相反），默认开启
     * 开启-转新地址, 关闭-转老地址
     */
    public static boolean ADDRESS_MAPPING_SWITCH = true;
    /**
     * 开关-出参是否转新地址，默认关闭
     * 开启-转新地址, 关闭-转老地址
     */
    public static boolean ADDRESS_OUT_MAPPING_SWITCH = false;
}
```

### 1.4 监听、正则监听（需要才使用）
>>使用场景：三级地址列表，如果实时性有偏差，可添加缓存，开关更新时清空缓存
```java
// jdk7
ConfigFactory.register("ADDRESS_MAPPING_SWITCH", new ConfigListener() {
    @Override
    public void onUpdate(String key, Object oldVal, Object newVal) {
        log.info("=============更新数据，key：{}，old：{}，new：{}", key, oldVal, newVal);
    }
});
// jdk8 lambda表达式
ConfigFactory.register("ADDRESS_RPC_SWITCH", (String key, Object oldVal, Object newVal) -> {
        log.info("=============更新数据，key：{}，old：{}，new：{}", key, oldVal, newVal);
    }
);
// 正则监听
ConfigFactory.register("LOG_LEVEL(\\.\\w+)+$", (String key, Object oldVal, Object newVal) -> {
        log.info("=============更新数据，key：{}，old：{}，new：{}", key, oldVal, newVal);
    }
);
```

### 1.5 ConfigUtil
>>使用场景：通过get方式获取配置，主要用于快速从ucc切换到ducc
* 1.5.1 获取字符串值：ConfigUtil.get(String key)
```java
// 当未进行配置时会返回null，需自行判空
String uccTimeout = ConfigUtil.get("hbase_crumbs_timeout");
```
* 1.5.2 获取布尔值：ConfigUtil.getBool(String key)
```java
// 当未进行配置时会返回null，需自行判空
if (ConfigUtil.getBool("jmq_degrade_all_send")) {
    // ...
}
```
* 1.5.3 获取布尔值：ConfigUtil.bool(String key)
```java
// 当未进行配置时会返回false
if (ConfigUtil.bool("jmq_degrade_all_send")) {
    // ...
}
```
* 1.5.4 获取布尔值：ConfigUtil.bool(String key, boolean defVal)
```java
// 当未进行配置时会返回 ${defVal}
if (ConfigUtil.bool("jmq_degrade_all_send"), true) {
    // ...
}
```

### 1.6 版本号配置
>> 使用场景：使用版本号
* `@IpConf` 字段加上该注解，会更新到对应版本号
```java
public class ConfigConstant {
    /** 商详pc版本号 */
    @IpConf
    public static String ITEM_STATIC_PC_VERSION;
}
```
