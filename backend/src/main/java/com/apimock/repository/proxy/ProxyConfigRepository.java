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

    // 新增方法：获取所有启用的代理配置
    List<ProxyConfig> findByEnabledTrueOrderByCreatedAtAsc();

    // 新增方法：获取指定项目的启用代理配置
    List<ProxyConfig> findByProjectIdAndEnabledTrueOrderByCreatedAtAsc(Long projectId);
}
