package com.apimock.repository;

import com.apimock.entity.project.Project;
import com.apimock.entity.project.ProjectVisibility;
import com.apimock.entity.auth.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // 基础查询方法
    Optional<Project> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByNameAndDeletedAtIsNull(String name);

    List<Project> findByOwnerAndDeletedAtIsNullOrderByUpdatedAtDesc(User owner);

    Page<Project> findByOwnerAndDeletedAtIsNullOrderByUpdatedAtDesc(User owner, Pageable pageable);

    // 分页查询活跃项目
    Page<Project> findByDeletedAtIsNullOrderByUpdatedAtDesc(Pageable pageable);

    // 根据名称搜索项目（支持模糊匹配）
    @Query("SELECT p FROM Project p WHERE p.deletedAt IS NULL " +
           "AND (p.name LIKE %:keyword% OR p.description LIKE %:keyword%) " +
           "ORDER BY p.updatedAt DESC")
    Page<Project> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 根据用户获取有权限的项目
    @Query("SELECT p FROM Project p WHERE p.deletedAt IS NULL " +
           "AND (p.owner = :user OR p.visibility = :publicVisibility) " +
           "ORDER BY p.updatedAt DESC")
    Page<Project> findAccessibleProjects(@Param("user") User user,
                                       @Param("publicVisibility") ProjectVisibility publicVisibility,
                                       Pageable pageable);

    // 根据可见性查询项目
    Page<Project> findByVisibilityAndDeletedAtIsNullOrderByUpdatedAtDesc(
            ProjectVisibility visibility, Pageable pageable);

    // 统计用户创建的项目数量
    long countByOwnerAndDeletedAtIsNull(User owner);

    // 查询最近创建的项目
    @Query("SELECT p FROM Project p WHERE p.deletedAt IS NULL " +
           "AND p.createdAt >= :since ORDER BY p.createdAt DESC")
    List<Project> findRecentProjects(@Param("since") LocalDateTime since);

    // 批量软删除
    @Query("UPDATE Project p SET p.deletedAt = :deletedAt, p.status = 0 " +
           "WHERE p.id IN :ids AND p.deletedAt IS NULL")
    void softDeleteByIds(@Param("ids") List<Long> ids, @Param("deletedAt") LocalDateTime deletedAt);

    // 更新项目统计信息
    @Query("UPDATE Project p SET p.apiCount = :apiCount WHERE p.id = :projectId")
    void updateApiCount(@Param("projectId") Long projectId, @Param("apiCount") Integer apiCount);

    @Query("UPDATE Project p SET p.mockCount = :mockCount WHERE p.id = :projectId")
    void updateMockCount(@Param("projectId") Long projectId, @Param("mockCount") Integer mockCount);

    @Query("UPDATE Project p SET p.memberCount = :memberCount WHERE p.id = :projectId")
    void updateMemberCount(@Param("projectId") Long projectId, @Param("memberCount") Integer memberCount);

    // 项目设置相关查询方法
    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long excludeId);
}