package com.apimock.dto.project;

import com.apimock.entity.project.ProjectType;
import com.apimock.entity.project.ProjectVisibility;

import java.time.LocalDateTime;

public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private ProjectType type;
    private ProjectVisibility visibility;
    private String baseUrl;
    private String environment;
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
    private String projectTags;
    private Integer status;
    private OwnerInfo owner;
    private Integer memberCount;
    private Integer apiCount;
    private Integer mockCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 构造函数
    public ProjectResponse() {}

    public ProjectResponse(Long id, String name, String description,
                          ProjectType type, ProjectVisibility visibility,
                          OwnerInfo owner, Integer memberCount,
                          Integer apiCount, Integer mockCount,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.visibility = visibility;
        this.owner = owner;
        this.memberCount = memberCount;
        this.apiCount = apiCount;
        this.mockCount = mockCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getter和Setter方法
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

    public ProjectType getType() {
        return type;
    }

    public void setType(ProjectType type) {
        this.type = type;
    }

    public ProjectVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(ProjectVisibility visibility) {
        this.visibility = visibility;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
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

    public String getProjectTags() {
        return projectTags;
    }

    public void setProjectTags(String projectTags) {
        this.projectTags = projectTags;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public OwnerInfo getOwner() {
        return owner;
    }

    public void setOwner(OwnerInfo owner) {
        this.owner = owner;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // 嵌套类：项目所有者信息
    public static class OwnerInfo {
        private Long id;
        private String username;
        private String email;

        public OwnerInfo() {}

        public OwnerInfo(Long id, String username, String email) {
            this.id = id;
            this.username = username;
            this.email = email;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}