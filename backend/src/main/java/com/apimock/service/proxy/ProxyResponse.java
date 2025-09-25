package com.apimock.service.proxy;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

/**
 * 代理响应封装
 */
public class ProxyResponse {

    private HttpStatus status;
    private HttpHeaders headers;
    private String body;
    private String mode; // MOCK, PROXY, AUTO

    public ProxyResponse() {
    }

    public ProxyResponse(HttpStatus status, HttpHeaders headers, String body, String mode) {
        this.status = status;
        this.headers = headers;
        this.body = body;
        this.mode = mode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public boolean isSuccess() {
        return status != null && status.is2xxSuccessful();
    }

    @Override
    public String toString() {
        return "ProxyResponse{" +
                "status=" + status +
                ", mode='" + mode + '\'' +
                ", bodyLength=" + (body != null ? body.length() : 0) +
                '}';
    }
}