package com.me.swagger.dubbo.extend;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.google.common.collect.Sets;
import org.apache.dubbo.config.ServiceConfigBase;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.NameValueExpression;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import springfox.documentation.RequestHandler;
import springfox.documentation.RequestHandlerKey;
import springfox.documentation.service.ResolvedMethodParameter;
import springfox.documentation.spi.service.RequestHandlerProvider;
import springfox.documentation.spring.web.plugins.CombinedRequestHandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

import static com.me.swagger.dubbo.Constants.DUBBO_TEST_ROOT_PATH_SPEL;

/**
 * dubbo RequestHandler 提供者
 *
 * @author zenglingjun
 * @since 2020/12/05
 */
public class DubboRequestHandlerProvider implements RequestHandlerProvider {

    private TypeResolver typeResolver;

    @Value(DUBBO_TEST_ROOT_PATH_SPEL)
    private String testRootPath;

    private Map<String, Class> handlerRequestTypeMap = new HashMap<>();

    public Class getRequestTypeByPath(String path) {
        return handlerRequestTypeMap.get(path);
    }

    public DubboRequestHandlerProvider(TypeResolver typeResolver) {
        this.typeResolver = typeResolver;
    }

    @Override
    public List<RequestHandler> requestHandlers() {
        Collection<ServiceConfigBase> services = ApplicationModel.getConfigManager().getServices();
        List<RequestHandler> requestHandlerList = new ArrayList<>();
        services.forEach(service -> {
            ReflectionUtils.doWithMethods(service.getInterfaceClass(), method -> {
                requestHandlerList.add(new DubboRequestHandler(service, method));
            });
        });
        return requestHandlerList;

    }

    /**
     * dubbo服务RequestHandler对象
     */
    class DubboRequestHandler implements RequestHandler {

        private ServiceConfigBase serviceConfigBase;

        private Method method;

        public DubboRequestHandler(ServiceConfigBase serviceConfigBase, Method method) {
            this.serviceConfigBase = serviceConfigBase;
            this.method = method;
        }

        @Override
        public Class<?> declaringClass() {
            return serviceConfigBase.getInterfaceClass();
        }

        @Override
        public boolean isAnnotatedWith(Class<? extends Annotation> annotation) {
            return serviceConfigBase.getInterfaceClass().isAnnotationPresent(annotation);
        }

        @Override
        public PatternsRequestCondition getPatternsCondition() {
            return new PatternsRequestCondition(getPath());
        }

        @Override
        public String groupName() {
            return serviceConfigBase.getId();
        }

        @Override
        public String getName() {
            return serviceConfigBase.getId();
        }

        @Override
        public Set<RequestMethod> supportedMethods() {
            return Sets.newHashSet(RequestMethod.POST);
        }

        @Override
        public Set<? extends MediaType> produces() {
            return new HashSet<>(Arrays.asList(MediaType.APPLICATION_JSON));
        }

        @Override
        public Set<? extends MediaType> consumes() {
            return new HashSet<>(Arrays.asList(MediaType.APPLICATION_JSON));
        }

        @Override
        public Set<NameValueExpression<String>> headers() {
            return Collections.emptySet();
        }

        @Override
        public Set<NameValueExpression<String>> params() {
            return Collections.emptySet();
        }

        @Override
        public <T extends Annotation> com.google.common.base.Optional<T> findAnnotation(Class<T> annotation) {
            return com.google.common.base.Optional.fromNullable(method.getAnnotation(annotation));
        }

        @Override
        public RequestHandlerKey key() {
            return new RequestHandlerKey(
                    Sets.newHashSet(getPath()),
                    supportedMethods(),
                    consumes(),
                    produces());
        }

        private String getPath() {
            return testRootPath + "/" + serviceConfigBase.getId() + "/" + method.getName();
        }

        @Override
        public List<ResolvedMethodParameter> getParameters() {
            //无参数返回空
            if (method.getParameters().length == 0) {
                return Collections.emptyList();
            }
            List<ResolvedMethodParameter> resolvedMethodParameterList = new ArrayList<>();
            ResolvedType paramType;
            //参数大于1或者第一个参数为基础类型 需要生成请求包装对象
            if (method.getParameters().length != 1
                    || method.getParameterTypes()[0].isPrimitive()) {
                paramType = typeResolver.resolve(LinkedHashMap.class, String.class, Object.class);

            } else {
                paramType = typeResolver.resolve(method.getParameterTypes()[0]);

            }
            handlerRequestTypeMap.put(getPath(), paramType.getErasedType());
            resolvedMethodParameterList.add(
                    new ResolvedMethodParameter(
                            0,
                            "param",
                            Arrays.asList(AnnotationHold.getParameterAnnotations(new Object())),
                            paramType
                    ));
            return resolvedMethodParameterList;
        }

        @Override
        public ResolvedType getReturnType() {
            return typeResolver.resolve(method.getReturnType());
        }

        @Override
        public <T extends Annotation> com.google.common.base.Optional<T> findControllerAnnotation(Class<T> annotation) {
            return (com.google.common.base.Optional<T>) com.google.common.base.Optional.fromNullable(serviceConfigBase.getInterfaceClass().getAnnotation(annotation));
        }

        @Override
        public RequestMappingInfo getRequestMapping() {
            return null;
        }

        @Override
        public HandlerMethod getHandlerMethod() {
            return null;
        }

        @Override
        public RequestHandler combine(RequestHandler other) {
            return new CombinedRequestHandler(this, other);
        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("DubboRequestHandler{");
            sb.append("key=").append(key());
            sb.append('}');
            return sb.toString();
        }
    }

    static class AnnotationHold {

        private Annotation[] parametersAnnotations;

        private static AnnotationHold instance = new AnnotationHold();

        public static Annotation[] getParameterAnnotations(@RequestBody Object p) {
            if (instance.parametersAnnotations == null) {
                try {
                    Method method = AnnotationHold.class.getMethod("getParameterAnnotations", new Class[]{Object.class});
                    instance.parametersAnnotations = method.getParameters()[0].getAnnotations();
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException("获取AnnotationHold.getParameterAnnotations method对象失败", e);
                }
            }
            return instance.parametersAnnotations;
        }
    }

}
