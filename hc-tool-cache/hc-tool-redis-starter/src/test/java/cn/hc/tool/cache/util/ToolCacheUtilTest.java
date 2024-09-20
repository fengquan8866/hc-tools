package cn.hc.tool.cache.util;

import cn.hc.tool.cache.bean.CacheConf;
import cn.hc.tool.cache.bean.CacheKey;
import com.hc.json.adapter.Json;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/16 18:27
 */
@RunWith(SpringRunner.class)
@ComponentScan(basePackages = {"cn.hc.tool.cache"})
@SpringBootTest(classes = {ToolCacheUtilTest.class})
@Slf4j
@EnableAutoConfiguration
public class ToolCacheUtilTest {

    @Autowired
    private ToolCacheUtil cacheUtil;

    @Test
    public void confMap() {
        log.info("--{}", CacheKey.SKU_INFO.getConfKey());
        log.info("2---{}", CacheConf.confMap);
    }

    /**
     * 测试范围：1-5
     */
    @Test
    public void get() {
        Integer sku = 1;
        Integer val = cacheUtil.get(CacheKey.SKU_INFO, Integer.class, () -> {
            if (sku > 0) {
                log.info("sku:{}", sku);
            }
            return sku + 1;
        }, sku);
        log.info("val:{}", val);
    }

    /**
     * 测试范围：6-10
     */
    @Test
    public void getFromList() {
        List<Integer> list = Arrays.asList(6, 7);
        List<Integer> val = cacheUtil.getFromList(CacheKey.SKU_INFO2, Integer.class, (l) -> {
            List<Integer> res = new ArrayList<>();
            for (Integer k : l) {
                res.add(k + 1);
            }
            return res;
        }, list);
        log.info("val:{}", val);
        val = cacheUtil.getFromList(CacheKey.SKU_INFO2, Integer.class, (l) -> {
            List<Integer> res = new ArrayList<>();
            for (Integer k : l) {
                res.add(k + 1);
            }
            return res;
        }, (k, o) -> new Object[]{k}, list);
        log.info("val:{}", val);
    }

    /**
     * 测试范围：11-15
     */
    @Test
    public void getListFromSet() {
        Set<Integer> list = Sets.set(11, 12);
        List<Integer> val = cacheUtil.getListFromSet(CacheKey.SKU_INFO2, Integer.class, (l) -> {
            List<Integer> res = new ArrayList<>();
            for (Integer k : l) {
                res.add(k + 1);
            }
            return res;
        }, list);
        log.info("val:{}", val);
        val = cacheUtil.getListFromSet(CacheKey.SKU_INFO2, Integer.class, (l) -> {
            List<Integer> res = new ArrayList<>();
            for (Integer k : l) {
                res.add(k + 1);
            }
            return res;
        }, (k, o) -> new Object[]{k}, list);
        log.info("val:{}", val);
    }

    /**
     * 测试范围：16-20
     */
    @Test
    public void getMapFromList() {
        List<Integer> list = Arrays.asList(16, 20);
        Map<Integer, Integer> val = cacheUtil.getMapFromList(CacheKey.SKU_INFO2, Integer.class, (p) -> {
            Map<Integer, Integer> res = new HashMap<>();
            for (Integer k : p) {
                res.put(k, k + 1);
            }
            return res;
        }, list);
        log.info("val:{}", val);
        val = cacheUtil.getMapFromList(CacheKey.SKU_INFO2, Integer.class, (l) -> {
            Map<Integer, Integer> res = new HashMap<>();
            for (Integer k : l) {
                res.put(k, k + 1);
            }
            return res;
        }, (k, v) -> k, (k, v) -> new Object[]{k}, list);
        log.info("val:{}", val);
    }

    /**
     * 测试范围：21-25
     */
    @Test
    public void getMapFromSet() {
        Set<Integer> list = Sets.set(21, 22);
        Map<Integer, Integer> val = cacheUtil.getMapFromSet(CacheKey.SKU_INFO2, Integer.class, (l) -> {
            Map<Integer, Integer> res = new HashMap<>();
            for (Integer k : l) {
                res.put(k, k + 1);
            }
            return res;
        }, list);
        log.info("val:{}", val);
        val = cacheUtil.getMapFromSet(CacheKey.SKU_INFO2, Integer.class, (l) -> {
            Map<Integer, Integer> res = new HashMap<>();
            for (Integer k : l) {
                res.put(k, k + 1);
            }
            return res;
        }, (k, v) -> k, (k, v) -> new Object[]{k}, list);
        log.info("val:{}", val);
    }

}
