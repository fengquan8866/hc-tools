总览见 [hc-tools](../README.md)

[TOC]

# hc-tool-trace

## 一、步骤
### 1.1 引入依赖
```xml
<!-- feign应用 -->
<dependency>
  <groupId>cn.hc</groupId>
  <artifactId>hc-tool-feign-trace-base</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

### 1.2 日志配置
* 日志配置格式加上 `%X{traceId}`，如：
```xml
// log4j2
<Property name="logPatternLayout">%d{yyyy-MM-dd HH:mm:ss.SSS} - %X{traceId} %X{PFTID} [%t] %-5level %logger{1.}:%L -%msg%n</Property>
<PatternLayout pattern="${logPatternLayout}"/>

// logback
<property name="logPatternLayout" value="%d{yyyy-MM-dd HH:mm:ss.SSS} - %X{traceId} %X{PFTID} [%t] %-5level %logger{1.}:%L -%msg%n"/>
<pattern>${logPatternLayout}</pattern>
```

### 1.3 已默认支持
1. http请求
2. 定时任务注解 @Scheduled
3. Rocketmq Listener @RocketMQMessageListener

### 1.4 自定义切面链路跟踪（针对`定时任务`等非`HTTP`入口）
>> 如果有重复配置，最上层配置生效
* 注解方式
```java
// 支持 类、方法 维度
@cn.hc.tool.trace.aspect.HcTrace
```

* xml方式
```xml
<aop:config>
    <aop:pointcut id="tracePointcut" expression="execution(* cn.hc..worker.**.*(..))
         or execution(* cn.hc..listener.**.*(..))"/>
    <aop:aspect ref="hcTraceAspect">
        <aop:around method="xmlPoint" pointcut-ref="tracePointcut"/>
    </aop:aspect>
</aop:config>
```

* 自定义切面表达式
```yaml
hc:
  trace:
    # 按需添加：自定义切面表达式
    pointcut: "@within(org.apache.rocketmq.spring.annotation.RocketMQMessageListener) || @annotation(org.springframework.scheduling.annotation.Scheduled)"
```

## 二、功能

注：可提需求、可提需求、可提需求

### 1、获取traceId
>> `httpClient` 等请求，需自行在`header`中添加`traceId`，获取`traceId`方式如下：
```java
org.slf4j.MDC.get(HcTraceConst.TRACE_ID);
```
