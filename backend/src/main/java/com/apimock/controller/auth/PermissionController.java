package com.apimock.controller.auth;

import com.apimock.annotation.RequirePermission;
import com.apimock.entity.auth.Permission;
import com.apimock.entity.auth.UserOperationLog;
import com.apimock.entity.project.ProjectRole;
import com.apimock.service.auth.PermissionService;
import com.apimock.service.auth.UserOperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 权限管理Controller
 *
 * @author AI Assistant
 */
@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private UserOperationLogService operationLogService;

    /**
     * 获取所有权限
     */
    @GetMapping
    @RequirePermission(value = "system:admin", requireProjectMember = false)
    public ResponseEntity<List<Permission>> getAllPermissions() {
        List<Permission> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(permissions);
    }

    /**
     * 根据分类获取权限
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Permission>> getPermissionsByCategory(@PathVariable String category) {
        List<Permission> permissions = permissionService.getPermissionsByCategory(category);
        return ResponseEntity.ok(permissions);
    }

    /**
     * 获取所有权限分类
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        List<String> categories = permissionService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * 获取角色的权限
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<Set<String>> getRolePermissions(@PathVariable ProjectRole role) {
        Set<String> permissions = permissionService.getRolePermissions(role);
        return ResponseEntity.ok(permissions);
    }

    /**
     * 检查角色权限
     */
    @GetMapping("/check")
    public ResponseEntity<Boolean> checkPermission(
            @RequestParam ProjectRole role,
            @RequestParam String permission) {
        boolean hasPermission = permissionService.hasPermission(role, permission);
        return ResponseEntity.ok(hasPermission);
    }

    /**
     * 为角色添加权限
     */
    @PostMapping("/role/{role}/add")
    @RequirePermission(value = "system:admin", requireProjectMember = false)
    public ResponseEntity<String> addPermissionToRole(
            @PathVariable ProjectRole role,
            @RequestParam String permissionCode) {
        permissionService.addPermissionToRole(role, permissionCode);
        return ResponseEntity.ok("权限添加成功");
    }

    /**
     * 从角色移除权限
     */
    @PostMapping("/role/{role}/remove")
    @RequirePermission(value = "system:admin", requireProjectMember = false)
    public ResponseEntity<String> removePermissionFromRole(
            @PathVariable ProjectRole role,
            @RequestParam String permissionCode) {
        permissionService.removePermissionFromRole(role, permissionCode);
        return ResponseEntity.ok("权限移除成功");
    }

    /**
     * 重置角色权限
     */
    @PostMapping("/role/{role}/reset")
    @RequirePermission(value = "system:admin", requireProjectMember = false)
    public ResponseEntity<String> resetRolePermissions(
            @PathVariable ProjectRole role,
            @RequestBody List<String> permissionCodes) {
        permissionService.resetRolePermissions(role, permissionCodes);
        return ResponseEntity.ok("角色权限重置成功");
    }

    /**
     * 创建新权限
     */
    @PostMapping
    @RequirePermission(value = "system:admin", requireProjectMember = false)
    public ResponseEntity<Permission> createPermission(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        String name = request.get("name");
        String description = request.get("description");
        String category = request.get("category");

        Permission permission = permissionService.createPermission(code, name, description, category);
        return ResponseEntity.ok(permission);
    }

    /**
     * 更新权限
     */
    @PutMapping("/{id}")
    @RequirePermission(value = "system:admin", requireProjectMember = false)
    public ResponseEntity<Permission> updatePermission(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String name = request.get("name");
        String description = request.get("description");
        String category = request.get("category");

        Permission permission = permissionService.updatePermission(id, name, description, category);
        return ResponseEntity.ok(permission);
    }

    /**
     * 启用/禁用权限
     */
    @PatchMapping("/{id}/toggle")
    @RequirePermission(value = "system:admin", requireProjectMember = false)
    public ResponseEntity<String> togglePermission(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        permissionService.togglePermission(id, enabled);
        return ResponseEntity.ok(enabled ? "权限已启用" : "权限已禁用");
    }

    /**
     * 删除权限
     */
    @DeleteMapping("/{id}")
    @RequirePermission(value = "system:admin", requireProjectMember = false)
    public ResponseEntity<String> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.ok("权限删除成功");
    }

    /**
     * 搜索权限
     */
    @GetMapping("/search")
    public ResponseEntity<List<Permission>> searchPermissions(@RequestParam String keyword) {
        List<Permission> permissions = permissionService.searchPermissions(keyword);
        return ResponseEntity.ok(permissions);
    }

    /**
     * 获取用户操作日志
     */
    @GetMapping("/logs/user/{userId}")
    @RequirePermission(value = {"member:manage", "stats:view"}, type = RequirePermission.CheckType.ANY)
    public ResponseEntity<Page<UserOperationLog>> getUserOperationLogs(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UserOperationLog> logs = operationLogService.getUserOperationLogs(userId, pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * 获取资源操作日志
     */
    @GetMapping("/logs/resource")
    @RequirePermission(value = {"project:manage", "stats:view"}, type = RequirePermission.CheckType.ANY)
    public ResponseEntity<Page<UserOperationLog>> getResourceOperationLogs(
            @RequestParam String resource,
            @RequestParam Long resourceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UserOperationLog> logs = operationLogService.getResourceOperationLogs(resource, resourceId, pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * 获取操作统计
     */
    @GetMapping("/logs/stats")
    @RequirePermission(value = "stats:view")
    public ResponseEntity<List<Object[]>> getOperationStats(@RequestParam(required = false) Integer days) {
        LocalDateTime since = days != null ? LocalDateTime.now().minusDays(days) : LocalDateTime.now().minusDays(7);
        List<Object[]> stats = operationLogService.countOperationsByType(since);
        return ResponseEntity.ok(stats);
    }
}