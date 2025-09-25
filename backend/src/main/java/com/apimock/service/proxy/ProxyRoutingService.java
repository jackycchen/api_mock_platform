package com.apimock.service.proxy;

import com.apimock.entity.proxy.ProxyConfig;
import com.apimock.entity.proxy.ProxyMode;
import com.apimock.entity.mock.MockCallLog;
import com.apimock.service.api.ApiService;
import com.apimock.service.mock.MockService;
import com.apimock.service.statistics.AccessStatisticsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 代理路由服务
 * 负责处理代理请求的路由和转发
 */
@Service
public class ProxyRoutingService {

    private static final Logger logger = LoggerFactory.getLogger(ProxyRoutingService.class);

    @Autowired
    private MockService mockService;

    @Autowired
    private ApiService apiService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProxyLogService proxyLogService;

    /**
     * 处理代理请求
     */
    public boolean handleProxyRequest(HttpServletRequest request,
                                    HttpServletResponse response,
                                    ProxyConfig config) {
        long startTime = System.currentTimeMillis();

        try {
            logger.info("Handling proxy request: {} {} with config: {}",
                request.getMethod(), request.getRequestURI(), config.getName());

            ProxyRequestContext context = buildRequestContext(request, config);

            ProxyResponse proxyResponse = null;

            switch (config.getMode()) {
                case MOCK:
                    proxyResponse = handleMockMode(context);
                    break;
                case PROXY:
                    proxyResponse = handleProxyMode(context);
                    break;
                case AUTO:
                    proxyResponse = handleAutoMode(context);
                    break;
                default:
                    logger.warn("Unknown proxy mode: {}", config.getMode());
                    return false;
            }

            if (proxyResponse != null) {
                writeResponse(response, proxyResponse);

                // 记录代理日志
                long responseTime = System.currentTimeMillis() - startTime;
                proxyLogService.logProxyRequest(context, proxyResponse, responseTime);

                return true;
            }

        } catch (Exception e) {
            logger.error("Error handling proxy request: {} {}",
                request.getMethod(), request.getRequestURI(), e);

            try {
                writeErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "Proxy error: " + e.getMessage());
                return true;
            } catch (IOException ioException) {
                logger.error("Failed to write error response", ioException);
            }
        }

        return false;
    }

    /**
     * 处理Mock模式
     */
    private ProxyResponse handleMockMode(ProxyRequestContext context) {
        try {
            // 查找匹配的API定义
            var apiOpt = apiService.findByProjectIdAndPath(
                context.getConfig().getProject().getId(),
                context.getPath(),
                context.getMethod()
            );

            if (apiOpt.isEmpty()) {
                logger.warn("No API definition found for: {} {}", context.getMethod(), context.getPath());
                return new ProxyResponse(HttpStatus.NOT_FOUND, new HttpHeaders(), "API not found", "MOCK");
            }

            var api = apiOpt.get();

            // 生成Mock数据
            String mockResponse = mockService.generateMockResponse(api);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("X-Mock-Mode", "MOCK");

            return new ProxyResponse(HttpStatus.OK, headers, mockResponse, "MOCK");

        } catch (Exception e) {
            logger.error("Error in mock mode", e);
            return new ProxyResponse(HttpStatus.INTERNAL_SERVER_ERROR, new HttpHeaders(),
                "Mock generation error: " + e.getMessage(), "MOCK");
        }
    }

    /**
     * 处理Proxy模式
     */
    private ProxyResponse handleProxyMode(ProxyRequestContext context) {
        try {
            String targetUrl = buildTargetUrl(context);

            HttpHeaders headers = buildForwardHeaders(context);
            HttpEntity<String> entity = new HttpEntity<>(context.getRequestBody(), headers);

            logger.info("Forwarding request to: {}", targetUrl);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                targetUrl,
                HttpMethod.valueOf(context.getMethod()),
                entity,
                String.class
            );

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.putAll(responseEntity.getHeaders());
            responseHeaders.add("X-Mock-Mode", "PROXY");

            return new ProxyResponse(
                responseEntity.getStatusCode(),
                responseHeaders,
                responseEntity.getBody(),
                "PROXY"
            );

        } catch (Exception e) {
            logger.error("Error in proxy mode", e);
            return new ProxyResponse(HttpStatus.BAD_GATEWAY, new HttpHeaders(),
                "Proxy forward error: " + e.getMessage(), "PROXY");
        }
    }

    /**
     * 处理Auto模式
     */
    private ProxyResponse handleAutoMode(ProxyRequestContext context) {
        // 首先尝试Mock模式
        try {
            var apiOpt = apiService.findByProjectIdAndPath(
                context.getConfig().getProject().getId(),
                context.getPath(),
                context.getMethod()
            );

            if (apiOpt.isPresent()) {
                logger.info("Auto mode: Using Mock for {}", context.getPath());
                return handleMockMode(context);
            }
        } catch (Exception e) {
            logger.warn("Auto mode: Mock failed, falling back to proxy", e);
        }

        // Mock失败或无API定义，尝试代理模式
        if (context.getConfig().getTargetUrl() != null) {
            logger.info("Auto mode: Using Proxy for {}", context.getPath());
            return handleProxyMode(context);
        }

        // 都失败了
        return new ProxyResponse(HttpStatus.NOT_FOUND, new HttpHeaders(),
            "No mock or proxy target available", "AUTO");
    }

    /**
     * 构建请求上下文
     */
    private ProxyRequestContext buildRequestContext(HttpServletRequest request, ProxyConfig config) {
        ProxyRequestContext context = new ProxyRequestContext();
        context.setConfig(config);
        context.setMethod(request.getMethod());
        context.setPath(request.getRequestURI());
        context.setQueryString(request.getQueryString());

        // 提取请求头
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                headers.put(headerName, request.getHeader(headerName));
            }
        }
        context.setHeaders(headers);

        // 提取请求体
        try {
            InputStream inputStream = request.getInputStream();
            String requestBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            context.setRequestBody(requestBody);
        } catch (IOException e) {
            logger.warn("Failed to read request body", e);
            context.setRequestBody("");
        }

        context.setClientIp(getClientIpAddress(request));

        return context;
    }

    /**
     * 构建目标URL
     */
    private String buildTargetUrl(ProxyRequestContext context) {
        String baseUrl = context.getConfig().getTargetUrl();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        String path = context.getPath();
        String queryString = context.getQueryString();

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(baseUrl).append(path);

        if (queryString != null && !queryString.isEmpty()) {
            urlBuilder.append("?").append(queryString);
        }

        return urlBuilder.toString();
    }

    /**
     * 构建转发请求头
     */
    private HttpHeaders buildForwardHeaders(ProxyRequestContext context) {
        HttpHeaders headers = new HttpHeaders();

        // 添加原始请求头
        Map<String, String> originalHeaders = context.getHeaders();
        for (Map.Entry<String, String> entry : originalHeaders.entrySet()) {
            String headerName = entry.getKey();
            String headerValue = entry.getValue();

            // 跳过某些不应该转发的请求头
            if (shouldSkipHeader(headerName)) {
                continue;
            }

            headers.add(headerName, headerValue);
        }

        // 处理Host头
        if (!context.getConfig().getPreserveHost()) {
            headers.remove("Host");
        }

        // 添加代理标识
        headers.add("X-Forwarded-For", context.getClientIp());
        headers.add("X-Forwarded-Proto", "http");

        return headers;
    }

    /**
     * 判断是否应该跳过某个请求头
     */
    private boolean shouldSkipHeader(String headerName) {
        String lowerName = headerName.toLowerCase();
        return lowerName.equals("content-length") ||
               lowerName.equals("connection") ||
               lowerName.equals("upgrade") ||
               lowerName.equals("proxy-connection") ||
               lowerName.equals("proxy-authorization");
    }

    /**
     * 写入响应
     */
    private void writeResponse(HttpServletResponse response, ProxyResponse proxyResponse) throws IOException {
        response.setStatus(proxyResponse.getStatus().value());

        // 设置响应头
        HttpHeaders headers = proxyResponse.getHeaders();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String headerName = entry.getKey();
            for (String headerValue : entry.getValue()) {
                response.addHeader(headerName, headerValue);
            }
        }

        // 写入响应体
        if (proxyResponse.getBody() != null) {
            response.getWriter().write(proxyResponse.getBody());
        }
    }

    /**
     * 写入错误响应
     */
    private void writeErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", status.value());
        errorResponse.put("message", message);
        errorResponse.put("timestamp", LocalDateTime.now());

        String json = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(json);
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}