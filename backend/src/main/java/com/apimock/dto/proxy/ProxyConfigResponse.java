package com.apimock.dto.proxy;

import com.apimock.entity.proxy.ProxyConfig;
import com.apimock.entity.proxy.ProxyMode;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 代理配置响应
 */
public class ProxyConfigResponse {

    private Long id;
    private String name;
    private String pathPattern;
    private String mode;
    private String targetUrl;
    private List<String> forwardHeaders;
    private Boolean preserveHost;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProxyConfigResponse() {
    }

    public ProxyConfigResponse(ProxyConfig config) {
        this.id = config.getId();
        this.name = config.getName();
        this.pathPattern = config.getPathPattern();
        ProxyMode mode = config.getMode();
        this.mode = mode != null ? mode.name() : ProxyMode.MOCK.name();
        this.targetUrl = config.getTargetUrl();
        this.forwardHeaders = parseHeaders(config.getForwardHeaders());
        this.preserveHost = config.getPreserveHost();
        this.enabled = config.getEnabled();
        this.createdAt = config.getCreatedAt();
        this.updatedAt = config.getUpdatedAt();
    }

    private List<String> parseHeaders(String headers) {
        if (headers == null || headers.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(headers.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
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

    public String getPathPattern() {
        return pathPattern;
    }

    public void setPathPattern(String pathPattern) {
        this.pathPattern = pathPattern;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public List<String> getForwardHeaders() {
        return forwardHeaders;
    }

    public void setForwardHeaders(List<String> forwardHeaders) {
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
