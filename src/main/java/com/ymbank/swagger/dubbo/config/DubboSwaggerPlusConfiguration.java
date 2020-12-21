package com.ymbank.swagger.dubbo.config;

import com.fasterxml.classmate.TypeResolver;
import com.ymbank.swagger.dubbo.EnableDubboSwaggerPlugin;
import com.ymbank.swagger.dubbo.extend.DubboRequestHandlerProvider;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Map;

/**
 * {desc}
 *
 * @author zenglingjun
 * @since 2020/12/07
 */
@Configuration
public class DubboSwaggerPlusConfiguration implements ImportBeanDefinitionRegistrar {

    private String apiScanPackage;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(EnableDubboSwaggerPlugin.class.getName());
        this.apiScanPackage = (String) attributes.get("apiScanPackage");
        AbstractBeanDefinition beanDefinition =
                BeanDefinitionBuilder.rootBeanDefinition(SwaggerDubboConfigurationBean.class)
                .addConstructorArgValue(apiScanPackage).getBeanDefinition();
        registry.registerBeanDefinition("SwaggerDubboConfigurationBean", beanDefinition);
    }

    @Configuration
    @ComponentScan(basePackages = {
            "com.ymbank.swagger.dubbo.web"
    })
    static class SwaggerDubboConfigurationBean {

        private String apiScanPackage;

        public SwaggerDubboConfigurationBean(String apiScanPackage) {
            this.apiScanPackage = apiScanPackage;
        }

        @Bean
        public DubboRequestHandlerProvider dubboRequestHandlerProvider(TypeResolver typeResolver) {
            return new DubboRequestHandlerProvider(typeResolver);
        }

        @Bean
        public Docket createDubboApi() {
            return new Docket(DocumentationType.SWAGGER_2)
                    .apiInfo(apiInfo())
                    .groupName("dubbo")
                    .select()
                    //为当前包路径
                    .apis(RequestHandlerSelectors.basePackage(this.apiScanPackage))
                    .paths(PathSelectors.any())
                    .build();
        }

        /**
         * 构建 api文档的详细信息函数,注意这里的注解引用的是哪个
         */
        private ApiInfo apiInfo() {
            return new ApiInfoBuilder()
                    //页面标题
                    .title("Dubbo-API说明文档")
                    //创建人
                    .contact(new Contact("ymbank", "https://www.ymbank.com/", "architecture@ymbank.com"))
                    //版本号
                    .version("1.0")
                    .build();
        }
    }

}
