package cn.hc.tool.config.util;

import cn.hc.tool.common.constant.EnvEnum;
import cn.hc.tool.common.util.ClassUtil;
import cn.hc.tool.common.util.IpUtil;
import cn.hc.tool.common.util.NumberUtil;
import cn.hc.tool.config.annotation.ConfigAnno;
import com.hc.json.adapter.Json;
import com.hc.json.adapter.TypeToken;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抽象配置类
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/15 19:44
 */
@Slf4j
public abstract class AbsConfiguration {

    /**
     * 常量类集
     */
    private static final Set<Class<?>> CLASSES = new HashSet<Class<?>>();
    public static final List<String> PKGS_AUTO_SCAN = new ArrayList<>();

    /**
     * 是否开启自动扫描
     */
    @Setter
    private static boolean autoScan = false;

    /**
     * 环境名称
     */
    @Getter
    private String profile;

    /**
     * 属性缓存
     */
    private static final Map<String, Set<Field>> FIELD_MAP = new ConcurrentHashMap<String, Set<Field>>();

    /**
     * 默认值缓存
     */
    private static final Map<Field, Object> DEFAULT_DATA_MAP = new ConcurrentHashMap<Field, Object>();

    /**
     * 初始化
     * @exception Exception 初始化异常
     */
    protected abstract void init() throws Exception;

    /**
     * 获取数据
     * @param key 配置key
     */
    public abstract Object getObject(String key);

    /**
     * 获取配置（字符串格式）
     *
     * @param key 配置key
     */
    public abstract String getStr(String key);

    /**
     * 获取Integer格式配置
     *
     * @param key 配置key
     */
    public Integer getInteger(String key) {
        Object val = getObject(key);
        return val == null ? null : NumberUtil.toInteger(val);
    }

    /**
     * 获取布尔格式配置
     *
     * @param key 配置key
     */
    public Boolean getBool(String key) {
        Object val = getObject(key);
        return val == null ? null : Boolean.parseBoolean(val.toString());
    }

    /**
     * 获取布尔格式配置
     *
     * @param key 配置key
     */
    public boolean bool(String key) {
        Object val = getObject(key);
        return val != null && Boolean.parseBoolean(val.toString());
    }

    /**
     * 获取ip维度配置
     */
    public String getIpConf(String key) {
        String val = getStr(key);
        return resolveIpConf(val);
    }

    /**
     * 解析ip维度配置
     */
    public String resolveIpConf(String val) {
        String ip = IpUtil.getLocalIpAddress();
        log.info("config ip: {}", ip);
        Map<String, String> map = Json.fromJson(val, new TypeToken<Map<String, String>>() {
        });
        String v = map.get(ip);
        return v == null ? map.get("All") : v;
    }

    /**
     * TODO 需要初始化机制
     * @throws Exception
     */
    public void config() throws Exception {
        scanAnno();
        // TODO 绑定config工具类，这里有点耦合，待改进
        ConfigUtil.setAbsConfiguration(this);
        try {
            init();
        } catch (Exception e) {
            log.error("初始化config失败！", e);
        }
    }

    /**
     * 扫描含有注解的类
     * TODO 需要初始化带扫描包路径
     */
    private void scanAnno() {
        if (autoScan) {
            for (String pkg : PKGS_AUTO_SCAN) {
                for (Class<?> cla : ClassUtil.findClassesInPackageByAnn(pkg, ConfigAnno.class)) {
                    initFields(cla);
                }
            }
        }
    }

    /**
     * 修改单个字段值
     */
    protected void changeProp(String key, String value) {
        Set<Field> list = FIELD_MAP.get(key);
        if (list != null) {
            for (Field f : list) {
                try {
                    Object obj;
                    if (value == null) {
                        obj = null;
//                    } else if (f.getAnnotation(IpConf.class) != null) {
//                        if (empty(value)) {
//                            obj = null;
//                        } else {
//                            obj = resolveIpConf(value);
//                        }
                    } else if (String.class.isAssignableFrom(f.getType())) {
                        obj = value.trim();
                    } else {
                        obj = ClassUtil.parseObj(value, f.getGenericType(), f.getType());
                    }
                    Object oldVal = f.get(f.getDeclaringClass());
                    ClassUtil.set(f.getDeclaringClass(), f, obj);
                    if ((obj == null && oldVal != null) || (obj != null && !obj.equals(oldVal))) {
                        log.info("AbsConfiguration 更新数据，key：{}，value：{}", key, value);
                        ConfigFactory.exec(key, oldVal, obj);
                    }
                } catch (Exception e) {
                    log.error("ducc 更新数据失败, field:{}, val:{}", f.toString(), value, e);
                }
            }
        } else {
            ConfigFactory.exec(key, null, value);
        }
    }

    private boolean empty(String val) {
        return val == null || val.isEmpty() || val.trim().isEmpty();
    }

    /**
     * 初始化属性集
     */
    private static void initFields(Class<?> cla) {
        if (cla == null) {
            log.error("initFields cla is empty!");
            return;
        }
        for (Field f : cla.getDeclaredFields()) {
            if (!FIELD_MAP.containsKey(f.getName())) {
                FIELD_MAP.put(f.getName(), new HashSet<Field>());
            }
            FIELD_MAP.get(f.getName()).add(f);
            if (!DEFAULT_DATA_MAP.containsKey(f)) {
                try {
                    Object defVal = ClassUtil.get(cla, f);
                    if (defVal != null) {
                        DEFAULT_DATA_MAP.put(f, defVal);
                    }
                } catch (Exception e) {
                    log.error("config缓存默认失败：{}#{}", f.getDeclaringClass().getName(), f.getName(), e);
                }
            }
        }
    }

    /**
     * 添加常量类
     */
    public static void addConstant(Class<?> cla) {
        if (!CLASSES.contains(cla)) {
            CLASSES.add(cla);
            initFields(cla);
            log.info("success in initFields: {}", cla.getName());
        } else {
            log.info("ucc class 已存在：{}", cla.getName());
        }
    }

    /**
     * 设置环境（可识别通用环境）
     */
    public void setProfile(String profile) {
        this.profile = EnvEnum.getEnvName(profile);
    }

}
