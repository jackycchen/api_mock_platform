package com.apimock.repository.project;

import com.apimock.entity.auth.User;
import com.apimock.entity.project.Project;
import com.apimock.entity.project.ProjectMember;
import com.apimock.entity.project.ProjectRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 项目成员数据访问层
 *
 * @author AI Assistant
 */
@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    // 基础查询方法
    Optional<ProjectMember> findByProjectAndUserAndRemovedAtIsNull(Project project, User user);

    List<ProjectMember> findByProjectAndRemovedAtIsNullOrderByJoinedAtAsc(Project project);

    Page<ProjectMember> findByProjectAndRemovedAtIsNullOrderByJoinedAtAsc(Project project, Pageable pageable);

    List<ProjectMember> findByUserAndRemovedAtIsNullOrderByJoinedAtDesc(User user);

    // 检查用户是否为项目成员
    boolean existsByProjectAndUserAndRemovedAtIsNull(Project project, User user);

    // 获取用户在项目中的角色
    @Query("SELECT pm.role FROM ProjectMember pm WHERE pm.project = :project AND pm.user = :user AND pm.removedAt IS NULL")
    Optional<ProjectRole> findUserRoleInProject(@Param("project") Project project, @Param("user") User user);

    // 检查用户是否有指定权限
    @Query("SELECT COUNT(pm) > 0 FROM ProjectMember pm WHERE pm.project.id = :projectId AND pm.user.id = :userId " +
           "AND pm.removedAt IS NULL AND pm.role IN :roles")
    boolean hasUserPermissionInProject(@Param("projectId") Long projectId, @Param("userId") Long userId,
                                     @Param("roles") List<ProjectRole> roles);

    // 按角色查询项目成员
    List<ProjectMember> findByProjectAndRoleAndRemovedAtIsNullOrderByJoinedAtAsc(Project project, ProjectRole role);

    // 获取项目所有者
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project = :project AND pm.role = 'OWNER' AND pm.removedAt IS NULL")
    Optional<ProjectMember> findProjectOwner(@Param("project") Project project);

    // 统计项目成员数量
    long countByProjectAndRemovedAtIsNull(Project project);

    // 统计用户参与的项目数量
    long countByUserAndRemovedAtIsNull(User user);

    // 按角色统计项目成员
    @Query("SELECT pm.role, COUNT(pm) FROM ProjectMember pm WHERE pm.project = :project AND pm.removedAt IS NULL GROUP BY pm.role")
    List<Object[]> countMembersByRole(@Param("project") Project project);

    // 搜索项目成员
    @Query("SELECT pm FROM ProjectMember pm JOIN pm.user u WHERE pm.project = :project AND pm.removedAt IS NULL " +
           "AND (u.username LIKE %:keyword% OR u.email LIKE %:keyword%) ORDER BY pm.joinedAt ASC")
    Page<ProjectMember> searchMembers(@Param("project") Project project, @Param("keyword") String keyword, Pageable pageable);

    // 软删除成员
    @Modifying
    @Query("UPDATE ProjectMember pm SET pm.removedAt = :removedAt, pm.status = 0 WHERE pm.id = :id AND pm.removedAt IS NULL")
    void softDeleteById(@Param("id") Long id, @Param("removedAt") LocalDateTime removedAt);

    // 批量软删除成员
    @Modifying
    @Query("UPDATE ProjectMember pm SET pm.removedAt = :removedAt, pm.status = 0 " +
           "WHERE pm.id IN :ids AND pm.removedAt IS NULL")
    void softDeleteByIds(@Param("ids") List<Long> ids, @Param("removedAt") LocalDateTime removedAt);

    // 更新成员角色
    @Modifying
    @Query("UPDATE ProjectMember pm SET pm.role = :role WHERE pm.id = :id AND pm.removedAt IS NULL")
    void updateMemberRole(@Param("id") Long id, @Param("role") ProjectRole role);

    // 删除项目的所有成员
    @Modifying
    @Query("UPDATE ProjectMember pm SET pm.removedAt = :removedAt, pm.status = 0 " +
           "WHERE pm.project = :project AND pm.removedAt IS NULL")
    void softDeleteByProject(@Param("project") Project project, @Param("removedAt") LocalDateTime removedAt);

    // 获取用户有权限的项目列表
    @Query("SELECT DISTINCT pm.project FROM ProjectMember pm WHERE pm.user = :user AND pm.removedAt IS NULL")
    List<Project> findUserProjects(@Param("user") User user);

    // 分页获取用户有权限的项目
    @Query("SELECT DISTINCT pm.project FROM ProjectMember pm WHERE pm.user = :user AND pm.removedAt IS NULL")
    Page<Project> findUserProjectsPaged(@Param("user") User user, Pageable pageable);

    // 检查用户是否为项目管理员（所有者或管理员）
    @Query("SELECT COUNT(pm) > 0 FROM ProjectMember pm WHERE pm.project.id = :projectId AND pm.user.id = :userId " +
           "AND pm.removedAt IS NULL AND pm.role IN ('OWNER', 'ADMIN')")
    boolean isProjectAdmin(@Param("projectId") Long projectId, @Param("userId") Long userId);

    // 检查用户是否为项目所有者
    @Query("SELECT COUNT(pm) > 0 FROM ProjectMember pm WHERE pm.project.id = :projectId AND pm.user.id = :userId " +
           "AND pm.removedAt IS NULL AND pm.role = 'OWNER'")
    boolean isProjectOwner(@Param("projectId") Long projectId, @Param("userId") Long userId);

    // 根据项目ID和用户ID查找成员关系
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project.id = :projectId AND pm.user.id = :userId " +
           "AND pm.removedAt IS NULL")
    Optional<ProjectMember> findByProjectIdAndUserIdAndRemovedAtIsNull(@Param("projectId") Long projectId,
                                                                       @Param("userId") Long userId);
}