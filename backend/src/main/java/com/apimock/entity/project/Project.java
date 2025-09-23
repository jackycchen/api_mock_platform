package com.apimock.entity.project;

import com.apimock.entity.auth.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectVisibility visibility;

    @Column(name = "base_url", length = 255)
    private String baseUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "environment", length = 50)
    private ProjectEnvironment environment = ProjectEnvironment.DEVELOPMENT;

    @Column(name = "mock_domain", length = 255)
    private String mockDomain;

    @Column(name = "proxy_enabled")
    private Boolean proxyEnabled = false;

    @Column(name = "proxy_target", length = 255)
    private String proxyTarget;

    @Column(name = "allow_cors")
    private Boolean allowCors = true;

    @Column(name = "cors_origins", length = 500)
    private String corsOrigins = "*";

    @Column(name = "request_timeout")
    private Integer requestTimeout = 30000; // 30秒

    @Column(name = "rate_limit")
    private Integer rateLimit = 1000; // 每分钟1000次请求

    @Column(name = "enable_logging")
    private Boolean enableLogging = true;

    @Column(name = "enable_cache")
    private Boolean enableCache = true;

    @Column(name = "cache_ttl")
    private Integer cacheTtl = 3600; // 1小时

    @Column(name = "project_tags", length = 500)
    private String projectTags;

    @Column(nullable = false)
    private Integer status = 1; // 1: 启用, 0: 禁用

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "member_count")
    private Integer memberCount = 1;

    @Column(name = "api_count")
    private Integer apiCount = 0;

    @Column(name = "mock_count")
    private Integer mockCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 构造函数
    public Project() {}

    public Project(String name, String description, ProjectType type,
                   ProjectVisibility visibility, User owner) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.visibility = visibility;
        this.owner = owner;
        this.environment = ProjectEnvironment.DEVELOPMENT;
        this.proxyEnabled = false;
        this.allowCors = true;
        this.corsOrigins = "*";
        this.requestTimeout = 30000;
        this.rateLimit = 1000;
        this.enableLogging = true;
        this.enableCache = true;
        this.cacheTtl = 3600;
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
        return environment != null ? environment.getCode() : ProjectEnvironment.DEVELOPMENT.getCode();
    }

    public void setEnvironment(String environment) {
        this.environment = ProjectEnvironment.fromCode(environment);
    }

    public ProjectEnvironment getProjectEnvironment() {
        return environment;
    }

    public void setProjectEnvironment(ProjectEnvironment environment) {
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

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
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

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    // 业务方法
    public boolean isActive() {
        return status == 1 && deletedAt == null;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void markAsDeleted() {
        this.deletedAt = LocalDateTime.now();
        this.status = 0;
    }

    public boolean isPublic() {
        return visibility == ProjectVisibility.PUBLIC;
    }

    public boolean isPrivate() {
        return visibility == ProjectVisibility.PRIVATE;
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", visibility=" + visibility +
                ", status=" + status +
                '}';
    }
}