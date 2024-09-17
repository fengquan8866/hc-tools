总览见 [hc-tools](../README.md)

[TOC]

# epx-tool-common

## 一、步骤
### 1.1 引入依赖
```xml
<dependency>
  <groupId>cn.hc</groupId>
  <artifactId>hc-tool-common</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```


## 二、功能

注：可提需求、可提需求、可提需求

### 1、ClassUtil
> 详见 cn.hc.tool.common.util.ClassUtil

### 2、SystemClock 毫秒工具
> 高并发场景下System.currentTimeMillis()的性能问题的优化

* 2.1、当前时间毫秒
```java
cn.hc.tool.common.util.SystemClock.currentTimeMillis()
```

* 2.2 更新其他时间变量
```java
Class T {
    protected static AtomicLong now;
}
cn.hc.tool.common.util.SystemClock.addField(T.now);
```