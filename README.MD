# hc-tools工具包

## pom依赖

* xml

```xml
<!-- hc tool -->
<dependency>
    <groupId>cn.hc</groupId>
    <artifactId>hc-tool-dependencies</artifactId>
    <version>${hc.tools.version}</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
```

* 版本号：`1.0-SNAPSHOT`

## 子模块介绍

1. [common组件README](./hc-tool-common/README.MD)： basic工具包
2. [trace组件README](./hc-tool-trace/README.MD)： 链路跟踪工具包
3. [config组件README](./hc-tool-config/README.MD): 配置中心防腐层
4. [cache组件README](./hc-tool-cache/README.MD)： 高级缓存工具（String、List、Set、Map）、分布式缓存防腐层（支持Redis）、~~流程编排缓存~~
