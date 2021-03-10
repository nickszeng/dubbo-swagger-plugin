package com.me.swagger.dubbo.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.me.swagger.dubbo.extend.DubboRequestHandlerProvider;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.me.swagger.dubbo.Constants.DUBBO_TEST_ROOT_PATH_SPEL;

/**
 * dubbo rest测试接口
 *
 * @author zenglingjun
 * @since 2020/12/04
 */
@ApiIgnore
@RestController
public class DubboTestController {

    private DubboRequestHandlerProvider dubboRequestHandlerProvider;

    @Autowired
    private ObjectMapper objectMapper;

    public DubboTestController(DubboRequestHandlerProvider dubboRequestHandlerProvider) {
        this.dubboRequestHandlerProvider = dubboRequestHandlerProvider;
    }

    @PostMapping(value = DUBBO_TEST_ROOT_PATH_SPEL + "/{service}/{method}")
    public Object test(@PathVariable String service,
                       @PathVariable String method,
                       @RequestBody Map param, HttpServletRequest request) throws ClassNotFoundException {
        Class requestType = dubboRequestHandlerProvider.getRequestTypeByPath(request.getServletPath());
        if (requestType == null) {
            throw new ClassNotFoundException("未找到dubbo测试接口 [" + request.getServletPath() + "] 对应的请求参数类型");
        }
        Object realParam = objectMapper.convertValue(param, requestType);
        return invokeDubboService(service, method, new String[]{requestType.getName()}, new Object[]{realParam});
    }

    private Object invokeDubboService(String service, String method, String[] parameterTypes, Object[] args) {
        // 该实例很重量，里面封装了所有与注册中心及服务提供方连接，请缓存
        ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
        // 弱类型接口名
        reference.setInterface(service);
        // 声明为泛化接口
        reference.setGeneric("true");
        reference.setScope("local");
        // 用org.apache.dubbo.rpc.service.GenericService可以替代所有接口引用
        GenericService genericService = reference.get();
        // 基本类型以及Date,List,Map等不需要转换，直接调用
        return genericService.$invoke(method, parameterTypes, args);
    }

}
