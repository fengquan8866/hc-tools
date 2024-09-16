总览见 [epx-tools](../README.md)

[TOC]

# epx-tool-cache 缓存组件

## 零、方案设计
* 1、默认策略简版
  ![CacheUtil默认策略简版](./images/CacheUtil默认策略简版.png)
* 2、核心流程
  ![CacheUtil默认策略](./images/CacheUtil默认策略.png)

## 一、步骤
### 1.1 引入依赖
```xml
<dependency>
  <groupId>cn.hc</groupId>
  <artifactId>hc-redis-spring-boot-starter</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```


## 二、配置
### 2.1 缓存策略
```java
public enum CacheStrategyEnum {
    /**
     * 未降级
     */
    ALL_OPEN(1, "未降级"),
    /**
     * 只从reloadTask获取数据
     */
    RELOAD_ONLY(2, "只从reloadTask获取数据"),
    /**
     * 只从缓存获取数据
     */
    CACHE_ONLY(3, "只从缓存获取数据"),
    /**
     * 逻辑过期：只从缓存获取数据，异步写数据
     */
    CACHE_ONLY_AND_ASYNC_WRITE(4, "逻辑过期：只从缓存获取数据，异步写数据"),
    /**
     * 直接返回
     */
    REJECT(5, "直接返回");
}
```

### 2.2 枚举
```java
@Getter
public enum CacheKey implements CacheConf {
  SKU_INFO("sku_info", "sku_info_{0}", TimeConstant.FIVE_MINUTE_OF_SECOND, TimeConstant.ONE_MINUTE_OF_SECOND),
  SKU_INFO2("sku_info2", "sku_info2_{0}", TimeConstant.FIVE_MINUTE_OF_SECOND, TimeConstant.ONE_MINUTE_OF_SECOND, TimeConstant.ONE_MINUTE_OF_SECOND),
  ;
  CacheKey(String confKey, String keyExp, int expire, int update) {
    this(confKey, keyExp, expire, update, 0);
  }
  CacheKey(String confKey, String keyExp, int expire, int update, int randomRange) {
    this.confKey = confKey;
    this.keyExp = keyExp;
    this.expire = expire;
    this.update = update;
    this.randomRange = randomRange;
  }
  /**
   * 加载数据配置名称，也是动态配置中缓存开关
   */
  private final String confKey;
  /**
   * 缓存key表达式，支持类型{0}占位符
   */
  private final String keyExp;
  /**
   * 缓存数据过期时间（秒）
   */
  private final int expire;
  /**
   * 缓存数据后台自动刷新时间间隔，小于等于0表示不自动刷新（秒）
   */
  private final int update;
  /**
   * 随机时间范围
   */
  private final int randomRange;
}
```


## 三、功能-缓存工具类
### 3.1 带缓存任务
* 方法介绍
```java
/**
 * @param cacheKey   缓存key枚举
 * @param reloadTask 缓存未命中加装数据的任务
 * @param keyParams  生成完整缓存key需要的参数
 * @param <V>        返回结果数据类型
 * @return
 */
<V> V getWithCache(CacheConf cacheKey, Callable<V> reloadTask, Object... keyParams) throws Exception;
```
* 使用示例一
```java
Integer category = 2;
Integer val = cacheUtil.getWithCache(CacheKey.SEA_ITEM_CATEGORY, () -> category + 1, category);
```
* 使用示例二
```java
List<VenderShopStoreVo> list = itemCacheUtil.getWithCache(CacheKey.PRODUCT_LS_SHOP,
        () -> venderRpc.getShopStoreBySpu(Arrays.asList(ids)), idvo.getSpuId());
```

### 3.2 自定义key生成器的带缓存任务
* 方法介绍
```java
/**
 * @param cacheKey   缓存key枚举
 * @param getKeyFunc 生成缓存key的方法
 * @param reloadTask 缓存未命中加装数据的任务
 * @param <V>        返回结果数据类型
 * @return
 */
<V, T extends CacheConf> V getWithCache(T cacheKey, Function<T, String> getKeyFunc, Callable<V> reloadTask) throws Exception;
```
* 使用示例一
```java
Integer category = 2;
Integer val = cacheUtil.getWithCache(CacheKey.SEA_ITEM_CATEGORY, c -> c.getSwitchKey(), () -> category + 1);
```
* 使用示例二
```java
StickerVO stickerVO = itemCacheUtil.getWithCache(CacheKey.WARE_STICKER_BY_MARKER, (CacheKey cacheKey) -> CacheKey.getWareStickerByMarkerKey(sticker),
        () -> stickerJsfService.getWareStickerByMarkIdAndValue(WareMarkConstant.STICKER, sticker));
```

### 3.3 结果数据是否缓存，缓存任务（基于3.1，新增判断是否缓存数据结果）
* 方法介绍
```java
/**
 * @param cacheKey   缓存key枚举
 * @param reloadTask 缓存未命中加装数据的任务
 * @param predicate  是否缓存子句
 * @param keyParams  生成完整缓存key需要的参数
 * @param <V>        返回结果数据类型
 * @return
 */
<V> V getWithCache(CacheConf cacheKey, Callable<V> reloadTask, Predicate<V> predicate, Object... keyParams) throws Exception;
```
* 使用示例
```java
Integer category = 2;
Integer val = cacheUtil.getWithCache(CacheKey.SEA_ITEM_CATEGORY, () -> category + 1, v -> v > 0, category);
```

### 3.4 批量缓存
* 方法介绍
```java
/**
 * 批量缓存
 *
 * @param cacheKey   缓存key枚举
 * @param reloadTask 缓存未命中加装数据的任务
 * @param keyFunc    生成缓存key的构造参数
 * @param keyParams  生成完整缓存key需要的参数
 * @param <V>        返回结果数据类型
 * @param <T>        加载任务的入参类型
 * @return
 */
<V, T> List<V> getWithCacheList(CacheConf cacheKey, Function<List<T>, List<V>> reloadTask, Function<V, Object[]> keyFunc, List<T> keyParams);
```
* 使用示例一
```java
List<Integer> val = cacheUtil.getWithCacheList(CacheKey.SEA_ITEM_CATEGORY, (l) -> {
    List<Integer> res = new ArrayList<>();
    for (Integer k : l) {
        res.add(k + 1);
    }
    return res;
}, o -> new Object[]{o}, list);
```
* 使用示例二
```java
List<ShopVo> shopVos = idCacheUtil.getWithCacheList(CacheKey.VENDER, shopIds -> idShopRpc.getShopInfoByShopIds(shopIds),
        v -> new Object[]{v.getId()}, new ArrayList<>(shopIdSet));
```

### 3.5 批量缓存：基于3.4，新增判断是否缓存数据结果
* 方法介绍
```java
/**
 * @param cacheKey  {@link #getWithCacheList(CacheConf, Function, Function, List)}
 * @param predicate 是否缓存子句
 * @return
 */
public <V, T> List<V> getWithCacheList(CacheConf cacheKey, Function<List<T>, List<V>> reloadTask, Function<V, Object[]> keyFunc, Predicate<V> predicate, List<T> keyParams)
```

### 3.6 入参Set批量缓存：基于3.4，Set维度入参
* 方法介绍
```java
/**
 * 批量缓存
 *
 * @param cacheKey   缓存key枚举
 * @param reloadTask 缓存未命中加装数据的任务
 * @param keyFunc    生成缓存key的方法
 * @param keyParams  生成完整缓存key需要的参数
 * @param <V>        返回结果数据类型
 * @param <P>        加载任务的入参类型
 */
public <V, P> List<V> getListWithCacheSet(CacheConf cacheKey, Function<Set<P>, List<V>> reloadTask, Function<V, Object[]> keyFunc, Set<P> keyParams)
```
* 使用示例
```java
Set<Integer> list = Sets.newHashSet(7, 8);
List<Integer> val = cacheUtil.getListWithCacheSet(CacheKey.SEA_ITEM_CATEGORY, (l) -> {
    List<Integer> res = new ArrayList<>();
    for (Integer k : l) {
        res.add(k + 1);
    }
    return res;
}, o -> new Object[]{o}, list);
```

### 3.7 入参Set批量缓存：基于3.6，新增判断是否缓存数据结果
* 方法介绍
```java
/**
 * @param cacheKey  {@link #getListWithCacheSet(CacheConf, Function, Function, Set)}
 * @param predicate 是否缓存子句
 */
public <V, P> List<V> getListWithCacheSet(CacheConf cacheKey, Function<Set<P>, List<V>> reloadTask, Function<V, Object[]> keyFunc, Predicate<V> predicate, Set<P> keyParams)
```

### 3.8 List2Map批量缓存：基于3.4，出参map
* 方法介绍
```java
/**
 * 批量缓存
 *
 * @param cacheKey     缓存key枚举
 * @param reloadTask   缓存未命中加装数据的任务
 * @param mapKeyFunc   生成map key的方法
 * @param cacheKeyFunc 生成缓存key的方法
 * @param keyParams    生成完整缓存key需要的参数
 * @param <V>          返回结果数据类型
 * @param <P>          加载任务的入参类型
 * @param <K>          map的key类型
 * @return
 */
public <V, P, K> Map<K, V> getMapWithCacheList(CacheConf cacheKey, Function<List<P>, Map<K, V>> reloadTask,
                                                   BiFunction<P, V, K> mapKeyFunc, BiFunction<K, V, Object[]> cacheKeyFunc,
                                                   List<P> keyParams)
```
* 使用示例
```java
List<Integer> list = Lists.newArrayList(9, 10);
// 示例1：包装类型，可直接接收
Map<Integer, Integer> val = cacheUtil.getMapWithCacheList(CacheKey.SEA_ITEM_CATEGORY, (l) -> {
    Map<Integer, Integer> res = new HashMap<>();
    for (Integer k : l) {
        res.put(k, k + 1);
    }
    return res;
}, (k, v) -> k, (k, v) -> new Object[]{k}, list);

// 示例2：基本类型，需使用Number来接收
Map<Integer, Number> val2 = cacheUtil.getMapWithCacheList(CacheKey.SEA_ITEM_CATEGORY, (l) -> {
    Map<Integer, Number> res = new HashMap<>();
    for (Integer k : l) {
        res.put(k, (long) (k + 1));
    }
    return res;
}, (k, v) -> k, (k, v) -> new Object[]{k}, list);
```

### 3.9 List2Map批量缓存：基于3.8，新增判断是否缓存数据结果
* 方法介绍
```java
/**
 * @param cacheKey  {@link #getMapWithCacheList(CacheConf, Function, BiFunction, BiFunction, List)}
 * @param predicate 是否缓存子句
 * @return
 */
public <V, P, K> Map<K, V> getMapWithCacheList(CacheConf cacheKey, Function<List<P>, Map<K, V>> reloadTask,
                                                   BiFunction<P, V, K> mapKeyFunc, BiFunction<K, V, Object[]> cacheKeyFunc,
                                                   Predicate<V> predicate, List<P> keyParams)
```

### 3.10 Set2Map批量缓存：基于3.8，入参Set
* 方法介绍
```java
/**
 * 批量缓存
 *
 * @param cacheKey     缓存key枚举
 * @param reloadTask   缓存未命中加装数据的任务
 * @param mapKeyFunc   生成map key的方法
 * @param cacheKeyFunc 生成缓存key的方法
 * @param keyParams    生成完整缓存key需要的参数
 * @param <V>          返回结果数据类型
 * @param <P>          加载任务的入参类型
 * @param <K>          map的key类型
 * @return
 */
public <V, P, K> Map<K, V> getMapWithCacheSet(CacheConf cacheKey, Function<Set<P>, Map<K, V>> reloadTask,
                                                   BiFunction<P, V, K> mapKeyFunc, BiFunction<K, V, Object[]> cacheKeyFunc,
                                                   Set<P> keyParams)
```
* 使用示例一
```java
Set<Integer> list = Sets.newHashSet(13, 14);
Map<Integer, Number> val = cacheUtil.getMapWithCacheSet(CacheKey.SEA_ITEM_CATEGORY, (l) -> {
    Map<Integer, Number> res = new HashMap<>();
    for (Integer k : l) {
        res.put(k, (long) (k + 1));
    }
    return res;
}, (k, v) -> k, (k, v) -> new Object[]{k}, list);
```
* 使用示例二
```java
// 批量缓存赠品
Map<String, List<Image>> imgMultipleMap = idCacheUtil.getMapWithCacheSet(CacheKey.PRODUCT_IMAGE,
        l -> idProductRpc.getSkuImgBySkuId(l), (k, v) -> k, (k, v) -> new Object[]{k}, skuIds);
```

### 3.11 Set2Map批量缓存：基于3.10，新增判断是否缓存数据结果
* 方法介绍
```java
/**
 * @param cacheKey  {@link #getMapWithCacheSet(CacheConf, Function, BiFunction, BiFunction, Set)}
 * @param predicate 是否缓存子句
 * @return
 */
public <V, P, K> Map<K, V> getMapWithCacheSet(CacheConf cacheKey, Function<Set<P>, Map<K, V>> reloadTask,
                                               BiFunction<P, V, K> mapKeyFunc, BiFunction<K, V, Object[]> cacheKeyFunc,
                                               Predicate<V> predicate, Set<P> keyParams)
```

### 3.12 批量并发任务缓存（流程编排）（依赖 epx-tool-concurrent）
* 方法介绍
```java
/**
 * 批量任务，当最终<=1个任务走rpc时，会同步执行；否则异步执行
 */
public void getWithCacheBatch(CacheDomain cacheDomain) throws Exception;
```
* 使用示例一
```java
cacheUtil.getWithCacheBatch(CacheDomain.build()
        // 缓存任务
        .add(CacheKey.SEA_ITEM_CATEGORY, () -> {
            log.info("category:{}", category);
            return category + 1;
        }, val::set, category)
        // 普通缓存任务
        .add(CacheKey.SEA_ITEM_SKU_INFO, Test1::new, t1 -> t1.setName("zsf"), category)
        // 不走缓存的任务
        .add(() -> {
            List<Integer> res = new ArrayList<>();
            for (Integer k : list) {
                res.add(k + 1);
            }
            vals.set(res);
        })
        // 支持前置条件判断任务是否执行
        .add(r -> r != null, () -> {
            List<Integer> res = new ArrayList<>();
            for (Integer k : list) {
                res.add(k + 1);
            }
            vals.set(res);
        })
        // 支持带 缓存、前置条件 任务
        .add(CacheKey.WARE_STICKER_BY_MARKER, v -> sticker != null, () -> stickerRpc.querySticker(sticker), stickerVo -> {
            if (stickerVo == null) {
                return;
            }
            idAbsProductItemDecorator.setImgUri(stickerVo.getImgUri());
        }, sticker));
```
* 使用示例二
```java
// 商品域缓存信息
idCacheUtil.getWithCacheBatch(CacheDomain.build()
        // sticker
        .add(CacheKey.WARE_STICKER_BY_MARKER, v -> sticker != null, () -> stickerRpc.querySticker(sticker), stickerVo -> {
            if (stickerVo == null) {
                return;
            }
            idAbsProductItemDecorator.setPositionX(stickerVo.getPositionX());
            idAbsProductItemDecorator.setPositionY(stickerVo.getPositionY());
            idAbsProductItemDecorator.setLength(stickerVo.getLength());
            idAbsProductItemDecorator.setWidth(stickerVo.getWidth());
            idAbsProductItemDecorator.setImgUri(stickerVo.getImgUri());
        }, sticker)
        // 角标
        .add(CacheKey.WARE_LOGO, () -> stickerRpc.getAffiliatedPic(spuId), affiliatedPictureVo -> {
            if (affiliatedPictureVo == null) {
                log.info("getAffiliatedPic 为null");
                return;
            }
            idAbsProductItemDecorator.setLogoUri(affiliatedPictureVo.getLogoUri());
        }, spuId)
        // 物权
        .add(CacheKey.WARE_PROPERTY_RIGHT,
                () -> idProductService.getPropertyRight(spuId), idAbsProductItemDecorator::setPropertyRight, spuId)
        // 前台类目
        .add(CacheKey.FRONT_CATEGORY_LIST, () -> idCategoryService.getCmCategoryByCateId(NumberUtil.toLong(bo.getCategory3()), fontCateType), cmCategoryVo -> {
            if (cmCategoryVo == null) {
                return;
            }
            if (cmCategoryVo.getLevel() == 3) {
                idAbsProductItemDecorator.setCmCatId3(cmCategoryVo.getCmCategoryId());
                idAbsProductItemDecorator.setCmCatName3(cmCategoryVo.getNameEn());
                idAbsProductItemDecorator.setCmCatUrl3(getCateUrl(cmCategoryVo));
                cmCategoryVo = cmCategoryVo.getParentCmCategoryVo();
            }
            if (cmCategoryVo.getLevel() == 2) {
                idAbsProductItemDecorator.setCmCatId2(cmCategoryVo.getCmCategoryId());
                idAbsProductItemDecorator.setCmCatName2(cmCategoryVo.getNameEn());
                idAbsProductItemDecorator.setCmCatUrl2(getCateUrl(cmCategoryVo));
                cmCategoryVo = cmCategoryVo.getParentCmCategoryVo();
            }
            if (cmCategoryVo.getLevel() == 1) {
                idAbsProductItemDecorator.setCmCatId1(cmCategoryVo.getCmCategoryId());
                idAbsProductItemDecorator.setCmCatName1(cmCategoryVo.getNameEn());
                idAbsProductItemDecorator.setCmCatUrl1(getCateUrl(cmCategoryVo));
            }
        }, bo.getCategory3(), fontCateType));
```


## 四、功能-锁工具

### 4.1 锁失败指定异常
* 方法介绍
```java
/**
 * 分布式锁执行方法,释放锁
 *
 * @param cacheInterface 缓存配置
 * @param callback       方法体
 * @param error          生成指定异常的方法体
 * @param <T>            泛型
 * @return 方法体执行结果
 */
public <T, E extends Exception> T execute(CacheConf cacheInterface, Func<T> callback, Func<E> error, Object... keyParams)
```
* 使用示例
```java
Long val = epxLockUtil.execute(CacheKey.LOCK_INIT_BIZ_ORG, () -> bizOrgService.initBizOrg(req),
                () -> new BizException(AuthErrorEnums.ERROR_INIT_BIZ_ORG_CONCURRENCY),
                req.getAccountId());
```

### 4.2 锁失败抛出默认异常 EpxException
* 方法介绍
```java
/**
 * 分布式锁执行方法,释放锁
 *
 * @param cacheInterface 缓存配置
 * @param callback       方法体
 * @param <T>            泛型
 * @return 方法体执行结果
 */
public <T, E extends Exception> T execute(CacheConf cacheInterface, Func<T> callback, Object... keyParams)
```
* 使用示例
```java
Long val = epxLockUtil.execute(CacheKey.LOCK_INIT_BIZ_ORG, () -> bizOrgService.initBizOrg(req),
                req.getAccountId());
```


## 四、高级配置

### 4.1 ducc动态配置缓存过期时间 `${cacheKey}-expire`
```text
// ducc配置 缓存过期时间1天（精确到秒）
sea_item_category-expire=86400
```

### 4.2 ducc动态配置缓存更新时间 `${cacheKey}-update`
```text
// ducc配置 缓存过期时间5分钟（精确到秒）
sea_item_category-update=300
```