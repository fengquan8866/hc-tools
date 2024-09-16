package cn.hc.tool.common.util;

import com.hc.json.adapter.Json;
import lombok.extern.slf4j.Slf4j;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 类相关的工具类
 *
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/15 14:39
 */
@Slf4j
public class ClassUtil {

    /**
     * 代理类名分隔符
     */
    private static final String SEPARATOR_PROXY_CLASS = "$$";
    /**
     * 接口生成的动态代理类
     */
    private static final String[] INTERFACE_PROXY_CLASS = {"$Proxy", "_proxy_"};

    public static void main(String[] args) throws Exception {
        /*
         * List<Class> classes = ClassUtil.getAllClassByInterface(Class.forName(
         * "com.threeti.dao.base.IGenericDao")); for (Class clas :classes) {
         * System.out.println(clas.getName()); }
         */
//		List<?> list = getClasses("com.hc.hc");
//		System.out.println(list);
//	    getAnnoByClass(BaseController.class);
//        getAnnoByClass(HelloController.class);
        Boolean b = parseObj("false", null, Boolean.class);
        System.out.println("b = " + b + "==, b.class" + b.getClass());
    }

    /**
     * 获取类的注解，如果没有注解，就递归获取父类注解
     */
    public static <A extends Annotation> A getAnnoByClassAndAnn(Class<?> c, Class<A> annCla) {
        A ann = c.getAnnotation(annCla);
        if (ann == null) {
            if (c.getSuperclass() != Object.class) {
                ann = getAnnoByClassAndAnn(c.getSuperclass(), annCla);
            }
        }
        return ann;
    }

    /**
     * 取得某个接口下所有实现这个接口的类
     */
    public static List<Class<?>> getAllClassByInterface(Class<?> c) {
        List<Class<?>> returnClassList = null;

        if (c.isInterface()) {
            // 获取当前的包名
            String packageName = c.getPackage().getName();
            // 获取当前包下以及子包下所以的类
            List<Class<?>> allClass = getClasses(packageName);
            if (allClass != null) {
                returnClassList = new ArrayList<Class<?>>();
                for (Class<?> classes : allClass) {
                    // 判断是否是同一个接口
                    if (c.isAssignableFrom(classes)) {
                        // 本身不加入进去
                        if (!c.equals(classes)) {
                            returnClassList.add(classes);
                        }
                    }
                }
            }
        }

        return returnClassList;
    }

    /*
     * 取得某一类所在包的所有类名 不含迭代
     */
    public static String[] getPackageAllClassName(String classLocation, String packageName) {
        // 将packageName分解
        String[] packagePathSplit = packageName.split("[.]");
        StringBuilder sb = new StringBuilder(classLocation);
        for (String str : packagePathSplit) {
            sb.append(File.separator).append(str);
        }
        File packeageDir = new File(sb.toString());
        if (packeageDir.isDirectory()) {
            return packeageDir.list();
        }
        return null;
    }

    /**
     * 从包package中获取所有的Class
     */
    public static List<Class<?>> getClasses(String packageName) {

        // 第一个class类的集合
        List<Class<?>> classes = new ArrayList<Class<?>>();
        // 是否循环迭代
        boolean recursive = true;
        // 获取包的名字 并进行替换
        String packageDirName = packageName.replace('.', '/');
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            // 循环迭代下去
            while (dirs.hasMoreElements()) {
                // 获取下一个元素
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                // 如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    // 获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
                } else if ("jar".equals(protocol)) {
                    // 如果是jar包文件
                    // 定义一个JarFile
                    JarFile jar;
                    try {
                        // 获取jar
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        // 从此jar包 得到一个枚举类
                        Enumeration<JarEntry> entries = jar.entries();
                        // 同样的进行循环迭代
                        while (entries.hasMoreElements()) {
                            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            // 如果是以/开头的
                            if (name.charAt(0) == '/') {
                                // 获取后面的字符串
                                name = name.substring(1);
                            }
                            // 如果前半部分和定义的包名相同
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                // 如果以"/"结尾 是一个包
                                if (idx != -1) {
                                    // 获取包名 把"/"替换成"."
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                // 如果可以迭代下去 并且是一个包
                                if ((idx != -1) || recursive) {
                                    // 如果是一个.class文件 而且不是目录
                                    if (name.endsWith(".class") && !entry.isDirectory()) {
                                        // 去掉后面的".class" 获取真正的类名
                                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                                        try {
                                            // 添加到classes
                                            classes.add(Class.forName(String.format("%s.%s", packageName, className)));
                                        } catch (Throwable e) {
                                            // TODO
//                                            log.error("error in classForName: {}.{}", packageName, className);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classes;
    }

    /**
     * 查找{@param packageName}下含有注解{@param annClses}的类
     */
    public static List<Class<?>> findClassesInPackageByAnn(String packageName, Class<? extends Annotation>... annClses) {
        List<Class<?>> result = new ArrayList<Class<?>>();
        List<Class<?>> classes = getClasses(packageName);
        for (Class<?> c : classes) {
            for (Class ann : annClses) {
                if (c.getAnnotation(ann) != null) {
                    result.add(c);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 查找{@param packageName}下含有注解{@param annClses}的方法
     */
    public static List<Method> findMethodInPkgByAnn(String packageName, Class<? extends Annotation>... annClses) {
        List<Method> result = new ArrayList<Method>();
        List<Class<?>> classes = getClasses(packageName);
        for (Class<?> c : classes) {
            for (Method m : c.getDeclaredMethods()) {
                for (Class ann : annClses) {
                    if (m.getAnnotation(ann) != null) {
                        result.add(m);
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * 查找{@param packageName}下含有{@param clsAnns}的类下含有注解{@param annClses}的方法
     */
    public static List<Method> findMethodInPkgByAnns(String packageName, List<Class<? extends Annotation>> clsAnns, Class<? extends Annotation>... annClses) {
        return findMethodInPkgsByAnns(Collections.singletonList(packageName), clsAnns, annClses);
    }

    public static List<Method> findMethodInPkgsByAnns(List<String> packageNames, List<Class<? extends Annotation>> clsAnns, Class<? extends Annotation>... annClses) {
        List<Method> result = new ArrayList<Method>();
        List<Class<?>> classes = new ArrayList<Class<?>>();
        for (String packageName : packageNames) {
            classes.addAll(getClasses(packageName));
        }
        for (Class<?> c : classes) {
            boolean b = false;
            for (Class ann : clsAnns) {
                if (ann != null && c.getAnnotation(ann) != null) {
                    b = true;
                    break;
                }
            }
            if (!b) {
                continue;
            }
            for (Method m : getMethods(c)) {
                for (Class ann : annClses) {
                    if (m.getAnnotation(ann) != null) {
                        result.add(m);
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * 以文件的形式来获取包下的所有Class
     */
    public static void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive,
                                                        List<Class<?>> classes) {
        // 获取此包的目录 建立一个File
        File dir = new File(packagePath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        File[] dirfiles = dir.listFiles(new FileFilter() {
            // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
            @Override
            public boolean accept(File file) {
                return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
            }
        });
        if (dirfiles == null) {
            return;
        }
        // 循环所有文件
        for (File file : dirfiles) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(String.format("%s.%s", packageName, file.getName()), file.getAbsolutePath(), recursive,
                        classes);
            } else {
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    // 添加到集合中去
                    classes.add(Class.forName(String.format("%s.%s", packageName, className)));
                } catch (ClassNotFoundException e) {
                    // TODO 排查具体原因
                    // e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取属性泛型，只针对集合
     *
     * @param field 属性
     * @return 泛型数组，list有1个元素，map有2个元素，可根据下标获取相应class
     */
    public static Type[] getGenericity(Field field) {
        Object type = field.getGenericType();
        if (type instanceof ParameterizedType) {    // grails中，type可能不是ParameterizedType类型
            ParameterizedType pt = (ParameterizedType) type;
            return pt.getActualTypeArguments();
        } else {
            return null;
        }
    }

    /**
     * 获取单列集合泛型class
     *
     * @param field 属性
     * @return 泛型class
     */
    public static Class<?> getCollectionGenericity(Field field) {
        Type[] types = getGenericity(field);
        return (Class<?>) (types != null ? types[0] : null);
    }

    /**
     * 类全路径转换，'.' 转换成 '_'，大写字母 转化成 下划线+小写字母
     * eg foo.oa.hr.AskForLeave -> foo_oa_hr__ask_for_leave
     *
     * @param className className
     */
    public static String formatName(String className) {
        return className.replaceAll("[A-Z]", "_$0").replaceAll("\\.", "_").toLowerCase();
    }

    /**
     * @see #formatName(String)
     */
    public static String formatName(Class<?> cla) {
        return formatName(cla.getName());
    }

    /**
     * 将一个 Map 对象转化为一个 JavaBean
     *
     * @param type 要转化的类型
     * @param map  包含属性值的 map
     * @return 转化出来的 JavaBean 对象
     * @throws IntrospectionException    如果分析类属性失败
     * @throws IllegalAccessException    如果实例化 JavaBean 失败
     * @throws InstantiationException    如果实例化 JavaBean 失败
     * @throws InvocationTargetException 如果调用属性的 setter 方法失败
     */
    public static Object convertMap(Class<? extends Object> type, Map<String, Object> map) throws IntrospectionException, IllegalAccessException, InstantiationException, InvocationTargetException {
        // 获取类属性
        BeanInfo beanInfo = Introspector.getBeanInfo(type);
        // 创建 JavaBean 对象
        Object obj = type.newInstance();

        // 给 JavaBean 对象的属性赋值
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor descriptor : propertyDescriptors) {
            String propertyName = descriptor.getName();

            if (map.containsKey(propertyName)) {
                // 下面一句可以 try 起来，这样当一个属性赋值失败的时候就不会影响其他属性赋值。
                Object value = map.get(propertyName);

                Object[] args = new Object[1];
                args[0] = value;

                descriptor.getWriteMethod().invoke(obj, args);
            }
        }
        return obj;
    }

    /**
     * 将一个 JavaBean 对象转化为一个 Map
     *
     * @param bean 要转化的JavaBean 对象
     * @return 转化出来的  Map 对象
     * @throws IntrospectionException    如果分析类属性失败
     * @throws IllegalAccessException    如果实例化 JavaBean 失败
     * @throws InvocationTargetException 如果调用属性的 setter 方法失败
     */
    public static Map<String, Object> convertBean(Object bean) throws IntrospectionException, IllegalAccessException, InvocationTargetException {
        Class<? extends Object> type = bean.getClass();
        Map<String, Object> returnMap = new HashMap<String, Object>();
        BeanInfo beanInfo = Introspector.getBeanInfo(type);

        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor descriptor : propertyDescriptors) {
            String propertyName = descriptor.getName();
            if (!"class".equals(propertyName)) {
                Method readMethod = descriptor.getReadMethod();
                Object result = readMethod.invoke(bean, new Object[0]);
                returnMap.put(propertyName, result != null ? result : "");
            }
        }
        return returnMap;
    }

    /**
     * 判断类是否含有某个字段
     */
    public static Boolean hasField(Class c, String fieldName) {
        return getField(c, fieldName) != null;
    }

    /**
     * 获取类的属性
     *
     * @param c         源类
     * @param fieldName 属性
     */
    public static Field getField(Class c, String fieldName) {
        Class<?> cl = c;
        while (cl != null && cl != Object.class) {
            for (Field f : cl.getDeclaredFields()) {
                if (f.getName().equals(fieldName)) {
                    return f;
                }
            }
            cl = cl.getSuperclass();
        }
        return null;
    }

    /**
     * 获取类的属性
     *
     * @param c          源类
     * @param fieldClass 属性
     */
    public static Field getField(Class c, Class fieldClass) {
        Class<?> cl = c;
        while (cl != Object.class) {
            for (Field f : cl.getDeclaredFields()) {
                if (f.getType() == fieldClass) {
                    return f;
                }
            }
            cl = cl.getSuperclass();
        }
        return null;
    }

    /**
     * 获取属性值
     */
    public static Object getFieldValue(Object o, Class fieldClass) throws IllegalAccessException {
        Field f = getField(o.getClass(), fieldClass);
        if (f != null) {
            return get(o, f);
        }
        return null;
    }

    /**
     * 获取属性值
     */
    public static Object getFieldValue(Object o, String field) throws IllegalAccessException {
        Field f = getField(o.getClass(), field);
        if (f != null) {
            return get(o, f);
        }
        return null;
    }

    /**
     * 获取静态属性值
     */
    public static Object getStaticFieldValue(Class<?> o, String field) throws IllegalAccessException {
        Field f = getField(o, field);
        if (f != null) {
            return get(o, f);
        }
        return null;
    }

    /**
     * 获取所有属性
     *
     * @param c 类
     * @return 所有属性
     */
    public static List<Field> getAllFields(Class c) {
        List<Field> list = new ArrayList<Field>();
        Class<?> cl = c;
        while (cl != Object.class) {
            list.addAll(Arrays.asList(cl.getDeclaredFields()));
            cl = cl.getSuperclass();
        }
        return list;
    }

    /**
     * 是否为抽象类
     */
    public static Boolean isAbstract(Class clazz) {
        return Modifier.isAbstract(clazz.getModifiers());
    }

    /**
     * 是否为接口类
     */
    public static Boolean isInterface(Class clazz) {
        return Modifier.isInterface(clazz.getModifiers());
    }

    /**
     * 属性赋值
     *
     * @param obj      源对象
     * @param f        属性
     * @param fieldObj 属性对象
     * @throws IllegalAccessException IllegalAccessException
     */
    public static void set(Object obj, Field f, Object fieldObj) throws IllegalAccessException {
        if (f == null || obj == null || fieldObj == null) {
            return;
        }
        boolean b = f.isAccessible();
        try {
            if (Modifier.isFinal(f.getModifiers())) {
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true); //Field 的 modifiers 是私有的
                modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
            }
            f.setAccessible(true);
            f.set(obj, fieldObj);
        } catch (NoSuchFieldException e) {
            log.error("ClassUtil.set", e);
        } finally {
            f.setAccessible(b);
        }
    }

    /**
     * 属性取值
     *
     * @param obj 源对象
     * @param f   属性
     */
    public static Object get(Object obj, Field f) throws IllegalAccessException {
        if (f == null || obj == null) {
            return null;
        }
        boolean b = f.isAccessible();
        try {
            f.setAccessible(true);
            return f.get(obj);
        } finally {
            f.setAccessible(b);
        }
    }

    /**
     * 属性赋值
     *
     * @see #set(Object, Field, Object)
     */
    public static void set(Object obj, String field, Object fieldObj) throws IllegalAccessException {
        if (obj == null || field == null || fieldObj == null) {
            return;
        }
        Field f = getField(obj.getClass(), field);
        set(obj, f, fieldObj);
    }

    /**
     * 加载jar文件
     *
     * @param f 文件路径
     * @throws NoSuchMethodException     NoSuchMethodException
     * @throws MalformedURLException     MalformedURLException
     * @throws InvocationTargetException InvocationTargetException
     * @throws IllegalAccessException    IllegalAccessException
     */
    public static void load(File f) throws NoSuchMethodException, MalformedURLException, InvocationTargetException, IllegalAccessException {
        load(f, Thread.currentThread().getContextClassLoader());
    }

    public static void load(File f, ClassLoader loader) throws NoSuchMethodException, MalformedURLException, InvocationTargetException, IllegalAccessException {
        Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        // 获取方法的访问权限
        boolean accessible = method.isAccessible();
        try {
            if (!accessible) {
                method.setAccessible(true);
            }
            method.invoke(loader, f.toURI().toURL());
        } finally {
            method.setAccessible(accessible);
        }
    }

    /**
     * 获取类（子类+递归父类）所有方法
     */
    public static List<Method> getMethods(Class c) {
        List<Method> result = new ArrayList<Method>();
        Class<?> cl = c;
        while (cl != null && cl != Object.class) {
            result.addAll(Arrays.asList(cl.getDeclaredMethods()));
            cl = cl.getSuperclass();
        }
        return result;
    }

    /**
     * 获取所有方法（子类+递归父类+递归接口）
     */
    public static List<Method> getAllMethods(Class c) {
        List<Method> result = new ArrayList<Method>();
        Class<?> cl = c;
        while (cl != null && cl != Object.class) {
            result.addAll(Arrays.asList(cl.getDeclaredMethods()));
            for (Class<?> anInterface : cl.getInterfaces()) {
                result.addAll(getMethods(anInterface));
            }

            cl = cl.getSuperclass();
        }
        return result;
    }

    /**
     * 获取包下所有方法
     */
    public static List<Method> getMethodsInPkg(String pkgName) {
        return getMethodsInPkgs(Collections.singletonList(pkgName));
    }

    public static List<Method> getMethodsInPkgs(List<String> pkgNames) {
        List<Method> result = new ArrayList<Method>();
        List<Class<?>> classes = new ArrayList<Class<?>>();
        for (String name : pkgNames) {
            classes.addAll(getClasses(name));
        }
        for (Class<?> c : classes) {
            result.addAll(getMethods(c));
        }
        return result;
    }

    /**
     * 获取方法（可获取父类的方法）
     *
     * @param aClass     源class
     * @param methodName 方法名
     * @param argClasses 方法参数
     */
    public static Method getDeclaredMethod(Class<?> aClass, String methodName, Class[] argClasses) throws NoSuchMethodException {
        Method r = null;
        Class<?> cl = aClass;
        NoSuchMethodException ex = null;
        while (cl != Object.class) {
            try {
                r = cl.getDeclaredMethod(methodName, argClasses);
            } catch (NoSuchMethodException e) {
                ex = e;
            }
            cl = cl.getSuperclass();
        }
        if (r == null && ex != null) {
            throw ex;
        }
        return r;
    }

    /**
     * 根据方法名称获取方法
     *
     * @param aClass     类
     * @param methodName 方法名称
     */
    public static Method getDeclareMethodByName(Class<?> aClass, String methodName) {
        List<Method> list = getMethods(aClass);
        for (Method m : list) {
            if (m.getName().equals(methodName)) {
                return m;
            }
        }
        return null;
    }

    /**
     * 根据方法名称获取方法列表
     *
     * @param aClass     类
     * @param methodName 方法名称
     */
    public static List<Method> getDeclareMethodsByName(Class<?> aClass, String methodName) {
        List<Method> result = new ArrayList<Method>();
        for (Method m : getMethods(aClass)) {
            if (m.getName().equals(methodName)) {
                result.add(m);
            }
        }
        return result;
    }

    /**
     * 根据方法名称获取方法列表
     *
     * @param aClass     类
     * @param methodName 方法名称
     */
    public static List<Method> getAllDeclareMethodsByName(Class<?> aClass, String methodName) {
        List<Method> result = new ArrayList<Method>();
        for (Method m : getAllMethods(aClass)) {
            if (m.getName().equals(methodName)) {
                result.add(m);
            }
        }
        return result;
    }

    /**
     * 获取方法（匹配前面几个参数类型）
     *
     * @param aClass     类
     * @param methodName 方法名称
     * @param argClasses 需匹配的参数类型列表
     */
    public static Method getMethodByPrePag(Class<?> aClass, String methodName, Class[] argClasses) {
        List<Method> list = getMethods(aClass);
        for (Method m : list) {
            if (m.getName().equals(methodName)) {
                boolean b = false;
                for (int i = 0; i < argClasses.length; i++) {
                    if (argClasses[i].getName().equals(m.getParameterTypes()[i].getName())) {
                        b = true;
                    } else {
                        b = false;
                        break;
                    }
                }
                if (b) {
                    return m;
                }
            }
        }
        return null;
    }

    /**
     * json转Object，支持布尔、数字
     *
     * @param json json串
     * @param t    类型
     * @param cls  类
     * @param <T>  泛型
     */
    public static <T> T parseObj(String json, Type t, Class<T> cls) {
        if (t instanceof ParameterizedType) {
            // 构造响应类型
            Type parameterizedTypeClass = new ParameterizedType() {
                @Override
                public Type[] getActualTypeArguments() {
                    Type[] actualTypeArguments = ((ParameterizedType) t).getActualTypeArguments();
                    return actualTypeArguments == null ? new Type[0] : actualTypeArguments;
                }

                @Override
                public Type getRawType() {
                    return cls;
                }

                @Override
                public Type getOwnerType() {
                    return t;
                }
            };
//            Type parameterizedTypeClass = new ParameterizedTypeImpl(cls, ((ParameterizedType) t).getActualTypeArguments(), t);
            return Json.fromJson(json, parameterizedTypeClass);
//            return JSON.parseObject(json, parameterizedTypeClass);
        } else {
            return Json.fromJson(json, cls);
//            return JSON.parseObject(json, cls);
        }
    }

    /**
     * 获取类名（去掉$$之后部分）
     */
    public static String getName(Class cla) {
        Class<?> realClass = getRealClass(cla);
        return getClassName(realClass.getName());
    }

    /**
     * 获取类名（去掉$$之后部分）
     */
    public static String getClassName(String claName) {
        int i = claName.indexOf(SEPARATOR_PROXY_CLASS);
        return i > 0 ? claName.substring(0, i) : claName;
    }

    /**
     * 获取类simple name（去掉$$之后部分）
     */
    public static String getSimpleName(Class cla) {
        Class<?> realClass = getRealClass(cla);
        return getSimpleName(realClass.getName());
    }

    /**
     * 获取类simple name
     */
    public static String getSimpleName(String claName) {
        String className = getClassName(claName);
        int i = className.lastIndexOf('.');
        return i > 0 ? className.substring(i + 1) : className;
    }

    /**
     * 获取真实类
     *
     * @param proxyClass 代理生成的类
     */
    public static Class<?> getRealClass(Class<?> proxyClass) {
        if (proxyClass.getName().contains(SEPARATOR_PROXY_CLASS)) {
            return getRealClass(proxyClass.getSuperclass());
        } else if (StringUtil.containsAny(proxyClass.getName(), INTERFACE_PROXY_CLASS)) {
            Class<?>[] interfaces = proxyClass.getInterfaces();
            return interfaces.length > 0 ? getRealClass(interfaces[0]) : proxyClass;
        } else {
            return proxyClass;
        }
    }

    /**
     * 执行方法
     *
     * @param method 方法
     * @param obj    执行方法的对象
     * @param params 方法参数
     */
    public static Object invoke(Method method, Object obj, Object... params) throws InvocationTargetException, IllegalAccessException {
        if (!method.isAccessible()) {
            method.setAccessible(true);
        }
        return method.invoke(obj, params);
    }

    /**
     * 执行对象 obj 的方法
     *
     * @param obj        对象
     * @param methodName 方法名
     * @param params     参数
     * @return 执行结果
     */
    public static Object invoke(Object obj, String methodName, Object... params) throws InvocationTargetException, IllegalAccessException {
        Method method = null;
        for (Method m : getDeclareMethodsByName(obj.getClass(), methodName)) {
            if (m.getParameterTypes().length == params.length) {
                boolean b = true;
                for (int i = 0; i < params.length; i++) {
                    Class<?> cl = m.getParameterTypes()[i];
                    if ("$this.method".equals(params[i]) && cl == Method.class) {
                        continue;
                    }
                    if (params[i] != null && !cl.isAssignableFrom(params[i].getClass())) {
                        b = false;
                        break;
                    }
                }
                if (b) {
                    method = m;
                    break;
                }
            }
        }
        if (method == null) {
            log.error("invoke method is not exists: {}#{}, p:{}", obj.getClass(), methodName, Json.toJson(params));
            return null;
        }
        for (int i = 0; i < params.length; i++) {
            if ("$this.method".equals(params[i])) {
                params[i] = method;
            }
        }
        return invoke(method, obj, params);
    }

    /**
     * 根据类型、注解获取属性
     *
     * @param cla       类
     * @param typeClass 属性的类型
     * @param annoClass 注解
     */
    public static Field findFieldByTypeAndAnno(Class<?> cla, Class<?> typeClass, Class<? extends Annotation> annoClass) {
        for (Field f : getAllFields(cla)) {
            if (f.getType() == typeClass && f.getAnnotation(annoClass) != null) {
                return f;
            }
        }
        return null;
    }

    /**
     * 根据类型、注解获取属性
     *
     * @param cla       类
     * @param typeClass 属性的类型
     * @param annoClass 注解
     */
    public static List<Field> findFieldsByTypeAndAnno(Class<?> cla, Class<?> typeClass, Class<? extends Annotation> annoClass) {
        List<Field> fields = new ArrayList<Field>();
        for (Field f : getAllFields(cla)) {
            if (f.getType() == typeClass && f.getAnnotation(annoClass) != null) {
                fields.add(f);
            }
        }
        return fields;
    }

    /**
     * 判断方法是否是void
     *
     * @param m 方法
     */
    public static boolean isVoid(Method m) {
        return m.getReturnType().equals(Void.TYPE);
    }

    /**
     * 是否静态方法
     *
     * @param m 方法
     */
    public static Boolean isStatic(Method m) {
        return Modifier.isStatic(m.getModifiers());
    }

    /**
     * 是否静态方法
     *
     * @param m 方法
     */
    public static Boolean isPrivate(Method m) {
        return Modifier.isPrivate(m.getModifiers());
    }

    /**
     * 打印方法
     *
     * @param method 方法
     * @return 类型字符串
     */
    public static String methodStr(Method method) {
        if (method == null) {
            return null;
        }
        Class<?> returnType = method.getReturnType();
        Class<?>[] parameterTypes = method.getParameterTypes();
        StringBuilder builder = new StringBuilder();
        builder.append(returnType.getSimpleName()).append(" ").append(method.getName()).append("( ");
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i != 0) {
                builder.append(", ");
            }
            builder.append(parameterTypes[i].getSimpleName());
        }
        builder.append(" )");
        return builder.toString();
    }
}
