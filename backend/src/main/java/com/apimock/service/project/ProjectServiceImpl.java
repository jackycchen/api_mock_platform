package com.apimock.service.project;

import com.apimock.dto.project.*;
import com.apimock.entity.auth.User;
import com.apimock.entity.project.Project;
import com.apimock.entity.project.ProjectType;
import com.apimock.entity.project.ProjectVisibility;
import com.apimock.entity.project.ProjectEnvironment;
import com.apimock.exception.ProjectNotFoundException;
import com.apimock.exception.ProjectNameAlreadyExistsException;
import com.apimock.exception.UnauthorizedAccessException;
import com.apimock.repository.ProjectRepository;
import com.apimock.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMemberService projectMemberService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public ProjectResponse createProject(CreateProjectRequest request, User currentUser) {
        // 检查项目名称是否已存在
        if (!isProjectNameAvailable(request.getName())) {
            throw new ProjectNameAlreadyExistsException("项目名称已存在");
        }

        // 创建项目实体
        Project project = new Project(
                request.getName(),
                request.getDescription(),
                request.getType(),
                request.getVisibility(),
                currentUser
        );

        // 设置其他属性
        if (request.getBaseUrl() != null && !request.getBaseUrl().trim().isEmpty()) {
            project.setBaseUrl(request.getBaseUrl().trim());
        }

        if (request.getEnvironment() != null && !request.getEnvironment().trim().isEmpty()) {
            project.setEnvironment(request.getEnvironment().trim());
        } else {
            project.setEnvironment("development");
        }

        // 保存项目
        Project savedProject = projectRepository.save(project);

        // 初始化项目成员关系（项目所有者自动成为管理员）
        projectMemberService.initializeProjectOwner(savedProject, currentUser);

        // TODO: 记录项目创建日志

        return convertToResponse(savedProject);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id, User currentUser) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ProjectNotFoundException("项目不存在"));

        // 检查访问权限
        if (!hasProjectAccess(project, currentUser)) {
            throw new UnauthorizedAccessException("无权限访问此项目");
        }

        return convertToResponse(project);
    }

    @Override
    public ProjectResponse updateProject(Long id, UpdateProjectRequest request, User currentUser) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ProjectNotFoundException("项目不存在"));

        // 检查是否为项目所有者
        if (!isProjectOwner(project, currentUser)) {
            throw new UnauthorizedAccessException("只有项目所有者可以修改项目信息");
        }

        // 如果修改了项目名称，检查是否已存在
        if (!project.getName().equals(request.getName()) &&
                !isProjectNameAvailable(request.getName())) {
            throw new ProjectNameAlreadyExistsException("项目名称已存在");
        }

        // 更新项目信息
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setType(request.getType());
        project.setVisibility(request.getVisibility());

        if (request.getBaseUrl() != null) {
            project.setBaseUrl(request.getBaseUrl().trim());
        }

        if (request.getEnvironment() != null && !request.getEnvironment().trim().isEmpty()) {
            project.setEnvironment(request.getEnvironment().trim());
        }

        // 更新高级设置字段
        if (request.getMockDomain() != null) {
            project.setMockDomain(request.getMockDomain().trim());
        }

        if (request.getProxyEnabled() != null) {
            project.setProxyEnabled(request.getProxyEnabled());
        }

        if (request.getProxyTarget() != null) {
            project.setProxyTarget(request.getProxyTarget().trim());
        }

        if (request.getAllowCors() != null) {
            project.setAllowCors(request.getAllowCors());
        }

        if (request.getCorsOrigins() != null) {
            project.setCorsOrigins(request.getCorsOrigins().trim());
        }

        if (request.getRequestTimeout() != null) {
            project.setRequestTimeout(request.getRequestTimeout());
        }

        if (request.getRateLimit() != null) {
            project.setRateLimit(request.getRateLimit());
        }

        if (request.getEnableLogging() != null) {
            project.setEnableLogging(request.getEnableLogging());
        }

        if (request.getEnableCache() != null) {
            project.setEnableCache(request.getEnableCache());
        }

        if (request.getCacheTtl() != null) {
            project.setCacheTtl(request.getCacheTtl());
        }

        if (request.getProjectTags() != null) {
            project.setProjectTags(request.getProjectTags().trim());
        }

        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }

        // 保存更新
        Project updatedProject = projectRepository.save(project);

        // TODO: 记录项目修改日志

        return convertToResponse(updatedProject);
    }

    @Override
    public void deleteProject(Long id, User currentUser) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ProjectNotFoundException("项目不存在"));

        // 检查是否为项目所有者
        if (!isProjectOwner(project, currentUser)) {
            throw new UnauthorizedAccessException("只有项目所有者可以删除项目");
        }

        // 软删除项目
        project.markAsDeleted();
        projectRepository.save(project);

        // 清理项目成员关系
        projectMemberService.cleanupProjectMembers(project);

        // TODO: 清理项目相关数据（API、Mock规则等）
        // TODO: 记录项目删除日志
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponse> getAccessibleProjects(User currentUser, Pageable pageable) {
        Page<Project> projects = projectRepository.findAccessibleProjects(
                currentUser, ProjectVisibility.PUBLIC, pageable);
        return projects.map(this::convertToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponse> searchProjects(String keyword, User currentUser, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAccessibleProjects(currentUser, pageable);
        }

        Page<Project> projects = projectRepository.searchByKeyword(keyword.trim(), pageable);

        // Convert projects to responses (filter will be done at the repository level for better performance)
        return projects.map(this::convertToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponse> getUserProjects(User currentUser, Pageable pageable) {
        Page<Project> projects = projectRepository.findByOwnerAndDeletedAtIsNullOrderByUpdatedAtDesc(
                currentUser, pageable);
        return projects.map(this::convertToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProjectNameAvailable(String name) {
        return !projectRepository.existsByNameAndDeletedAtIsNull(name);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasProjectAccess(Long projectId, User user) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(projectId).orElse(null);
        return project != null && hasProjectAccess(project, user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProjectOwner(Long projectId, User user) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(projectId).orElse(null);
        return project != null && isProjectOwner(project, user);
    }

    @Override
    public void updateProjectStats(Long projectId) {
        // TODO: 实现项目统计信息更新
        // 1. 统计API数量
        // 2. 统计Mock规则数量
        // 3. 统计项目成员数量
    }

    // 私有辅助方法
    private boolean hasProjectAccess(Project project, User user) {
        // 检查是否为项目成员
        if (projectMemberService.isProjectMember(project, user)) {
            return true;
        }

        // 检查项目是否为公开项目
        return project.getVisibility() == ProjectVisibility.PUBLIC;
    }

    private boolean isProjectOwner(Project project, User user) {
        return projectMemberService.isProjectOwner(project, user);
    }

    private ProjectResponse convertToResponse(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setName(project.getName());
        response.setDescription(project.getDescription());
        response.setType(project.getType());
        response.setVisibility(project.getVisibility());
        response.setBaseUrl(project.getBaseUrl());
        response.setEnvironment(project.getEnvironment());
        response.setMockDomain(project.getMockDomain());
        response.setProxyEnabled(project.getProxyEnabled());
        response.setProxyTarget(project.getProxyTarget());
        response.setAllowCors(project.getAllowCors());
        response.setCorsOrigins(project.getCorsOrigins());
        response.setRequestTimeout(project.getRequestTimeout());
        response.setRateLimit(project.getRateLimit());
        response.setEnableLogging(project.getEnableLogging());
        response.setEnableCache(project.getEnableCache());
        response.setCacheTtl(project.getCacheTtl());
        response.setProjectTags(project.getProjectTags());
        response.setStatus(project.getStatus());
        response.setMemberCount(project.getMemberCount());
        response.setApiCount(project.getApiCount());
        response.setMockCount(project.getMockCount());
        response.setCreatedAt(project.getCreatedAt());
        response.setUpdatedAt(project.getUpdatedAt());

        // 设置所有者信息
        User owner = project.getOwner();
        if (owner != null) {
            ProjectResponse.OwnerInfo ownerInfo = new ProjectResponse.OwnerInfo(
                    owner.getId(), owner.getUsername(), owner.getEmail());
            response.setOwner(ownerInfo);
        }

        return response;
    }

    // ========== 项目设置相关方法实现 ==========

    @Override
    @Transactional(readOnly = true)
    public ProjectSettingsResponse getProjectSettings(Long projectId, User currentUser) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("项目不存在"));

        // 检查权限：只有项目成员可以查看设置
        if (!hasProjectAccess(project, currentUser)) {
            throw new UnauthorizedAccessException("没有权限访问该项目");
        }

        return new ProjectSettingsResponse(project);
    }

    @Override
    public ProjectSettingsResponse updateProjectBasicSettings(Long projectId, ProjectBasicSettingsRequest request, User currentUser) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("项目不存在"));

        // 检查权限：只有项目管理员和所有者可以修改基本设置
        if (!projectMemberService.hasPermission(project, currentUser, "project:write")) {
            throw new UnauthorizedAccessException("没有权限修改项目设置");
        }

        // 检查项目名称唯一性（排除当前项目）
        if (!request.getName().equals(project.getName()) && !isProjectNameAvailable(request.getName(), projectId)) {
            throw new ProjectNameAlreadyExistsException("项目名称已存在");
        }

        // 更新基本信息
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setType(ProjectType.valueOf(request.getType()));
        project.setVisibility(ProjectVisibility.valueOf(request.getVisibility()));
        project.setProjectTags(request.getProjectTags());

        project = projectRepository.save(project);
        return new ProjectSettingsResponse(project);
    }

    @Override
    public ProjectSettingsResponse updateProjectEnvironmentSettings(Long projectId, ProjectEnvironmentSettingsRequest request, User currentUser) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("项目不存在"));

        // 检查权限：只有项目管理员和所有者可以修改环境设置
        if (!projectMemberService.hasPermission(project, currentUser, "project:write")) {
            throw new UnauthorizedAccessException("没有权限修改项目设置");
        }

        // 更新环境配置
        project.setProjectEnvironment(ProjectEnvironment.fromCode(request.getEnvironment()));
        project.setBaseUrl(request.getBaseUrl());
        project.setMockDomain(request.getMockDomain());
        project.setProxyEnabled(request.getProxyEnabled());
        project.setProxyTarget(request.getProxyTarget());
        project.setAllowCors(request.getAllowCors());
        project.setCorsOrigins(request.getCorsOrigins());
        project.setRequestTimeout(request.getRequestTimeout());
        project.setRateLimit(request.getRateLimit());
        project.setEnableLogging(request.getEnableLogging());
        project.setEnableCache(request.getEnableCache());
        project.setCacheTtl(request.getCacheTtl());

        // 如果没有设置Mock域名，自动生成
        if (request.getMockDomain() == null || request.getMockDomain().trim().isEmpty()) {
            project.setMockDomain(generateMockDomain(projectId));
        }

        project = projectRepository.save(project);
        return new ProjectSettingsResponse(project);
    }

    @Override
    public boolean transferProjectOwnership(Long projectId, ProjectTransferRequest request, User currentUser) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("项目不存在"));

        // 检查权限：只有项目所有者可以转让
        if (!isProjectOwner(project, currentUser)) {
            throw new UnauthorizedAccessException("只有项目所有者可以转让项目");
        }

        // 验证当前用户密码
        if (!validateUserPassword(currentUser, request.getConfirmPassword())) {
            throw new UnauthorizedAccessException("密码验证失败");
        }

        // 查找新所有者
        User newOwner = userRepository.findById(request.getNewOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("新所有者不存在"));

        // 验证新所有者用户名
        if (!newOwner.getUsername().equals(request.getNewOwnerUsername())) {
            throw new IllegalArgumentException("用户名不匹配");
        }

        // 执行转让
        User oldOwner = project.getOwner();
        project.setOwner(newOwner);
        projectRepository.save(project);

        // 更新项目成员关系
        try {
            projectMemberService.transferOwnership(project, oldOwner, newOwner);
            return true;
        } catch (Exception e) {
            // 如果成员关系更新失败，回滚项目所有者
            project.setOwner(oldOwner);
            projectRepository.save(project);
            throw new RuntimeException("项目转让失败: " + e.getMessage());
        }
    }

    @Override
    public boolean permanentDeleteProject(Long projectId, String confirmPassword, User currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("项目不存在"));

        // 检查权限：只有项目所有者可以永久删除
        if (!isProjectOwner(project, currentUser)) {
            throw new UnauthorizedAccessException("只有项目所有者可以永久删除项目");
        }

        // 验证密码
        if (!validateUserPassword(currentUser, confirmPassword)) {
            throw new UnauthorizedAccessException("密码验证失败");
        }

        // 删除项目相关数据
        // TODO: 删除API、Mock规则、调用日志等相关数据

        // 删除项目成员关系
        projectMemberService.removeAllMembers(project);

        // 永久删除项目
        projectRepository.delete(project);
        return true;
    }

    @Override
    public boolean restoreProject(Long projectId, User currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("项目不存在"));

        // 检查是否为删除状态
        if (!project.isDeleted()) {
            throw new IllegalStateException("项目未被删除");
        }

        // 检查权限：只有项目所有者可以恢复
        if (!isProjectOwner(project, currentUser)) {
            throw new UnauthorizedAccessException("只有项目所有者可以恢复项目");
        }

        // 恢复项目
        project.setDeletedAt(null);
        project.setStatus(1);
        projectRepository.save(project);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProjectNameAvailable(String name, Long excludeProjectId) {
        if (excludeProjectId != null) {
            return !projectRepository.existsByNameAndIdNotAndDeletedAtIsNull(name, excludeProjectId);
        }
        return !projectRepository.existsByNameAndDeletedAtIsNull(name);
    }

    @Override
    public String generateMockDomain(Long projectId) {
        // 生成格式：mock-{projectId}.{domain}
        // 实际部署时可以配置具体的域名
        return String.format("mock-%d.apimock.local", projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateUserPassword(User user, String password) {
        return passwordEncoder.matches(password, user.getPassword());
    }
}