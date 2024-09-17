package cn.hc.tool.cache.conf;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/16 17:35
 */
@Slf4j
public class HcRedisComponentScanner implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        log.info("scan HcRedisComponentScanner-----------------");
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry);
        String[] packages = new String[]{"cn.hc.tool.cache"};
        scanner.scan(packages);
    }

}
