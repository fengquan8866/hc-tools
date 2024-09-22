package cn.hc.tool.cache.aspect;

import cn.hc.tool.cache.util.HcCacheManager;
import cn.hc.tool.cache.util.HcToolCache;
import cn.hc.tool.cache.util.ToolCacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/21 11:17
 */
@Aspect
@Component
@Slf4j
public class HcCacheAspect implements Ordered {
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private ToolCacheUtil toolCacheUtil;

    private final Set<String> cacheableSet = new HashSet<>();
    private final Set<String> cachePutSet = new HashSet<>();
    private final Set<String> cacheEvictSet = new HashSet<>();

    @Before("@annotation(org.springframework.cache.annotation.Cacheable)")
    public void beforeCacheable(JoinPoint pjp) throws Throwable {
        MethodSignature ms = (MethodSignature) pjp.getSignature();
        String methodFullName = ms.getMethod().toString();
        if (cacheableSet.contains(methodFullName)) {
            return;
        }
        cacheableSet.add(methodFullName);
        log.info("beforeCacheable: {}", methodFullName);
        setCacheNames(ms, CacheConfig.class);
        setCacheNames(ms, Cacheable.class);
    }

    @Before("@annotation(org.springframework.cache.annotation.CachePut)")
    public void beforeCachePut(JoinPoint pjp) throws Throwable {
        MethodSignature ms = (MethodSignature) pjp.getSignature();
        String methodFullName = ms.getMethod().toString();
        if (cachePutSet.contains(methodFullName)) {
            return;
        }
        cachePutSet.add(methodFullName);
        log.info("beforeCachePut: {}", methodFullName);
        setCacheNames(ms, CacheConfig.class);
        setCacheNames(ms, CachePut.class);
    }

    @Before("@annotation(org.springframework.cache.annotation.CacheEvict)")
    public void beforeCacheEvict(JoinPoint pjp) throws Throwable {
        MethodSignature ms = (MethodSignature) pjp.getSignature();
        String methodFullName = ms.getMethod().toString();
        if (cacheEvictSet.contains(methodFullName)) {
            return;
        }
        cacheEvictSet.add(methodFullName);
        log.info("beforeCacheEvict: {}", methodFullName);
        setCacheNames(ms, CacheConfig.class);
        setCacheNames(ms, CacheEvict.class);
    }

    private void setCacheNames(MethodSignature ms, Class cls) {
        log.info("setCacheNames--------------");
        Annotation[] annotationsClass = ms.getDeclaringType().getAnnotationsByType(cls);
        Annotation[] annotationsMethod = ms.getMethod().getAnnotationsByType(cls);
        Set<Annotation> set=new HashSet<>();
        set.addAll(Arrays.asList(annotationsClass));
        set.addAll(Arrays.asList(annotationsMethod));

        Map<String, HcToolCache> caches = ((HcCacheManager) cacheManager).getCaches();
        for (Annotation item : set) {
            if (item instanceof CacheConfig) {
                CacheConfig config = (CacheConfig) item;
                for (String s : config.cacheNames()) {
                    caches.put(ms.getMethod().toString(), new HcToolCache(true, s, toolCacheUtil, ms.getMethod().getGenericReturnType()));
                }
            } else if (item instanceof Cacheable) {
                Cacheable config = (Cacheable) item;
                String[] strings = config.cacheNames();
                String[] values = config.value();
                Set<String> nameSet =new HashSet<>();
                nameSet.addAll(Arrays.asList(strings));
                nameSet.addAll(Arrays.asList(values));
                for (String s : nameSet) {
                    caches.put(ms.getMethod().toString(), new HcToolCache(true, s, toolCacheUtil, ms.getMethod().getGenericReturnType()));
                }
            }
        }
//        ((HcCacheManager) cacheManager).setCaches(setCache);
        ((HcCacheManager) cacheManager).initializeCaches();
    }

    // 优先执行
    @Override
    public int getOrder() {
        return -999;
    }
}
