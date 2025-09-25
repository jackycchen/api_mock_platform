package com.apimock.entity.mock;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mock_call_logs", indexes = {
    @Index(name = "idx_mock_rule_id", columnList = "mock_rule_id"),
    @Index(name = "idx_api_id", columnList = "api_id"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_response_status", columnList = "response_status")
})
public class MockCallLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mock_rule_id", nullable = false)
    private Long mockRuleId;

    @Column(name = "api_id", nullable = false)
    private Long apiId;

    @Column(name = "request_method", nullable = false, length = 10)
    private String requestMethod;

    @Column(name = "request_path", nullable = false, length = 500)
    private String requestPath;

    @Column(name = "request_headers", columnDefinition = "TEXT")
    private String requestHeaders;

    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody;

    @Column(name = "request_params", columnDefinition = "TEXT")
    private String requestParams;

    @Column(name = "response_status", nullable = false)
    private Integer responseStatus;

    @Column(name = "response_headers", columnDefinition = "TEXT")
    private String responseHeaders;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "response_time")
    private Integer responseTime;

    @Column(name = "client_ip", length = 45)
    private String clientIp;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 构造函数
    public MockCallLog() {
        this.createdAt = LocalDateTime.now();
    }

    public MockCallLog(Long mockRuleId, Long apiId, String requestMethod, String requestPath) {
        this();
        this.mockRuleId = mockRuleId;
        this.apiId = apiId;
        this.requestMethod = requestMethod;
        this.requestPath = requestPath;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMockRuleId() {
        return mockRuleId;
    }

    public void setMockRuleId(Long mockRuleId) {
        this.mockRuleId = mockRuleId;
    }

    public Long getApiId() {
        return apiId;
    }

    public void setApiId(Long apiId) {
        this.apiId = apiId;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public String getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(String requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }

    public Integer getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(Integer responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(String responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public Integer getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Integer responseTime) {
        this.responseTime = responseTime;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // 便捷方法
    public boolean isSuccess() {
        return responseStatus != null && responseStatus >= 200 && responseStatus < 300;
    }

    public boolean isClientError() {
        return responseStatus != null && responseStatus >= 400 && responseStatus < 500;
    }

    public boolean isServerError() {
        return responseStatus != null && responseStatus >= 500;
    }

    @Override
    public String toString() {
        return "MockCallLog{" +
                "id=" + id +
                ", mockRuleId=" + mockRuleId +
                ", apiId=" + apiId +
                ", requestMethod='" + requestMethod + '\'' +
                ", requestPath='" + requestPath + '\'' +
                ", responseStatus=" + responseStatus +
                ", responseTime=" + responseTime +
                ", createdAt=" + createdAt +
                '}';
    }
}