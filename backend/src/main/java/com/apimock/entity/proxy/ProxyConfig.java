package com.apimock.entity.proxy;

import com.apimock.entity.project.Project;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 项目代理配置
 */
@Entity
@Table(name = "proxy_configs", uniqueConstraints = {
        @UniqueConstraint(name = "uq_proxy_config_project_path", columnNames = {"project_id", "path_pattern"})
})
public class ProxyConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "path_pattern", nullable = false, length = 255)
    private String pathPattern;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProxyMode mode = ProxyMode.MOCK;

    @Column(name = "target_url", length = 255)
    private String targetUrl;

    @Column(name = "forward_headers", columnDefinition = "TEXT")
    private String forwardHeaders;

    @Column(name = "preserve_host")
    private Boolean preserveHost = false;

    @Column(nullable = false)
    private Boolean enabled = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPathPattern() {
        return pathPattern;
    }

    public void setPathPattern(String pathPattern) {
        this.pathPattern = pathPattern;
    }

    public ProxyMode getMode() {
        return mode;
    }

    public void setMode(ProxyMode mode) {
        this.mode = mode;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public String getForwardHeaders() {
        return forwardHeaders;
    }

    public void setForwardHeaders(String forwardHeaders) {
        this.forwardHeaders = forwardHeaders;
    }

    public Boolean getPreserveHost() {
        return preserveHost;
    }

    public void setPreserveHost(Boolean preserveHost) {
        this.preserveHost = preserveHost;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
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
}
