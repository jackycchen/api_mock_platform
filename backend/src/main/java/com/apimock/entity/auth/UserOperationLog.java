package com.apimock.entity.auth;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 用户操作日志实体类
 * 记录用户的重要操作，特别是权限相关操作
 *
 * @author AI Assistant
 */
@Entity
@Table(name = "user_operation_logs")
public class UserOperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(length = 100)
    private String username;

    @Column(nullable = false, length = 50)
    private String operation;

    @Column(length = 255)
    private String description;

    @Column(length = 100)
    private String resource;

    @Column
    private Long resourceId;

    @Column(nullable = false, length = 50)
    private String result;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    @Column(columnDefinition = "TEXT")
    private String details;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 构造函数
    public UserOperationLog() {}

    public UserOperationLog(Long userId, String username, String operation, String description) {
        this.userId = userId;
        this.username = username;
        this.operation = operation;
        this.description = description;
        this.result = "SUCCESS";
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // 构建器模式
    public static class Builder {
        private UserOperationLog log;

        public Builder(Long userId, String operation) {
            log = new UserOperationLog();
            log.userId = userId;
            log.operation = operation;
            log.result = "SUCCESS";
        }

        public Builder username(String username) {
            log.username = username;
            return this;
        }

        public Builder description(String description) {
            log.description = description;
            return this;
        }

        public Builder resource(String resource, Long resourceId) {
            log.resource = resource;
            log.resourceId = resourceId;
            return this;
        }

        public Builder result(String result) {
            log.result = result;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            log.ipAddress = ipAddress;
            return this;
        }

        public Builder userAgent(String userAgent) {
            log.userAgent = userAgent;
            return this;
        }

        public Builder details(String details) {
            log.details = details;
            return this;
        }

        public UserOperationLog build() {
            return log;
        }
    }

    @Override
    public String toString() {
        return "UserOperationLog{" +
                "id=" + id +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", operation='" + operation + '\'' +
                ", resource='" + resource + '\'' +
                ", result='" + result + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}