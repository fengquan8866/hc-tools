package cn.hc.tool.common.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 高并发场景下System.currentTimeMillis()的性能问题的优化
 * 时间戳打印建议使用
 * 参考： <a href="https://blog.csdn.net/qq_38011415/java/article/details/82813299">高并发下System.currentTimeMillis()并发问题以及优化对比</a>
 */
@Slf4j
public class SystemClock {
    private static final String THREAD_NAME = "system.clock";
    private static final SystemClock MILLIS_CLOCK = new SystemClock(1);
    private final long precision;
    private final AtomicLong now;
    private static Set<AtomicLong> timeSet;

    private SystemClock(long precision) {
        this.precision = precision;
        now = new AtomicLong(System.currentTimeMillis());
        scheduleClockUpdating();
    }

    public static SystemClock millisClock() {
        return MILLIS_CLOCK;
    }

    public static synchronized void addField(AtomicLong field) {
        if (field == null) {
            return;
        }
        if (timeSet == null) {
            timeSet = new CopyOnWriteArraySet<AtomicLong>();
        }
        field.set(currentTimeMillis());
        timeSet.add(field);
    }

    private void scheduleClockUpdating() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, THREAD_NAME);
                thread.setDaemon(true);
                return thread;
            }
        });
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                long t = System.currentTimeMillis();
                now.set(t);
                if (timeSet != null) {
                    for (AtomicLong f : timeSet) {
                        f.set(t);
                    }
                }
            }
        }, precision, precision, TimeUnit.MILLISECONDS);
    }

    public long now() {
        return now.get();
    }

    public static long currentTimeMillis() {
        return millisClock().now.get();
    }

    public static Date currentDate() {
        return new Date(currentTimeMillis());
    }
}