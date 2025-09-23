package com.apimock.entity.auth;

import com.apimock.entity.project.ProjectRole;
import jakarta.persistence.*;

/**
 * 角色权限关联实体类
 * 定义角色和权限的多对多关系
 *
 * @author AI Assistant
 */
@Entity
@Table(name = "role_permissions")
public class RolePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @Column(nullable = false)
    private Boolean enabled = true;

    // 构造函数
    public RolePermission() {}

    public RolePermission(ProjectRole role, Permission permission) {
        this.role = role;
        this.permission = permission;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProjectRole getRole() {
        return role;
    }

    public void setRole(ProjectRole role) {
        this.role = role;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "RolePermission{" +
                "id=" + id +
                ", role=" + role +
                ", permission=" + (permission != null ? permission.getCode() : null) +
                ", enabled=" + enabled +
                '}';
    }
}