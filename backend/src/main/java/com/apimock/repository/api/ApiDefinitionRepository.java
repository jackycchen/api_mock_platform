package com.apimock.repository.api;

import com.apimock.entity.api.ApiDefinition;
import com.apimock.entity.api.ApiGroup;
import com.apimock.entity.project.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiDefinitionRepository extends JpaRepository<ApiDefinition, Long> {

    Optional<ApiDefinition> findByIdAndDeletedAtIsNull(Long id);

    Page<ApiDefinition> findByProjectAndDeletedAtIsNull(Project project, Pageable pageable);

    Page<ApiDefinition> findByProjectAndDeletedAtIsNullAndMethodIgnoreCase(Project project, String method, Pageable pageable);

    Page<ApiDefinition> findByProjectAndGroupAndDeletedAtIsNull(Project project, ApiGroup group, Pageable pageable);

    boolean existsByProjectAndMethodIgnoreCaseAndPathIgnoreCaseAndDeletedAtIsNull(Project project, String method, String path);

    boolean existsByProjectAndMethodIgnoreCaseAndPathIgnoreCaseAndIdNotAndDeletedAtIsNull(Project project, String method, String path, Long excludeId);

    @Query("SELECT a FROM ApiDefinition a WHERE a.project = :project AND a.deletedAt IS NULL " +
            "AND (LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(a.path) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%')))" )
    Page<ApiDefinition> searchByKeyword(@Param("project") Project project,
                                        @Param("keyword") String keyword,
                                        Pageable pageable);

    @Query("SELECT COUNT(a) FROM ApiDefinition a WHERE a.project = :project AND a.deletedAt IS NULL")
    long countByProject(@Param("project") Project project);

    List<ApiDefinition> findByIdInAndDeletedAtIsNull(List<Long> ids);

    // 新增方法：根据项目ID、路径和方法查找API（用于代理服务）
    Optional<ApiDefinition> findByProjectIdAndPathIgnoreCaseAndMethodIgnoreCaseAndDeletedAtIsNull(Long projectId, String path, String method);
}
