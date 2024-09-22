package cn.hc.tool.cache.util;

import cn.hc.tool.cache.bean.CacheKey;
import cn.hc.tool.common.util.SilentUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CountDownLatch;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/22 16:28
 */
@RunWith(SpringRunner.class)
@ComponentScan(basePackages = {"cn.hc.tool.cache"})
@SpringBootTest(classes = {ToolLockUtilTest.class})
@Slf4j
@EnableAutoConfiguration
public class ToolLockUtilTest {

    @Autowired
    private ToolLockUtil toolLockUtil;

    @Test
    public void lock2() {
        new RunnableTask("other", toolLockUtil).run();
    }

    @Test
    public void lock() {
        CountDownLatch cdl = new CountDownLatch(2);
        Thread t1 = new Thread(new RunnableTask("thread1", toolLockUtil, cdl));
        Thread t2 = new Thread(new RunnableTask("thread2", toolLockUtil, cdl));
        t1.start();
        t2.start();
        SilentUtil.doWith(() -> cdl.await(5000, java.util.concurrent.TimeUnit.MILLISECONDS));
        new RunnableTask("other", toolLockUtil).run();
    }

    static class RunnableTask implements Runnable {
        private final String name;
        private final ToolLockUtil toolLockUtil;
        private final CountDownLatch cdl;

        public RunnableTask(String name, ToolLockUtil toolLockUtil) {
            this(name, toolLockUtil, null);
        }

        public RunnableTask(String name, ToolLockUtil toolLockUtil, CountDownLatch cdl) {
            this.name = name;
            this.toolLockUtil = toolLockUtil;
            this.cdl = cdl;
        }

        @Override
        public void run() {
            log.info("开始{}", name);
            try {
                String rst = toolLockUtil.execute(CacheKey.LOCK, () -> {
                    log.info("在锁中:{}", name);
                    SilentUtil.doWith(() -> Thread.sleep(200));
                    return "ok-" + name;
                });
                log.info("结果-{}：{}", name, rst);
                if (cdl != null) cdl.countDown();
            } catch (Exception e) {
                log.error("err in lock {}", name, e);
            }
        }
    }
}
