package com.apimock.service.api;

import com.apimock.dto.api.*;
import com.apimock.entity.auth.User;
import com.apimock.entity.api.ApiDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ApiService {

    ApiResponseDto createApi(CreateApiRequest request, User operator);

    ApiResponseDto updateApi(Long id, UpdateApiRequest request, User operator);

    void deleteApi(Long id, User operator);

    ApiResponseDto getApiById(Long id, User operator);

    Page<ApiResponseDto> getProjectApis(Long projectId, String method, String keyword, User operator, Pageable pageable);

    Page<ApiResponseDto> getGroupApis(Long groupId, User operator, Pageable pageable);

    boolean isApiPathAndMethodUnique(Long projectId, String path, String method, Long excludeId);

    void batchDeleteApis(List<Long> apiIds, User operator);

    void batchMoveApisToGroup(List<Long> apiIds, Long groupId, User operator);

    void updateApiSortOrder(Long id, Integer sortOrder, User operator);

    ApiResponseDto copyApi(Long id, String newName, User operator);

    // 新增方法：根据项目ID、路径和方法查找API（用于代理服务）
    Optional<ApiDefinition> findByProjectIdAndPath(Long projectId, String path, String method);

    Object getApiStatistics(Long projectId, User operator);

    List<ApiVersionDto> listVersions(Long apiId, User operator);

    ApiVersionDetailDto getVersionDetail(Long apiId, Integer versionNumber, User operator);

    ApiResponseDto restoreVersion(Long apiId, Integer versionNumber, String changeSummary, User operator);

    ApiVersionCompareResponse compareVersions(Long apiId, Integer version1, Integer version2, User operator);
}
