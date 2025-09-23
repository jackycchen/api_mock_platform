package com.apimock.repository.proxy;

import com.apimock.entity.proxy.ProxyConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProxyConfigRepository extends JpaRepository<ProxyConfig, Long> {

    List<ProxyConfig> findByProjectIdOrderByCreatedAtAsc(Long projectId);

    boolean existsByProjectIdAndPathPattern(Long projectId, String pathPattern);

    boolean existsByProjectIdAndPathPatternAndIdNot(Long projectId, String pathPattern, Long excludeId);

    Optional<ProxyConfig> findByIdAndProjectId(Long id, Long projectId);
}
