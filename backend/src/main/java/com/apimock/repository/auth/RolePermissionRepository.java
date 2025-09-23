package com.apimock.repository.auth;

import com.apimock.entity.auth.RolePermission;
import com.apimock.entity.project.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色权限关联Repository接口
 *
 * @author AI Assistant
 */
@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    /**
     * 根据角色查找权限关联
     */
    List<RolePermission> findByRole(ProjectRole role);

    /**
     * 根据角色和启用状态查找权限关联
     */
    List<RolePermission> findByRoleAndEnabled(ProjectRole role, Boolean enabled);

    /**
     * 根据权限ID查找角色关联
     */
    List<RolePermission> findByPermissionId(Long permissionId);

    /**
     * 查找角色的所有权限代码
     */
    @Query("SELECT p.code FROM RolePermission rp JOIN rp.permission p WHERE rp.role = :role AND rp.enabled = true AND p.enabled = true")
    List<String> findPermissionCodesByRole(@Param("role") ProjectRole role);

    /**
     * 检查角色是否有指定权限
     */
    @Query("SELECT COUNT(rp) > 0 FROM RolePermission rp JOIN rp.permission p WHERE rp.role = :role AND p.code = :permissionCode AND rp.enabled = true AND p.enabled = true")
    boolean hasPermission(@Param("role") ProjectRole role, @Param("permissionCode") String permissionCode);

    /**
     * 根据角色和权限ID查找关联
     */
    RolePermission findByRoleAndPermissionId(ProjectRole role, Long permissionId);

    /**
     * 删除角色的所有权限
     */
    void deleteByRole(ProjectRole role);

    /**
     * 根据权限ID删除关联
     */
    void deleteByPermissionId(Long permissionId);
}