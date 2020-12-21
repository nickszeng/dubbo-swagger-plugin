package com.ymbank.swagger.dubbo;

/**
 * @author zenglingjun
 * @since 2020/12/07
 */
public interface Constants {

    /**
     * dubbo测试根路径 SPEL表达式
     */
    String DUBBO_TEST_ROOT_PATH_SPEL = "${custom.swagger.dubbo.test.root-path:/dubbo/test}";
}
