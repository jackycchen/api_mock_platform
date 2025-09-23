package com.apimock.controller.proxy;

import com.apimock.annotation.RequirePermission;
import com.apimock.dto.ApiResponse;
import com.apimock.dto.proxy.CreateProxyConfigRequest;
import com.apimock.dto.proxy.ProxyConfigResponse;
import com.apimock.dto.proxy.UpdateProxyConfigRequest;
import com.apimock.entity.auth.User;
import com.apimock.service.proxy.ProxyConfigService;
import com.apimock.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 代理配置控制器
 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/proxy-configs")
public class ProxyConfigController {

    @Autowired
    private ProxyConfigService proxyConfigService;

    @GetMapping
    @RequirePermission({"project:read"})
    public ResponseEntity<ApiResponse<List<ProxyConfigResponse>>> listProxyConfigs(@PathVariable Long projectId) {
        User currentUser = SecurityUtils.getCurrentUser();
        List<ProxyConfigResponse> configs = proxyConfigService.listProxyConfigs(projectId, currentUser);
        return ResponseEntity.ok(ApiResponse.success(configs));
    }

    @PostMapping
    @RequirePermission({"project:write"})
    public ResponseEntity<ApiResponse<ProxyConfigResponse>> createProxyConfig(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateProxyConfigRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();
        ProxyConfigResponse response = proxyConfigService.createProxyConfig(projectId, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("代理配置创建成功", response));
    }

    @PutMapping("/{configId}")
    @RequirePermission({"project:write"})
    public ResponseEntity<ApiResponse<ProxyConfigResponse>> updateProxyConfig(
            @PathVariable Long projectId,
            @PathVariable Long configId,
            @Valid @RequestBody UpdateProxyConfigRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();
        ProxyConfigResponse response = proxyConfigService.updateProxyConfig(projectId, configId, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("代理配置更新成功", response));
    }

    @PostMapping("/{configId}/toggle")
    @RequirePermission({"project:write"})
    public ResponseEntity<ApiResponse<ProxyConfigResponse>> toggleProxyConfig(
            @PathVariable Long projectId,
            @PathVariable Long configId,
            @RequestParam boolean enabled) {
        User currentUser = SecurityUtils.getCurrentUser();
        ProxyConfigResponse response = proxyConfigService.toggleProxyConfig(projectId, configId, enabled, currentUser);
        String message = enabled ? "代理配置已启用" : "代理配置已禁用";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    @DeleteMapping("/{configId}")
    @RequirePermission({"project:write"})
    public ResponseEntity<ApiResponse<Void>> deleteProxyConfig(
            @PathVariable Long projectId,
            @PathVariable Long configId) {
        User currentUser = SecurityUtils.getCurrentUser();
        proxyConfigService.deleteProxyConfig(projectId, configId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("代理配置已删除", null));
    }
}
