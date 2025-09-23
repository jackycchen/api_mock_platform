package com.apimock.dto.project;

import com.apimock.entity.project.Project;
import com.apimock.entity.project.ProjectEnvironment;

/**
 * 项目设置响应DTO
 *
 * @author AI Assistant
 */
public class ProjectSettingsResponse {

    // 基本信息
    private Long id;
    private String name;
    private String description;
    private String type;
    private String visibility;
    private String projectTags;

    // 环境配置
    private String environment;
    private String baseUrl;
    private String mockDomain;
    private Boolean proxyEnabled;
    private String proxyTarget;
    private Boolean allowCors;
    private String corsOrigins;
    private Integer requestTimeout;
    private Integer rateLimit;
    private Boolean enableLogging;
    private Boolean enableCache;
    private Integer cacheTtl;

    // 统计信息
    private Integer memberCount;
    private Integer apiCount;
    private Integer mockCount;
    private Integer status;

    // 所有者信息
    private Long ownerId;
    private String ownerUsername;

    // 时间信息
    private String createdAt;
    private String updatedAt;

    // 构造函数
    public ProjectSettingsResponse() {}

    public ProjectSettingsResponse(Project project) {
        this.id = project.getId();
        this.name = project.getName();
        this.description = project.getDescription();
        this.type = project.getType().toString();
        this.visibility = project.getVisibility().toString();
        this.projectTags = project.getProjectTags();

        this.environment = project.getEnvironment();
        this.baseUrl = project.getBaseUrl();
        this.mockDomain = project.getMockDomain();
        this.proxyEnabled = project.getProxyEnabled();
        this.proxyTarget = project.getProxyTarget();
        this.allowCors = project.getAllowCors();
        this.corsOrigins = project.getCorsOrigins();
        this.requestTimeout = project.getRequestTimeout();
        this.rateLimit = project.getRateLimit();
        this.enableLogging = project.getEnableLogging();
        this.enableCache = project.getEnableCache();
        this.cacheTtl = project.getCacheTtl();

        this.memberCount = project.getMemberCount();
        this.apiCount = project.getApiCount();
        this.mockCount = project.getMockCount();
        this.status = project.getStatus();

        if (project.getOwner() != null) {
            this.ownerId = project.getOwner().getId();
            this.ownerUsername = project.getOwner().getUsername();
        }

        this.createdAt = project.getCreatedAt() != null ? project.getCreatedAt().toString() : null;
        this.updatedAt = project.getUpdatedAt() != null ? project.getUpdatedAt().toString() : null;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getProjectTags() {
        return projectTags;
    }

    public void setProjectTags(String projectTags) {
        this.projectTags = projectTags;
    }

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

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public Integer getApiCount() {
        return apiCount;
    }

    public void setApiCount(Integer apiCount) {
        this.apiCount = apiCount;
    }

    public Integer getMockCount() {
        return mockCount;
    }

    public void setMockCount(Integer mockCount) {
        this.mockCount = mockCount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}