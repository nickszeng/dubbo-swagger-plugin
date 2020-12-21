# dubbo-swagger-plugin
无侵入扩展式支持dubbo转swagger调试插件
## 使用方式


```java
@Api("dubbo演示服务")
public interface IDemoService {

    @ApiOperation("获取用户名称")
    Result<String> getUserNameById(Long userId);

    @ApiOperation("校验示例")
    Result<String> validateDemo(DemoDTO demoDTO);

}
```

提供者
```java
@EnableSwagger2
@SpringBootApplication
@DubboComponentScan("com.samples.dubbo.provider.service")
@EnableSwaggerDubboPlugin(apiScanPackage = "com.samples.dubbo.api") //添加开启swagger-dubbo插件 扫描的路径为dubbo接口路径
public class DubboProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(DubboProviderApplication.class, args);
    }
}
```
### 启动服务调试
#### 界面调试
访问 http://localhost:8081/swagger-ui.html

#### 获取swagger-json（可同步到YAPI等其他开源接口平台）
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
