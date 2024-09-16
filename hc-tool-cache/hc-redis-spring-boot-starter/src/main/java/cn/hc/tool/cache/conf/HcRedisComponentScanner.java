package cn.hc.tool.cache.conf;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author huangchao E-mail:fengquan8866@163.com
 * @version 创建时间：2024/9/16 17:35
 */
@Slf4j
@ComponentScan("cn.hc.tool.cache")
public class HcRedisComponentScanner implements InitializingBean, EnvironmentAware, ImportBeanDefinitionRegistrar {

    private Environment environment;

//    @Override
//    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
//        log.info("scan HcRedisComponentScanner-----------------");
//        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry);
//        String[] packages = new String[]{"cn.hc.tool.cache"};
//        scanner.scan(packages);
//    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("11111111111111111111111111111111111111111111");
    }
}
