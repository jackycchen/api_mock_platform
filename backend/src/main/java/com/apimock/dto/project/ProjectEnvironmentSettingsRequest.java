package com.apimock.dto.project;

import jakarta.validation.constraints.*;

/**
 * 项目环境配置请求DTO
 *
 * @author AI Assistant
 */
public class ProjectEnvironmentSettingsRequest {

    @NotBlank(message = "环境类型不能为空")
    private String environment;

    @Size(max = 255, message = "基础URL不能超过255个字符")
    @Pattern(regexp = "^$|^https?://.*", message = "基础URL格式不正确")
    private String baseUrl;

    @Size(max = 255, message = "Mock域名不能超过255个字符")
    private String mockDomain;

    private Boolean proxyEnabled = false;

    @Size(max = 255, message = "代理目标不能超过255个字符")
    @Pattern(regexp = "^$|^https?://.*", message = "代理目标URL格式不正确")
    private String proxyTarget;

    private Boolean allowCors = true;

    @Size(max = 500, message = "CORS域名不能超过500个字符")
    private String corsOrigins = "*";

    @Min(value = 1000, message = "请求超时时间不能小于1000毫秒")
    @Max(value = 300000, message = "请求超时时间不能大于300000毫秒")
    private Integer requestTimeout = 30000;

    @Min(value = 1, message = "速率限制不能小于1")
    @Max(value = 100000, message = "速率限制不能大于100000")
    private Integer rateLimit = 1000;

    private Boolean enableLogging = true;

    private Boolean enableCache = true;

    @Min(value = 60, message = "缓存TTL不能小于60秒")
    @Max(value = 86400, message = "缓存TTL不能大于86400秒")
    private Integer cacheTtl = 3600;

    // 构造函数
    public ProjectEnvironmentSettingsRequest() {}

    // Getters and Setters
    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getMockDomain() {
        return mockDomain;
    }

    public void setMockDomain(String mockDomain) {
        this.mockDomain = mockDomain;
    }

    public Boolean getProxyEnabled() {
        return proxyEnabled;
    }

    public void setProxyEnabled(Boolean proxyEnabled) {
        this.proxyEnabled = proxyEnabled;
    }

    public String getProxyTarget() {
        return proxyTarget;
    }

    public void setProxyTarget(String proxyTarget) {
        this.proxyTarget = proxyTarget;
    }

    public Boolean getAllowCors() {
        return allowCors;
    }

    public void setAllowCors(Boolean allowCors) {
        this.allowCors = allowCors;
    }

    public String getCorsOrigins() {
        return corsOrigins;
    }

    public void setCorsOrigins(String corsOrigins) {
        this.corsOrigins = corsOrigins;
    }

    public Integer getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(Integer requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public Integer getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(Integer rateLimit) {
        this.rateLimit = rateLimit;
    }

    public Boolean getEnableLogging() {
        return enableLogging;
    }

    public void setEnableLogging(Boolean enableLogging) {
        this.enableLogging = enableLogging;
    }

    public Boolean getEnableCache() {
        return enableCache;
    }

    public void setEnableCache(Boolean enableCache) {
        this.enableCache = enableCache;
    }

    public Integer getCacheTtl() {
        return cacheTtl;
    }

    public void setCacheTtl(Integer cacheTtl) {
        this.cacheTtl = cacheTtl;
    }
}