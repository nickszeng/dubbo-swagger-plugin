# dubbo-swagger-plugin
## 背景
最新比较热门的spring-cloud-alibaba微服务解决方案，其中dubbo做为rpc服务，nacos为推荐的注册中心，而从官方的dubbo-admin目前[2020年12月21日]看还是没法支持spring-cloud方式注册到nacos的接口调试，
而用上了此微服务方案的朋友如果碰到dubbo接口不好调试的情况可以考虑下本插件方式，或者想通过dubbo接口统一规范化到Ypi的朋友也可以考虑。

## 特性
* 无侵入扩展式支持dubbo转swagger调试插件
* 支持swagger-ui测试dubbo接口
* 支持同步到YAPI开源接口测试平台
## 集成原理
首先简单描述下swagger扫描接口原理

**RequestHandler**
`RequestHandler`为一个请求接口处理抽象

**RequestHandlerProvider**
`RequestHandlerProvider`为`RequestHandler`的提供器，获取`RequestHandler`列表的组件
* springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider为webmvc的RequestHandler提供者

**总结**
`swagger`是通过`RequestHandlerProvider`来获取请求接口的抽象对象`RequestHandler`列表,然后进一步解析为接口文档。

**本插件集成原理**

1.扩展swagger接口提供者
> 扩展一个`DubboRequestHandlerProvider`将dubbo服务提供者当前已有的service转换为swagger能解析的请求接口对象`DubboRequestHandler`供swagger解析为接口文档

2.定义一个全局rest测试入口
> 定义一个全局的测试入口`DubboTestController`,将dubbo接口的传参全转化为map对象，
> 通过dubbo的泛化调用方式调用实际的dubbo服务

## 使用方式
### 依赖
**本地打包到maven私服，并项目中添加如下依赖**
```xml
    <dependency>
        <groupId>com.github</groupId>
        <artifactId>dubbo-swagger-plugin</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
```

**dubbo接口上添加swagger注解**
```java
@Api("dubbo演示服务")
public interface IDemoService {

    @ApiOperation("获取用户名称")
    Result<String> getUserNameById(Long userId);

    @ApiOperation("校验示例")
    Result<String> validateDemo(DemoDTO demoDTO);

}
```

**提供者服务添加插件扫描器**
```java
@EnableSwagger2
@SpringBootApplication
@DubboComponentScan("com.samples.dubbo.provider.service")
@EnableDubboSwaggerPlugin(apiScanPackage = "com.samples.dubbo.api") //添加开启swagger-dubbo插件 扫描的路径为dubbo接口路径
public class DubboProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(DubboProviderApplication.class, args);
    }
}
```
### 启动服务调试
#### 界面调试
访问 http://localhost:8081/swagger-ui.html

#### 获取swagger-json

可同步到YAPI等其他开源接口平台
访问 http://localhost:8081/v2/api-docs?group=dubbo
案例json如下：
```json
{
    "swagger":"2.0",
    "info":{
        "version":"1.0",
        "title":"Dubbo-API说明文档",
        "contact":{
            "name":"xxx",
            "url":"https://www.xxx.com/",
            "email":""
        }
    },
    "host":"localhost:8081",
    "basePath":"/",
    "tags":[
        {
            "name":"com.samples.dubbo.api.IDemoService",
            "description":"I Demo Service"
        }
    ],
    "paths":{
        "/dubbo/test/com.samples.dubbo.api.IDemoService/getUserNameById":{
            "post":{
                "tags":[
                    "com.samples.dubbo.api.IDemoService"
                ],
                "summary":"获取用户名称",
                "operationId":"com.samples.dubbo.api.IDemoServiceUsingPOST",
                "consumes":[
                    "application/json"
                ],
                "produces":[
                    "application/json"
                ],
                "parameters":[
                    {
                        "in":"body",
                        "name":"param",
                        "description":"param",
                        "required":true,
                        "schema":{
                            "type":"integer",
                            "format":"int64"
                        }
                    }
                ],
                "responses":{
                    "200":{
                        "description":"OK",
                        "schema":{
                            "$ref":"#/definitions/Result"
                        }
                    },
                    "201":{
                        "description":"Created"
                    },
                    "401":{
                        "description":"Unauthorized"
                    },
                    "403":{
                        "description":"Forbidden"
                    },
                    "404":{
                        "description":"Not Found"
                    }
                },
                "deprecated":false
            }
        },
        "/dubbo/test/com.samples.dubbo.api.IDemoService/validateDemo":{
            "post":{
                "tags":[
                    "com.samples.dubbo.api.IDemoService"
                ],
                "summary":"校验示例",
                "operationId":"com.samples.dubbo.api.IDemoServiceUsingPOST_1",
                "consumes":[
                    "application/json"
                ],
                "produces":[
                    "application/json"
                ],
                "parameters":[
                    {
                        "in":"body",
                        "name":"param",
                        "description":"param",
                        "required":true,
                        "schema":{
                            "$ref":"#/definitions/DemoDTO"
                        }
                    }
                ],
                "responses":{
                    "200":{
                        "description":"OK",
                        "schema":{
                            "$ref":"#/definitions/Result"
                        }
                    },
                    "201":{
                        "description":"Created"
                    },
                    "401":{
                        "description":"Unauthorized"
                    },
                    "403":{
                        "description":"Forbidden"
                    },
                    "404":{
                        "description":"Not Found"
                    }
                },
                "deprecated":false
            }
        }
    },
    "definitions":{
        "DemoDTO":{
            "type":"object",
            "properties":{
                "id":{
                    "type":"integer",
                    "format":"int64"
                },
                "name":{
                    "type":"string"
                }
            },
            "title":"DemoDTO"
        },
        "Result":{
            "type":"object",
            "properties":{
                "code":{
                    "type":"string"
                },
                "data":{
                    "type":"object"
                },
                "message":{
                    "type":"string"
                }
            },
            "title":"Result"
        }
    }
}
```
