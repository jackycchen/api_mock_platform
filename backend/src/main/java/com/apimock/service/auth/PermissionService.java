package com.apimock.service.auth;

import com.apimock.entity.auth.Permission;
import com.apimock.entity.auth.RolePermission;
import com.apimock.entity.project.ProjectRole;
import com.apimock.repository.auth.PermissionRepository;
import com.apimock.repository.auth.RolePermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限管理服务
 *
 * @author AI Assistant
 */
@Service
@Transactional
public class PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    /**
     * 获取所有权限
     */
    public List<Permission> getAllPermissions() {
        return permissionRepository.findByEnabledTrue();
    }

    /**
     * 根据分类获取权限
     */
    public List<Permission> getPermissionsByCategory(String category) {
        return permissionRepository.findByCategoryAndEnabled(category, true);
    }

    /**
     * 获取所有权限分类
     */
    public List<String> getAllCategories() {
        return permissionRepository.findAllCategories();
    }

    /**
     * 根据权限代码获取权限
     */
    public Optional<Permission> getPermissionByCode(String code) {
        return permissionRepository.findByCode(code);
    }

    /**
     * 获取角色的所有权限
     */
    @Cacheable(value = "rolePermissions", key = "#role.name()")
    public Set<String> getRolePermissions(ProjectRole role) {
        List<String> permissions = rolePermissionRepository.findPermissionCodesByRole(role);
        return permissions.stream().collect(Collectors.toSet());
    }

    /**
     * 检查角色是否有指定权限
     */
    @Cacheable(value = "permissionCheck", key = "#role.name() + ':' + #permissionCode")
    public boolean hasPermission(ProjectRole role, String permissionCode) {
        return rolePermissionRepository.hasPermission(role, permissionCode);
    }

    /**
     * 检查角色是否有任意一个权限
     */
    public boolean hasAnyPermission(ProjectRole role, String... permissionCodes) {
        for (String permissionCode : permissionCodes) {
            if (hasPermission(role, permissionCode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查角色是否有所有权限
     */
    public boolean hasAllPermissions(ProjectRole role, String... permissionCodes) {
        for (String permissionCode : permissionCodes) {
            if (!hasPermission(role, permissionCode)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 为角色添加权限
     */
    public void addPermissionToRole(ProjectRole role, String permissionCode) {
        Optional<Permission> permission = permissionRepository.findByCode(permissionCode);
        if (permission.isPresent()) {
            RolePermission existing = rolePermissionRepository.findByRoleAndPermissionId(role, permission.get().getId());
            if (existing == null) {
                RolePermission rolePermission = new RolePermission(role, permission.get());
                rolePermissionRepository.save(rolePermission);
            } else if (!existing.getEnabled()) {
                existing.setEnabled(true);
                rolePermissionRepository.save(existing);
            }
        }
    }

    /**
     * 从角色移除权限
     */
    public void removePermissionFromRole(ProjectRole role, String permissionCode) {
        Optional<Permission> permission = permissionRepository.findByCode(permissionCode);
        if (permission.isPresent()) {
            RolePermission existing = rolePermissionRepository.findByRoleAndPermissionId(role, permission.get().getId());
            if (existing != null) {
                existing.setEnabled(false);
                rolePermissionRepository.save(existing);
            }
        }
    }

    /**
     * 重置角色权限
     */
    public void resetRolePermissions(ProjectRole role, List<String> permissionCodes) {
        // 禁用角色的所有权限
        List<RolePermission> existingPermissions = rolePermissionRepository.findByRole(role);
        for (RolePermission rp : existingPermissions) {
            rp.setEnabled(false);
            rolePermissionRepository.save(rp);
        }

        // 添加新权限
        for (String permissionCode : permissionCodes) {
            addPermissionToRole(role, permissionCode);
        }
    }

    /**
     * 创建新权限
     */
    public Permission createPermission(String code, String name, String description, String category) {
        if (permissionRepository.existsByCode(code)) {
            throw new IllegalArgumentException("权限代码已存在: " + code);
        }

        Permission permission = new Permission(code, name, description, category);
        return permissionRepository.save(permission);
    }

    /**
     * 更新权限
     */
    public Permission updatePermission(Long id, String name, String description, String category) {
        Optional<Permission> permissionOpt = permissionRepository.findById(id);
        if (permissionOpt.isEmpty()) {
            throw new IllegalArgumentException("权限不存在: " + id);
        }

        Permission permission = permissionOpt.get();
        permission.setName(name);
        permission.setDescription(description);
        permission.setCategory(category);
        return permissionRepository.save(permission);
    }

    /**
     * 启用/禁用权限
     */
    public void togglePermission(Long id, boolean enabled) {
        Optional<Permission> permissionOpt = permissionRepository.findById(id);
        if (permissionOpt.isPresent()) {
            Permission permission = permissionOpt.get();
            permission.setEnabled(enabled);
            permissionRepository.save(permission);
        }
    }

    /**
     * 删除权限
     */
    public void deletePermission(Long id) {
        if (permissionRepository.existsById(id)) {
            // 先删除角色权限关联
            rolePermissionRepository.deleteByPermissionId(id);
            // 再删除权限
            permissionRepository.deleteById(id);
        }
    }

    /**
     * 搜索权限
     */
    public List<Permission> searchPermissions(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllPermissions();
        }
        return permissionRepository.findByNameContaining(keyword.trim());
    }

    /**
     * 检查权限代码是否存在
     */
    public boolean existsByCode(String code) {
        return permissionRepository.existsByCode(code);
    }
}