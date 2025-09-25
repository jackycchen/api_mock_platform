package com.apimock.repository.api;

import com.apimock.entity.api.ApiDefinition;
import com.apimock.entity.api.ApiDefinitionVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiDefinitionVersionRepository extends JpaRepository<ApiDefinitionVersion, Long> {

    List<ApiDefinitionVersion> findByApiOrderByVersionNumberDesc(ApiDefinition api);

    Optional<ApiDefinitionVersion> findFirstByApiOrderByVersionNumberDesc(ApiDefinition api);

    Optional<ApiDefinitionVersion> findByApiAndVersionNumber(ApiDefinition api, Integer versionNumber);

    void deleteByApi(ApiDefinition api);
}
