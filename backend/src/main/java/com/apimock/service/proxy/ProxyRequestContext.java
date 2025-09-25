package com.apimock.service.proxy;

import com.apimock.entity.proxy.ProxyConfig;

import java.util.Map;

/**
 * 代理请求上下文
 * 封装代理请求的所有信息
 */
public class ProxyRequestContext {

    private ProxyConfig config;
    private String method;
    private String path;
    private String queryString;
    private Map<String, String> headers;
    private String requestBody;
    private String clientIp;

    public ProxyRequestContext() {
    }

    public ProxyConfig getConfig() {
        return config;
    }

    public void setConfig(ProxyConfig config) {
        this.config = config;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    /**
     * 获取完整的URL（包含查询参数）
     */
    public String getFullUrl() {
        if (queryString != null && !queryString.isEmpty()) {
            return path + "?" + queryString;
        }
        return path;
    }

    @Override
    public String toString() {
        return "ProxyRequestContext{" +
                "method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", queryString='" + queryString + '\'' +
                ", clientIp='" + clientIp + '\'' +
                ", configName='" + (config != null ? config.getName() : "null") + '\'' +
                '}';
    }
}