package com.apimock.service.auth;

import com.apimock.entity.auth.UserOperationLog;
import com.apimock.repository.auth.UserOperationLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户操作日志服务
 *
 * @author AI Assistant
 */
@Service
@Transactional
public class UserOperationLogService {

    @Autowired
    private UserOperationLogRepository operationLogRepository;

    /**
     * 异步记录用户操作
     */
    @Async
    public void logOperation(Long userId, String username, String operation, String description) {
        UserOperationLog.Builder builder = new UserOperationLog.Builder(userId, operation)
                .username(username)
                .description(description);

        // 尝试获取HTTP请求信息
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                builder.ipAddress(getClientIpAddress(request))
                       .userAgent(request.getHeader("User-Agent"));
            }
        } catch (Exception e) {
            // 忽略获取请求信息的异常
        }

        UserOperationLog log = builder.build();
        operationLogRepository.save(log);
    }

    /**
     * 记录资源相关操作
     */
    @Async
    public void logResourceOperation(Long userId, String username, String operation,
                                   String resource, Long resourceId, String description) {
        UserOperationLog.Builder builder = new UserOperationLog.Builder(userId, operation)
                .username(username)
                .description(description)
                .resource(resource, resourceId);

        // 尝试获取HTTP请求信息
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                builder.ipAddress(getClientIpAddress(request))
                       .userAgent(request.getHeader("User-Agent"));
            }
        } catch (Exception e) {
            // 忽略获取请求信息的异常
        }

        UserOperationLog log = builder.build();
        operationLogRepository.save(log);
    }

    /**
     * 记录操作失败
     */
    @Async
    public void logFailedOperation(Long userId, String username, String operation,
                                 String description, String errorMessage) {
        UserOperationLog.Builder builder = new UserOperationLog.Builder(userId, operation)
                .username(username)
                .description(description)
                .result("FAILED")
                .details(errorMessage);

        // 尝试获取HTTP请求信息
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                builder.ipAddress(getClientIpAddress(request))
                       .userAgent(request.getHeader("User-Agent"));
            }
        } catch (Exception e) {
            // 忽略获取请求信息的异常
        }

        UserOperationLog log = builder.build();
        operationLogRepository.save(log);
    }

    /**
     * 获取用户操作日志
     */
    public Page<UserOperationLog> getUserOperationLogs(Long userId, Pageable pageable) {
        return operationLogRepository.findByUserId(userId, pageable);
    }

    /**
     * 获取资源相关的操作日志
     */
    public Page<UserOperationLog> getResourceOperationLogs(String resource, Long resourceId, Pageable pageable) {
        return operationLogRepository.findByResourceAndResourceId(resource, resourceId, pageable);
    }

    /**
     * 根据时间范围获取操作日志
     */
    public Page<UserOperationLog> getOperationLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return operationLogRepository.findByCreatedAtBetween(startTime, endTime, pageable);
    }

    /**
     * 根据操作类型获取日志
     */
    public Page<UserOperationLog> getOperationLogsByType(String operation, Pageable pageable) {
        return operationLogRepository.findByOperation(operation, pageable);
    }

    /**
     * 获取用户在指定时间范围内的操作日志
     */
    public Page<UserOperationLog> getUserOperationLogsByTimeRange(Long userId, LocalDateTime startTime,
                                                                 LocalDateTime endTime, Pageable pageable) {
        return operationLogRepository.findByUserIdAndCreatedAtBetween(userId, startTime, endTime, pageable);
    }

    /**
     * 统计用户在指定时间后的操作次数
     */
    public long countUserOperations(Long userId, LocalDateTime since) {
        return operationLogRepository.countByUserIdAndCreatedAtAfter(userId, since);
    }

    /**
     * 统计操作类型的次数
     */
    public List<Object[]> countOperationsByType(LocalDateTime since) {
        return operationLogRepository.countByOperationAndCreatedAtAfter(since);
    }

    /**
     * 清理指定时间之前的日志
     */
    public void cleanupLogs(LocalDateTime before) {
        operationLogRepository.deleteByCreatedAtBefore(before);
    }

    /**
     * 常用操作类型常量
     */
    public static class Operations {
        public static final String LOGIN = "LOGIN";
        public static final String LOGOUT = "LOGOUT";
        public static final String CREATE_PROJECT = "CREATE_PROJECT";
        public static final String UPDATE_PROJECT = "UPDATE_PROJECT";
        public static final String DELETE_PROJECT = "DELETE_PROJECT";
        public static final String INVITE_MEMBER = "INVITE_MEMBER";
        public static final String REMOVE_MEMBER = "REMOVE_MEMBER";
        public static final String CHANGE_ROLE = "CHANGE_ROLE";
        public static final String CREATE_API = "CREATE_API";
        public static final String UPDATE_API = "UPDATE_API";
        public static final String DELETE_API = "DELETE_API";
        public static final String CREATE_MOCK = "CREATE_MOCK";
        public static final String UPDATE_MOCK = "UPDATE_MOCK";
        public static final String DELETE_MOCK = "DELETE_MOCK";
        public static final String EXPORT_DOC = "EXPORT_DOC";
        public static final String PERMISSION_DENIED = "PERMISSION_DENIED";
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] ipHeaders = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
        };

        for (String header : ipHeaders) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // 处理多个IP的情况，取第一个
                if (ip.contains(",")) {
                    ip = ip.split(",")[0];
                }
                return ip.trim();
            }
        }

        return request.getRemoteAddr();
    }
}