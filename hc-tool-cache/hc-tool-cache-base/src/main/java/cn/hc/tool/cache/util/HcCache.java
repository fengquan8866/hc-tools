//package cn.hc.tool.cache.util;
//
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.cache.Cache;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.concurrent.Callable;
//
///**
// * @author huangchao E-mail:fengquan8866@163.com
// * @version 创建时间：2024/9/21 10:50
// */
//@Configuration
//@Slf4j
//@Data
//public class HcCache implements Cache {
//    private String name;
//
//    public HcCache(String name) {
//        this.name = name;
//    }
//
//    @Override
//    public Object getNativeCache() {
//        return null;
//    }
//
//    @Override
//    public ValueWrapper get(Object key) {
//        ValueWrapper result = null;
//        Object thevalue = store.get(key);
//        if(thevalue!=null) {
//            logger.info("["+name+"]got cache, key:"+key);
//            result = new SimpleValueWrapper(thevalue);
//        }else{
//            logger.info("["+name+"]missing cache, key:"+key);
//        }
//        return result;
//    }
//
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public <T> T get(Object key, Class<T> type) {
//        ValueWrapper vw = get(key);
//        if(vw==null){
//            return null;
//        }
//        return (T)vw.get();
//    }
//
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public <T> T get(Object key, Callable<T> valueLoader) {
//        ValueWrapper vw = get(key);
//        if(vw==null){
//            return null;
//        }
//        return (T)vw.get();
//    }
//
//
//    @Override
//    public void put(Object key, Object value) {
//        store.put(key, value);
//    }
//
//
//    @Override
//    public Cache.ValueWrapper putIfAbsent(Object key, Object value) {
//        Object existing = this.store.putIfAbsent(key, value);
//        return (existing != null ? new SimpleValueWrapper(existing) : null);
//    }
//
//
//    @Override
//    public void evict(Object key) {
//        store.remove(key);
//    }
//
//
//    @Override
//    public void clear() {
//        store.clear();
//    }
//}
