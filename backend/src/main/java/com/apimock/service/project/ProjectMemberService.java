package com.apimock.service.project;

import com.apimock.entity.auth.User;
import com.apimock.entity.project.Project;
import com.apimock.entity.project.ProjectMember;
import com.apimock.entity.project.ProjectRole;
import com.apimock.repository.project.ProjectMemberRepository;
import com.apimock.service.auth.PermissionService;
import com.apimock.service.auth.UserOperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 项目成员服务实现
 * 处理项目成员管理相关业务逻辑
 *
 * @author AI Assistant
 */
@Service
@Transactional
public class ProjectMemberService {

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private UserOperationLogService operationLogService;

    /**
     * 初始化项目所有者成员关系
     * 在创建项目时调用，将创建者设置为项目所有者
     */
    public ProjectMember initializeProjectOwner(Project project, User owner) {
        // 检查是否已经存在成员关系
        Optional<ProjectMember> existingMember = projectMemberRepository
                .findByProjectAndUserAndRemovedAtIsNull(project, owner);

        if (existingMember.isPresent()) {
            // 如果已存在，更新角色为所有者
            ProjectMember member = existingMember.get();
            member.setRole(ProjectRole.OWNER);
            return projectMemberRepository.save(member);
        }

        // 创建新的成员关系
        ProjectMember projectMember = new ProjectMember(project, owner, ProjectRole.OWNER);
        return projectMemberRepository.save(projectMember);
    }

    /**
     * 添加项目成员
     */
    public ProjectMember addMember(Project project, User user, ProjectRole role) {
        // 检查用户是否已经是项目成员
        if (projectMemberRepository.existsByProjectAndUserAndRemovedAtIsNull(project, user)) {
            throw new RuntimeException("用户已经是项目成员");
        }

        ProjectMember projectMember = new ProjectMember(project, user, role);
        return projectMemberRepository.save(projectMember);
    }

    /**
     * 移除项目成员
     */
    public void removeMember(Project project, User user) {
        Optional<ProjectMember> memberOpt = projectMemberRepository
                .findByProjectAndUserAndRemovedAtIsNull(project, user);

        if (memberOpt.isPresent()) {
            ProjectMember member = memberOpt.get();
            if (member.getRole() == ProjectRole.OWNER) {
                throw new RuntimeException("不能移除项目所有者");
            }
            member.markAsRemoved();
            projectMemberRepository.save(member);
        }
    }

    /**
     * 更新成员角色
     */
    public ProjectMember updateMemberRole(Project project, User user, ProjectRole newRole) {
        Optional<ProjectMember> memberOpt = projectMemberRepository
                .findByProjectAndUserAndRemovedAtIsNull(project, user);

        if (memberOpt.isEmpty()) {
            throw new RuntimeException("用户不是项目成员");
        }

        ProjectMember member = memberOpt.get();

        if (member.getRole() == ProjectRole.OWNER && newRole != ProjectRole.OWNER) {
            throw new RuntimeException("不能修改项目所有者的角色");
        }

        member.setRole(newRole);
        return projectMemberRepository.save(member);
    }

    /**
     * 获取项目成员列表
     */
    @Transactional(readOnly = true)
    public List<ProjectMember> getProjectMembers(Project project) {
        return projectMemberRepository.findByProjectAndRemovedAtIsNullOrderByJoinedAtAsc(project);
    }

    /**
     * 获取用户在项目中的角色
     */
    @Transactional(readOnly = true)
    public Optional<ProjectRole> getUserRoleInProject(Project project, User user) {
        return projectMemberRepository.findUserRoleInProject(project, user);
    }

    /**
     * 检查用户是否为项目成员
     */
    @Transactional(readOnly = true)
    public boolean isProjectMember(Project project, User user) {
        return projectMemberRepository.existsByProjectAndUserAndRemovedAtIsNull(project, user);
    }

    /**
     * 检查用户是否为项目所有者
     */
    @Transactional(readOnly = true)
    public boolean isProjectOwner(Project project, User user) {
        return projectMemberRepository.isProjectOwner(project.getId(), user.getId());
    }

    /**
     * 检查用户是否为项目管理员（所有者或管理员）
     */
    @Transactional(readOnly = true)
    public boolean isProjectAdmin(Project project, User user) {
        return projectMemberRepository.isProjectAdmin(project.getId(), user.getId());
    }

    /**
     * 检查用户是否有指定权限
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(Project project, User user, String permission) {
        Optional<ProjectRole> roleOpt = getUserRoleInProject(project, user);
        if (roleOpt.isEmpty()) {
            return false;
        }
        return permissionService.hasPermission(roleOpt.get(), permission);
    }

    /**
     * 检查用户是否有任意一个权限
     */
    @Transactional(readOnly = true)
    public boolean hasAnyPermission(Project project, User user, String... permissions) {
        Optional<ProjectRole> roleOpt = getUserRoleInProject(project, user);
        if (roleOpt.isEmpty()) {
            return false;
        }
        return permissionService.hasAnyPermission(roleOpt.get(), permissions);
    }

    /**
     * 检查用户是否有所有权限
     */
    @Transactional(readOnly = true)
    public boolean hasAllPermissions(Project project, User user, String... permissions) {
        Optional<ProjectRole> roleOpt = getUserRoleInProject(project, user);
        if (roleOpt.isEmpty()) {
            return false;
        }
        return permissionService.hasAllPermissions(roleOpt.get(), permissions);
    }

    /**
     * 获取项目成员的详细信息（包括权限）
     */
    @Transactional(readOnly = true)
    public Optional<ProjectMember> getProjectMember(Long projectId, Long userId) {
        return projectMemberRepository.findByProjectIdAndUserIdAndRemovedAtIsNull(projectId, userId);
    }

    /**
     * 添加项目成员（带权限检查和日志记录）
     */
    public ProjectMember addMemberWithPermissionCheck(Project project, User user, ProjectRole role, User operator) {
        // 检查操作者权限
        if (!hasPermission(project, operator, "member:invite")) {
            throw new RuntimeException("您没有邀请成员的权限");
        }

        // 检查角色层级
        Optional<ProjectRole> operatorRoleOpt = getUserRoleInProject(project, operator);
        if (operatorRoleOpt.isPresent()) {
            ProjectRole operatorRole = operatorRoleOpt.get();
            if (!operatorRole.canManageRole(role)) {
                throw new RuntimeException("您不能邀请比您权限更高的成员");
            }
        }

        // 添加成员
        ProjectMember member = addMember(project, user, role);

        // 记录操作日志
        operationLogService.logResourceOperation(
            operator.getId(),
            operator.getUsername(),
            UserOperationLogService.Operations.INVITE_MEMBER,
            "project",
            project.getId(),
            String.format("邀请用户 %s 加入项目，角色：%s", user.getUsername(), role.getDisplayName())
        );

        return member;
    }

    /**
     * 移除项目成员（带权限检查和日志记录）
     */
    public void removeMemberWithPermissionCheck(Project project, User user, User operator) {
        // 检查操作者权限
        if (!hasPermission(project, operator, "member:remove")) {
            throw new RuntimeException("您没有移除成员的权限");
        }

        // 检查是否试图移除自己
        if (user.getId().equals(operator.getId())) {
            throw new RuntimeException("您不能移除自己");
        }

        // 检查角色层级
        Optional<ProjectRole> operatorRoleOpt = getUserRoleInProject(project, operator);
        Optional<ProjectRole> targetRoleOpt = getUserRoleInProject(project, user);

        if (operatorRoleOpt.isPresent() && targetRoleOpt.isPresent()) {
            ProjectRole operatorRole = operatorRoleOpt.get();
            ProjectRole targetRole = targetRoleOpt.get();
            if (!operatorRole.canManageRole(targetRole)) {
                throw new RuntimeException("您不能移除比您权限更高的成员");
            }
        }

        // 移除成员
        removeMember(project, user);

        // 记录操作日志
        operationLogService.logResourceOperation(
            operator.getId(),
            operator.getUsername(),
            UserOperationLogService.Operations.REMOVE_MEMBER,
            "project",
            project.getId(),
            String.format("移除用户 %s 从项目", user.getUsername())
        );
    }

    /**
     * 更新成员角色（带权限检查和日志记录）
     */
    public ProjectMember updateMemberRoleWithPermissionCheck(Project project, User user, ProjectRole newRole, User operator) {
        // 检查操作者权限
        if (!hasPermission(project, operator, "member:role")) {
            throw new RuntimeException("您没有修改成员角色的权限");
        }

        // 检查是否试图修改自己的角色
        if (user.getId().equals(operator.getId())) {
            throw new RuntimeException("您不能修改自己的角色");
        }

        // 检查角色层级
        Optional<ProjectRole> operatorRoleOpt = getUserRoleInProject(project, operator);
        Optional<ProjectRole> currentRoleOpt = getUserRoleInProject(project, user);

        if (operatorRoleOpt.isPresent()) {
            ProjectRole operatorRole = operatorRoleOpt.get();

            // 检查是否能管理目标用户的当前角色
            if (currentRoleOpt.isPresent() && !operatorRole.canManageRole(currentRoleOpt.get())) {
                throw new RuntimeException("您不能修改比您权限更高的成员角色");
            }

            // 检查是否能分配新角色
            if (!operatorRole.canManageRole(newRole)) {
                throw new RuntimeException("您不能分配比您权限更高的角色");
            }
        }

        // 更新角色
        ProjectMember member = updateMemberRole(project, user, newRole);

        // 记录操作日志
        String oldRoleName = currentRoleOpt.map(ProjectRole::getDisplayName).orElse("未知");
        operationLogService.logResourceOperation(
            operator.getId(),
            operator.getUsername(),
            UserOperationLogService.Operations.CHANGE_ROLE,
            "project",
            project.getId(),
            String.format("修改用户 %s 的角色：%s -> %s", user.getUsername(), oldRoleName, newRole.getDisplayName())
        );

        return member;
    }

    /**
     * 获取项目成员数量
     */
    @Transactional(readOnly = true)
    public long getProjectMemberCount(Project project) {
        return projectMemberRepository.countByProjectAndRemovedAtIsNull(project);
    }

    /**
     * 获取用户参与的项目列表
     */
    @Transactional(readOnly = true)
    public List<Project> getUserProjects(User user) {
        return projectMemberRepository.findUserProjects(user);
    }

    /**
     * 转移项目所有权
     */
    public void transferOwnership(Project project, User currentOwner, User newOwner) {
        // 检查当前用户是否为所有者
        if (!isProjectOwner(project, currentOwner)) {
            throw new RuntimeException("只有项目所有者可以转移所有权");
        }

        // 检查新所有者是否为项目成员
        Optional<ProjectMember> newOwnerMemberOpt = projectMemberRepository
                .findByProjectAndUserAndRemovedAtIsNull(project, newOwner);

        if (newOwnerMemberOpt.isEmpty()) {
            // 如果新所有者不是项目成员，先添加为成员
            addMember(project, newOwner, ProjectRole.OWNER);
        } else {
            // 如果已是成员，更新角色为所有者
            ProjectMember newOwnerMember = newOwnerMemberOpt.get();
            newOwnerMember.setRole(ProjectRole.OWNER);
            projectMemberRepository.save(newOwnerMember);
        }

        // 将当前所有者角色改为管理员
        Optional<ProjectMember> currentOwnerMemberOpt = projectMemberRepository
                .findByProjectAndUserAndRemovedAtIsNull(project, currentOwner);

        if (currentOwnerMemberOpt.isPresent()) {
            ProjectMember currentOwnerMember = currentOwnerMemberOpt.get();
            currentOwnerMember.setRole(ProjectRole.ADMIN);
            projectMemberRepository.save(currentOwnerMember);
        }
    }

    /**
     * 清理项目成员（项目删除时调用）
     */
    public void cleanupProjectMembers(Project project) {
        projectMemberRepository.softDeleteByProject(project, LocalDateTime.now());
    }

    /**
     * 移除项目的所有成员（项目删除时调用）
     */
    public void removeAllMembers(Project project) {
        cleanupProjectMembers(project);
    }
}