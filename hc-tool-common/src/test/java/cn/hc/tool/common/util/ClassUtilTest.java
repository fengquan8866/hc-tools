package cn.hc.tool.common.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author huangchao E-mail:cdhuangchao3@jd.com
 * @version 创建时间：2020/3/29 0:22
 */
@Slf4j
public class ClassUtilTest {
    public static final Integer ABC = 1;
    public static final String BCD = "bcd";
    public static final String CDE = null != null ? "abc" : "bcd";
    public static final String DEF = new String("abc");
    public static final String EFG = "efg";

    /**
     * 修改final属性值
     */
    @Test
    public void setIntValFinalTest() throws IllegalAccessException {
        log.info("setIntValFinalTest.before val:{}", ABC);
        Field f = ClassUtil.getField(ClassUtilTest.class, "ABC");
        ClassUtil.set(ClassUtilTest.class, f, 2);
        log.info("setIntValFinalTest.after val:{}", ABC);
    }

    /**
     * 修改final属性值
     */
    @Test
    public void setStrValFinalTest() throws IllegalAccessException {
        log.info("setIntValFinalTest.before val:{}", BCD);
        Field f = ClassUtil.getField(ClassUtilTest.class, "BCD");
        ClassUtil.set(ClassUtilTest.class, f, "BCD");
        log.info("setIntValFinalTest.after val:{}", BCD);
    }
    /**
     * 修改final属性值
     */
    @Test
    public void setStringValFinalTest() throws IllegalAccessException {
        log.info("setStringValFinalTest.before val:{}", CDE);
        Field f = ClassUtil.getField(ClassUtilTest.class, "CDE");
        ClassUtil.set(ClassUtilTest.class, f, "CDE");
        log.info("setStringValFinalTest.after val:{}", CDE);
    }
    /**
     * 修改final属性值
     */
    @Test
    public void setStr2ValFinalTest() throws IllegalAccessException {
        log.info("setStr2ValFinalTest.before val:{}", DEF);
        Field f = ClassUtil.getField(ClassUtilTest.class, "DEF");
        ClassUtil.set(ClassUtilTest.class, f, "DEF");
        log.info("setStr2ValFinalTest.after val:{}", DEF);
    }

    @Test
    public void parseObjTest() {
        Boolean b = ClassUtil.parseObj("false", null, Boolean.class);
        log.info("b = {}, b.class=={}", b, b.getClass());
    }

    @Test
    public void getSimpleName() {
        Assertions.assertEquals(ClassUtil.getSimpleName("com.jd.Test$$aaa"), "Test");
    }

    @Test
    public void isVoid() {
        Method method = ClassUtil.getDeclareMethodByName(ClassUtilTest.class, "isVoid");
        log.info("ClassUtilTest.isVoid: {}", ClassUtil.isVoid(method));

        method = ClassUtil.getDeclareMethodByName(ClassUtil.class, "isVoid");
        log.info("ClassUtil.isVoid: {}", ClassUtil.isVoid(method));
    }

    @Test
    public void invoke() throws InvocationTargetException, IllegalAccessException {
        Method method = ClassUtil.getDeclareMethodByName(ClassUtilTest.class, "isVoid");
        Object invoke = ClassUtil.invoke(method, new ClassUtilTest());
        log.info("res: {}", invoke);
    }

    @Test
    public void getStaticFieldValue() throws IllegalAccessException {
        Object code = ClassUtil.getStaticFieldValue(ClassUtilTest.class, "EFG");
        log.info("code:{}", code);
    }
}