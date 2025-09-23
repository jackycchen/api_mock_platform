package com.apimock.entity.project;

import java.util.Arrays;
import java.util.List;

/**
 * 项目角色枚举
 * 定义项目中的不同角色和权限
 *
 * @author AI Assistant
 */
public enum ProjectRole {
    OWNER("所有者", "项目所有者，拥有所有权限", Arrays.asList(
            "project:manage", "project:delete", "project:settings",
            "member:manage", "member:invite", "member:remove",
            "api:create", "api:read", "api:update", "api:delete",
            "mock:create", "mock:read", "mock:update", "mock:delete",
            "doc:read", "doc:export", "stats:view"
    )),

    ADMIN("管理员", "项目管理员，拥有大部分权限", Arrays.asList(
            "project:settings", "member:invite", "member:remove",
            "api:create", "api:read", "api:update", "api:delete",
            "mock:create", "mock:read", "mock:update", "mock:delete",
            "doc:read", "doc:export", "stats:view"
    )),

    DEVELOPER("开发者", "开发者，可以管理API和Mock", Arrays.asList(
            "api:create", "api:read", "api:update", "api:delete",
            "mock:create", "mock:read", "mock:update", "mock:delete",
            "doc:read", "doc:export"
    )),

    VIEWER("观察者", "只读权限，只能查看", Arrays.asList(
            "api:read", "mock:read", "doc:read"
    ));

    private final String displayName;
    private final String description;
    private final List<String> permissions;

    ProjectRole(String displayName, String description, List<String> permissions) {
        this.displayName = displayName;
        this.description = description;
        this.permissions = permissions;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    /**
     * 检查是否有指定权限
     */
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    /**
     * 检查是否有任意一个权限
     */
    public boolean hasAnyPermission(String... permissionArray) {
        for (String permission : permissionArray) {
            if (hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否有所有权限
     */
    public boolean hasAllPermissions(String... permissionArray) {
        for (String permission : permissionArray) {
            if (!hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 从字符串转换为枚举
     */
    public static ProjectRole fromString(String role) {
        for (ProjectRole projectRole : ProjectRole.values()) {
            if (projectRole.name().equalsIgnoreCase(role)) {
                return projectRole;
            }
        }
        return VIEWER; // 默认为观察者
    }

    /**
     * 判断角色等级（用于权限比较）
     */
    public int getLevel() {
        switch (this) {
            case OWNER: return 4;
            case ADMIN: return 3;
            case DEVELOPER: return 2;
            case VIEWER: return 1;
            default: return 0;
        }
    }

    /**
     * 判断是否可以管理指定角色
     */
    public boolean canManageRole(ProjectRole targetRole) {
        return this.getLevel() > targetRole.getLevel();
    }

    /**
     * 获取可以邀请的角色列表
     */
    public List<ProjectRole> getInvitableRoles() {
        switch (this) {
            case OWNER:
                return Arrays.asList(ADMIN, DEVELOPER, VIEWER);
            case ADMIN:
                return Arrays.asList(DEVELOPER, VIEWER);
            default:
                return Arrays.asList(); // 其他角色不能邀请成员
        }
    }
}