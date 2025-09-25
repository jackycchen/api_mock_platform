package com.apimock.service.proxy;

import com.apimock.entity.mock.MockCallLog;
import com.apimock.repository.mock.MockCallLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 代理日志服务
 * 负责记录代理请求的详细信息
 */
@Service
public class ProxyLogService {

    private static final Logger logger = LoggerFactory.getLogger(ProxyLogService.class);

    @Autowired
    private MockCallLogRepository mockCallLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 记录代理请求日志
     */
    public void logProxyRequest(ProxyRequestContext context, ProxyResponse response, long responseTime) {
        try {
            MockCallLog log = new MockCallLog();

            // 基础信息
            log.setRequestMethod(context.getMethod());
            log.setRequestPath(context.getPath());
            log.setResponseStatus(response.getStatus().value());
            log.setResponseTime((int) responseTime);
            log.setClientIp(context.getClientIp());
            log.setCreatedAt(LocalDateTime.now());

            // 请求信息
            log.setRequestHeaders(mapToJson(context.getHeaders()));
            log.setRequestBody(context.getRequestBody());
            log.setRequestParams(context.getQueryString());

            // 响应信息
            log.setResponseHeaders(headersToJson(response.getHeaders()));
            log.setResponseBody(response.getBody());

            // 代理信息 - 使用user_agent字段存储代理模式
            log.setUserAgent("Proxy-Mode: " + response.getMode() +
                           ", Config: " + context.getConfig().getName());

            // 设置关联ID（如果存在）
            // 这里可以尝试查找匹配的API ID，但不是必需的
            try {
                // 暂时设置为0，后续可以根据需要查找对应的API ID
                log.setApiId(0L);
                log.setMockRuleId(0L);
            } catch (Exception e) {
                logger.debug("Could not find API/Mock rule for proxy request: {}", context.getPath());
                log.setApiId(0L);
                log.setMockRuleId(0L);
            }

            // 保存日志
            mockCallLogRepository.save(log);

            logger.debug("Proxy request logged: {} {} -> {} ({}ms)",
                context.getMethod(), context.getPath(), response.getStatus(), responseTime);

        } catch (Exception e) {
            logger.error("Failed to log proxy request: {} {}", context.getMethod(), context.getPath(), e);
        }
    }

    /**
     * Map转JSON字符串
     */
    private String mapToJson(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to convert map to JSON", e);
            return map.toString();
        }
    }

    /**
     * HttpHeaders转JSON字符串
     */
    private String headersToJson(org.springframework.http.HttpHeaders headers) {
        if (headers == null || headers.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(headers.toSingleValueMap());
        } catch (JsonProcessingException e) {
            logger.warn("Failed to convert headers to JSON", e);
            return headers.toString();
        }
    }

    /**
     * 记录代理错误
     */
    public void logProxyError(ProxyRequestContext context, String errorMessage, long responseTime) {
        try {
            MockCallLog log = new MockCallLog();

            log.setRequestMethod(context.getMethod());
            log.setRequestPath(context.getPath());
            log.setResponseStatus(500); // Internal Server Error
            log.setResponseTime((int) responseTime);
            log.setClientIp(context.getClientIp());
            log.setCreatedAt(LocalDateTime.now());

            log.setRequestHeaders(mapToJson(context.getHeaders()));
            log.setRequestBody(context.getRequestBody());
            log.setRequestParams(context.getQueryString());

            log.setResponseBody("{\"error\": \"" + errorMessage + "\"}");
            log.setUserAgent("Proxy-Error: " + context.getConfig().getName());

            log.setApiId(0L);
            log.setMockRuleId(0L);

            mockCallLogRepository.save(log);

            logger.info("Proxy error logged: {} {} -> Error: {}",
                context.getMethod(), context.getPath(), errorMessage);

        } catch (Exception e) {
            logger.error("Failed to log proxy error: {} {}", context.getMethod(), context.getPath(), e);
        }
    }
}