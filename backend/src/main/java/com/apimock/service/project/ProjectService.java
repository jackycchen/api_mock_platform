package com.apimock.service.project;

import com.apimock.dto.project.*;
import com.apimock.entity.auth.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectService {

    /**
     * 创建项目
     */
    ProjectResponse createProject(CreateProjectRequest request, User currentUser);

    /**
     * 获取项目详情
     */
    ProjectResponse getProjectById(Long id, User currentUser);

    /**
     * 更新项目信息
     */
    ProjectResponse updateProject(Long id, UpdateProjectRequest request, User currentUser);

    /**
     * 删除项目（软删除）
     */
    void deleteProject(Long id, User currentUser);

    /**
     * 获取用户有权限访问的项目列表
     */
    Page<ProjectResponse> getAccessibleProjects(User currentUser, Pageable pageable);

    /**
     * 搜索项目
     */
    Page<ProjectResponse> searchProjects(String keyword, User currentUser, Pageable pageable);

    /**
     * 获取用户创建的项目列表
     */
    Page<ProjectResponse> getUserProjects(User currentUser, Pageable pageable);

    /**
     * 检查项目名称是否可用
     */
    boolean isProjectNameAvailable(String name);

    /**
     * 检查用户是否有项目访问权限
     */
    boolean hasProjectAccess(Long projectId, User user);

    /**
     * 检查用户是否为项目所有者
     */
    boolean isProjectOwner(Long projectId, User user);

    /**
     * 更新项目统计信息
     */
    void updateProjectStats(Long projectId);

    // ========== 项目设置相关方法 ==========

    /**
     * 获取项目设置
     */
    ProjectSettingsResponse getProjectSettings(Long projectId, User currentUser);

    /**
     * 更新项目基本设置
     */
    ProjectSettingsResponse updateProjectBasicSettings(Long projectId, ProjectBasicSettingsRequest request, User currentUser);

    /**
     * 更新项目环境设置
     */
    ProjectSettingsResponse updateProjectEnvironmentSettings(Long projectId, ProjectEnvironmentSettingsRequest request, User currentUser);

    /**
     * 转让项目所有权
     */
    boolean transferProjectOwnership(Long projectId, ProjectTransferRequest request, User currentUser);

    /**
     * 永久删除项目
     */
    boolean permanentDeleteProject(Long projectId, String confirmPassword, User currentUser);

    /**
     * 恢复已删除的项目
     */
    boolean restoreProject(Long projectId, User currentUser);

    /**
     * 检查项目名称是否可用（排除指定项目）
     */
    boolean isProjectNameAvailable(String name, Long excludeProjectId);

    /**
     * 生成项目Mock域名
     */
    String generateMockDomain(Long projectId);

    /**
     * 验证用户密码
     */
    boolean validateUserPassword(User user, String password);
}