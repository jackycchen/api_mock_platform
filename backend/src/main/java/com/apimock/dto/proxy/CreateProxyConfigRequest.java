package com.apimock.dto.proxy;

import jakarta.validation.constraints.*;

import java.util.List;

/**
 * 创建代理配置请求
 */
public class CreateProxyConfigRequest {

    @NotBlank(message = "规则名称不能为空")
    @Size(max = 100, message = "规则名称不能超过100个字符")
    private String name;

    @NotBlank(message = "路径匹配规则不能为空")
    @Pattern(regexp = "/.*", message = "路径规则必须以/开头")
    private String pathPattern;

    @NotBlank(message = "代理模式不能为空")
    private String mode;

    @Size(max = 255, message = "目标地址不能超过255个字符")
    private String targetUrl;

    private List<@Size(max = 100, message = "请求头名称不能超过100个字符") String> forwardHeaders;

    private Boolean preserveHost = false;

    private Boolean enabled = true;

    // Getters and Setters

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
}
