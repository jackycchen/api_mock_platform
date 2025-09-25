package com.apimock.controller.test;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 代理测试控制器
 * 用于测试代理拦截功能
 */
@RestController
@RequestMapping("/api/test")
public class ProxyTestController {

    /**
     * 测试Mock路径 - 应该被代理拦截
     */
    @GetMapping("/mock/users/{id}")
    public ResponseEntity<Map<String, Object>> getMockUser(@PathVariable Long id) {
        // 这个方法通常不会被执行，因为会被代理拦截
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This should not be reached if proxy is working");
        response.put("id", id);
        return ResponseEntity.ok(response);
    }

    /**
     * 测试正常路径 - 不应该被代理拦截
     */
    @GetMapping("/normal/users/{id}")
    public ResponseEntity<Map<String, Object>> getNormalUser(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Normal endpoint - not intercepted");
        response.put("id", id);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * 代理状态检查
     */
    @GetMapping("/proxy/status")
    public ResponseEntity<Map<String, Object>> getProxyStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("proxyEnabled", true);
        status.put("message", "Proxy interceptor is running");
        status.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(status);
    }
}