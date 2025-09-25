package com.apimock.repository.api;

import com.apimock.entity.api.ApiGroup;
import com.apimock.entity.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiGroupRepository extends JpaRepository<ApiGroup, Long> {

    List<ApiGroup> findByProjectOrderBySortOrderAsc(Project project);

    Optional<ApiGroup> findByIdAndProject(Long id, Project project);

    boolean existsByProjectAndName(Project project, String name);
}
