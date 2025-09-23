package com.apimock.dto.project;

import com.apimock.entity.project.ProjectType;
import com.apimock.entity.project.ProjectVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateProjectRequest {

    @NotBlank(message = "项目名称不能为空")
    @Size(min = 2, max = 100, message = "项目名称长度在2到100个字符之间")
    private String name;

    @Size(max = 500, message = "项目描述不能超过500个字符")
    private String description;

    @NotNull(message = "项目类型不能为空")
    private ProjectType type;

    @NotNull(message = "项目可见性不能为空")
    private ProjectVisibility visibility;

    @Size(max = 255, message = "基础URL不能超过255个字符")
    private String baseUrl;

    @Size(max = 50, message = "环境名称不能超过50个字符")
    private String environment;

    @Size(max = 255, message = "Mock域名不能超过255个字符")
    private String mockDomain;

    private Boolean proxyEnabled;

    @Size(max = 255, message = "代理目标不能超过255个字符")
    private String proxyTarget;

    private Boolean allowCors;

    @Size(max = 500, message = "CORS来源不能超过500个字符")
    private String corsOrigins;

    private Integer requestTimeout;

    private Integer rateLimit;

    private Boolean enableLogging;

    private Boolean enableCache;

    private Integer cacheTtl;

    @Size(max = 500, message = "项目标签不能超过500个字符")
    private String projectTags;

    private Integer status;

    // 构造函数
    public UpdateProjectRequest() {}

    // Getter和Setter方法
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

    @Override
    public String toString() {
        return "UpdateProjectRequest{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", visibility=" + visibility +
                '}';
    }
}