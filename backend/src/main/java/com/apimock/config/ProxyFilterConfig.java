package com.apimock.config;

import com.apimock.interceptor.ApiProxyInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * 代理拦截器配置
 */
@Configuration
public class ProxyFilterConfig {

    @Autowired
    private ApiProxyInterceptor apiProxyInterceptor;

    /**
     * 注册API代理拦截器
     */
    @Bean
    public FilterRegistrationBean<ApiProxyInterceptor> proxyFilterRegistration() {
        FilterRegistrationBean<ApiProxyInterceptor> registration = new FilterRegistrationBean<>();

        registration.setFilter(apiProxyInterceptor);

        // 拦截路径 - 拦截所有请求，在Filter内部进行判断
        registration.addUrlPatterns("/*");

        // 设置优先级 - 需要在其他Filter之前执行
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);

        // Filter名称
        registration.setName("ApiProxyInterceptor");

        return registration;
    }
}