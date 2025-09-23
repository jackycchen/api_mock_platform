package com.apimock.repository.auth;

import com.apimock.entity.auth.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 权限Repository接口
 *
 * @author AI Assistant
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * 根据权限代码查找权限
     */
    Optional<Permission> findByCode(String code);

    /**
     * 根据权限代码列表查找权限
     */
    List<Permission> findByCodeIn(List<String> codes);

    /**
     * 根据分类查找权限
     */
    List<Permission> findByCategory(String category);

    /**
     * 根据分类和启用状态查找权限
     */
    List<Permission> findByCategoryAndEnabled(String category, Boolean enabled);

    /**
     * 查找所有启用的权限
     */
    List<Permission> findByEnabledTrue();

    /**
     * 根据权限名称模糊查询
     */
    @Query("SELECT p FROM Permission p WHERE p.name LIKE %:name% AND p.enabled = true")
    List<Permission> findByNameContaining(@Param("name") String name);

    /**
     * 获取所有权限分类
     */
    @Query("SELECT DISTINCT p.category FROM Permission p WHERE p.enabled = true ORDER BY p.category")
    List<String> findAllCategories();

    /**
     * 检查权限代码是否存在
     */
    boolean existsByCode(String code);
}