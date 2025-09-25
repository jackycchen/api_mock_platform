package com.apimock.interceptor;

import com.apimock.entity.proxy.ProxyConfig;
import com.apimock.entity.proxy.ProxyMode;
import com.apimock.service.proxy.ProxyRoutingService;
import com.apimock.service.proxy.ProxyConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * API代理拦截器
 * 拦截所有API请求，根据代理配置决定是否转发
 */
@Component
@Order(1)
public class ApiProxyInterceptor implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(ApiProxyInterceptor.class);

    @Autowired
    private ProxyConfigService proxyConfigService;

    @Autowired
    private ProxyRoutingService proxyRoutingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestPath = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        logger.debug("Intercepting request: {} {}", method, requestPath);

        // 跳过非API请求
        if (!shouldIntercept(requestPath)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            // 查找匹配的代理配置
            ProxyConfig matchedConfig = findMatchingProxyConfig(requestPath);

            if (matchedConfig == null || !matchedConfig.getEnabled()) {
                // 没有匹配的代理配置，继续正常处理
                chain.doFilter(request, response);
                return;
            }

            logger.info("Found matching proxy config: {} for path: {}",
                matchedConfig.getName(), requestPath);

            // 执行代理转发
            boolean handled = proxyRoutingService.handleProxyRequest(
                httpRequest, httpResponse, matchedConfig);

            if (!handled) {
                // 代理处理失败，继续正常处理
                chain.doFilter(request, response);
            }

        } catch (Exception e) {
            logger.error("Error in proxy interceptor for path: {}", requestPath, e);
            // 出错时继续正常处理，避免影响系统稳定性
            chain.doFilter(request, response);
        }
    }

    /**
     * 判断是否需要拦截该请求
     */
    private boolean shouldIntercept(String requestPath) {
        // 只拦截API请求，跳过静态资源和系统接口
        if (requestPath.startsWith("/api/mock/") ||
            requestPath.startsWith("/mock/")) {
            return true;
        }

        // 跳过系统管理接口
        if (requestPath.startsWith("/api/v1/")) {
            return false;
        }

        // 跳过静态资源
        if (requestPath.startsWith("/static/") ||
            requestPath.startsWith("/css/") ||
            requestPath.startsWith("/js/") ||
            requestPath.startsWith("/images/") ||
            requestPath.contains(".")) {
            return false;
        }

        return false; // 默认不拦截
    }

    /**
     * 查找匹配的代理配置
     */
    private ProxyConfig findMatchingProxyConfig(String requestPath) {
        try {
            // 获取所有启用的代理配置
            List<ProxyConfig> enabledConfigs = proxyConfigService.getAllEnabledConfigs();

            // 按路径模式匹配，优先匹配更具体的路径
            return enabledConfigs.stream()
                .filter(config -> pathMatches(requestPath, config.getPathPattern()))
                .findFirst()
                .orElse(null);

        } catch (Exception e) {
            logger.error("Error finding matching proxy config for path: {}", requestPath, e);
            return null;
        }
    }

    /**
     * 路径模式匹配
     */
    private boolean pathMatches(String requestPath, String pathPattern) {
        if (pathPattern == null || requestPath == null) {
            return false;
        }

        // 精确匹配
        if (pathPattern.equals(requestPath)) {
            return true;
        }

        // 通配符匹配
        if (pathPattern.endsWith("/*")) {
            String prefix = pathPattern.substring(0, pathPattern.length() - 2);
            return requestPath.startsWith(prefix);
        }

        // 正则表达式匹配（可选扩展）
        if (pathPattern.startsWith("regex:")) {
            String regex = pathPattern.substring(6);
            return requestPath.matches(regex);
        }

        return false;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Initializing API Proxy Interceptor");
    }

    @Override
    public void destroy() {
        logger.info("Destroying API Proxy Interceptor");
    }
}