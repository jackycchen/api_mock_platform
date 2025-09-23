package com.apimock.controller.project;

import com.apimock.annotation.RequirePermission;
import com.apimock.dto.ApiResponse;
import com.apimock.dto.project.*;
import com.apimock.entity.auth.User;
import com.apimock.service.project.ProjectService;
import com.apimock.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 项目设置控制器
 * 处理项目设置相关的HTTP请求
 *
 * @author AI Assistant
 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/settings")
public class ProjectSettingsController {

    @Autowired
    private ProjectService projectService;

    /**
     * 获取项目设置
     */
    @GetMapping
    @RequirePermission({"project:read"})
    public ResponseEntity<ApiResponse<ProjectSettingsResponse>> getProjectSettings(@PathVariable Long projectId) {
        User currentUser = SecurityUtils.getCurrentUser();
        ProjectSettingsResponse settings = projectService.getProjectSettings(projectId, currentUser);
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    /**
     * 更新项目基本设置
     */
    @PutMapping("/basic")
    @RequirePermission({"project:write"})
    public ResponseEntity<ApiResponse<ProjectSettingsResponse>> updateBasicSettings(
            @PathVariable Long projectId,
            @Validated @RequestBody ProjectBasicSettingsRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();
        ProjectSettingsResponse settings = projectService.updateProjectBasicSettings(projectId, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("基本设置更新成功", settings));
    }

    /**
     * 更新项目环境设置
     */
    @PutMapping("/environment")
    @RequirePermission({"project:write"})
    public ResponseEntity<ApiResponse<ProjectSettingsResponse>> updateEnvironmentSettings(
            @PathVariable Long projectId,
            @Validated @RequestBody ProjectEnvironmentSettingsRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();
        ProjectSettingsResponse settings = projectService.updateProjectEnvironmentSettings(projectId, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("环境设置更新成功", settings));
    }

    /**
     * 转让项目所有权
     */
    @PostMapping("/transfer")
    @RequirePermission({"project:admin"})
    public ResponseEntity<ApiResponse<Boolean>> transferOwnership(
            @PathVariable Long projectId,
            @Validated @RequestBody ProjectTransferRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();
        boolean success = projectService.transferProjectOwnership(projectId, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("项目所有权转让成功", success));
    }

    /**
     * 永久删除项目
     */
    @DeleteMapping("/permanent")
    @RequirePermission({"project:admin"})
    public ResponseEntity<ApiResponse<Boolean>> permanentDelete(
            @PathVariable Long projectId,
            @RequestParam String confirmPassword) {
        User currentUser = SecurityUtils.getCurrentUser();
        boolean success = projectService.permanentDeleteProject(projectId, confirmPassword, currentUser);
        return ResponseEntity.ok(ApiResponse.success("项目已永久删除", success));
    }

    /**
     * 恢复已删除的项目
     */
    @PostMapping("/restore")
    @RequirePermission({"project:admin"})
    public ResponseEntity<ApiResponse<Boolean>> restoreProject(@PathVariable Long projectId) {
        User currentUser = SecurityUtils.getCurrentUser();
        boolean success = projectService.restoreProject(projectId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("项目恢复成功", success));
    }

    /**
     * 检查项目名称可用性（用于设置页面）
     */
    @GetMapping("/check-name")
    @RequirePermission({"project:read"})
    public ResponseEntity<ApiResponse<Boolean>> checkNameAvailability(
            @PathVariable Long projectId,
            @RequestParam String name) {
        boolean available = projectService.isProjectNameAvailable(name, projectId);
        return ResponseEntity.ok(ApiResponse.success(available));
    }

    /**
     * 生成Mock域名
     */
    @PostMapping("/generate-mock-domain")
    @RequirePermission({"project:write"})
    public ResponseEntity<ApiResponse<String>> generateMockDomain(@PathVariable Long projectId) {
        String mockDomain = projectService.generateMockDomain(projectId);
        return ResponseEntity.ok(ApiResponse.success(mockDomain));
    }

    /**
     * 验证用户密码（用于敏感操作）
     */
    @PostMapping("/verify-password")
    public ResponseEntity<ApiResponse<Boolean>> verifyPassword(@RequestParam String password) {
        User currentUser = SecurityUtils.getCurrentUser();
        boolean valid = projectService.validateUserPassword(currentUser, password);
        return ResponseEntity.ok(ApiResponse.success(valid));
    }
}