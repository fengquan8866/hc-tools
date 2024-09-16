package cn.hc.tool.common.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class SystemClockTest {
    @Test
    public void now() {
        int times = Integer.MAX_VALUE / 100;

        long start = System.currentTimeMillis();
        for (long i = 0; i < times; i++) {
            SystemClock.currentTimeMillis();
        }
        long end = System.currentTimeMillis();

        log.info("SystemClock Time:{}毫秒", end - start);

        long start2 = System.currentTimeMillis();
        for (long i = 0; i < times; i++) {
            System.currentTimeMillis();
        }
        long end2 = System.currentTimeMillis();
        log.info("SystemCurrentTimeMillis Time:{}毫秒", end2 - start2);
        log.info("cutTime: {}", System.currentTimeMillis());
        log.info("cutTime: {}", SystemClock.millisClock().now());
        log.info("curDate: {}", SystemClock.currentDate());
    }

    @Test
    public void addField() throws InterruptedException {
        if (ClockTest.now == null) {
            ClockTest.now = new AtomicLong();
        }
        SystemClock.addField(ClockTest.now);
        log.info("ClockTest.now1: {}", ClockTest.now.get());
        Thread.sleep(100);
        log.info("ClockTest.now2: {}", ClockTest.now.get());
    }

    @Test
    public void set() {
        ClockTest.now = new AtomicLong();
        ClockTest.now.set(System.currentTimeMillis());
        log.info("ClockTest.now1: {}", ClockTest.now);
        aa(ClockTest.now);
        log.info("ClockTest.now2: {}", ClockTest.now);
    }

    private static void aa(AtomicLong now) {
        now.set(3);
    }
}

@Data
class ClockTest {
    protected static AtomicLong now;
}