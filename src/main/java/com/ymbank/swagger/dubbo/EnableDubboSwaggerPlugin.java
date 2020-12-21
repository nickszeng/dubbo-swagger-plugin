package com.ymbank.swagger.dubbo;

import com.ymbank.swagger.dubbo.config.DubboSwaggerPlusConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 开启swagger-dubbo插件
 *
 * @author zenglingjun
 * @since 2020/12/07
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Configuration
@Import({DubboSwaggerPlusConfiguration.class})
public @interface EnableDubboSwaggerPlugin {

    /**
     * dubbo api接口扫描路径
     */
    String apiScanPackage();
}
