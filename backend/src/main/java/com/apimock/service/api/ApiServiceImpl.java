package com.apimock.service.api;

import com.apimock.dto.api.*;
import com.apimock.entity.api.ApiDefinition;
import com.apimock.entity.api.ApiDefinitionVersion;
import com.apimock.entity.api.ApiGroup;
import com.apimock.entity.auth.User;
import com.apimock.entity.project.Project;
import com.apimock.exception.*;
import com.apimock.repository.ProjectRepository;
import com.apimock.repository.api.ApiDefinitionRepository;
import com.apimock.repository.api.ApiDefinitionVersionRepository;
import com.apimock.repository.api.ApiGroupRepository;
import com.apimock.service.project.ProjectMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ApiServiceImpl implements ApiService {

    private static final String PERMISSION_API_ADMIN = "api:admin";
    private static final String PERMISSION_API_READ = "api:read";

    @Autowired
    private ApiDefinitionRepository apiDefinitionRepository;

    @Autowired
    private ApiDefinitionVersionRepository apiDefinitionVersionRepository;

    @Autowired
    private ApiGroupRepository apiGroupRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMemberService projectMemberService;

    @Override
    public ApiResponseDto createApi(CreateApiRequest request, User operator) {
        Project project = loadProject(request.getProjectId());
        ensurePermission(project, operator, PERMISSION_API_ADMIN);

        if (apiDefinitionRepository.existsByProjectAndMethodIgnoreCaseAndPathIgnoreCaseAndDeletedAtIsNull(
                project, request.getMethod(), request.getPath())) {
            throw new ApiPathConflictException("接口路径与方法已存在");
        }

        ApiGroup group = resolveGroup(project, request.getGroupId());

        ApiDefinition definition = new ApiDefinition();
        definition.setProject(project);
        definition.setGroup(group);
        definition.setName(request.getName());
        definition.setMethod(request.getMethod());
        definition.setPath(request.getPath());
        definition.setDescription(request.getDescription());
        definition.setStatus(Optional.ofNullable(request.getStatus()).orElse(1));
        definition.setSortOrder(Optional.ofNullable(request.getSortOrder()).orElse(0));
        definition.setRequestHeaders(request.getRequestHeaders());
        definition.setPathParameters(request.getPathParameters());
        definition.setQueryParameters(request.getQueryParameters());
        definition.setRequestBodySchema(request.getRequestBodySchema());
        definition.setResponseSchema(request.getResponseSchema());
        definition.setResponseExamples(request.getResponseExamples());
        definition.setMetadata(request.getMetadata());
        definition.setCreatedBy(operator);
        definition.setUpdatedBy(operator);
        ApiDefinition saved = apiDefinitionRepository.save(definition);

        createVersionSnapshot(saved, 1, request.getChangeSummary(), request.getMetadata(), operator);

        updateProjectApiCount(project);

        return convertToDto(saved);
    }

    @Override
    public ApiResponseDto updateApi(Long id, UpdateApiRequest request, User operator) {
        ApiDefinition definition = loadApi(id);
        Project project = definition.getProject();
        ensurePermission(project, operator, PERMISSION_API_ADMIN);

        boolean pathChanged = !definition.getPath().equalsIgnoreCase(request.getPath()) ||
                !definition.getMethod().equalsIgnoreCase(request.getMethod());
        if (pathChanged && apiDefinitionRepository
                .existsByProjectAndMethodIgnoreCaseAndPathIgnoreCaseAndIdNotAndDeletedAtIsNull(
                        project, request.getMethod(), request.getPath(), definition.getId())) {
            throw new ApiPathConflictException("接口路径与方法已存在");
        }

        ApiGroup group = resolveGroup(project, request.getGroupId());

        definition.setGroup(group);
        definition.setName(request.getName());
        definition.setMethod(request.getMethod());
        definition.setPath(request.getPath());
        definition.setDescription(request.getDescription());
        definition.setStatus(Optional.ofNullable(request.getStatus()).orElse(1));
        definition.setSortOrder(Optional.ofNullable(request.getSortOrder()).orElse(definition.getSortOrder()));
        definition.setRequestHeaders(request.getRequestHeaders());
        definition.setPathParameters(request.getPathParameters());
        definition.setQueryParameters(request.getQueryParameters());
        definition.setRequestBodySchema(request.getRequestBodySchema());
        definition.setResponseSchema(request.getResponseSchema());
        definition.setResponseExamples(request.getResponseExamples());
        definition.setMetadata(request.getMetadata());
        definition.setUpdatedBy(operator);
        definition.setLatestVersion(definition.getLatestVersion() + 1);
        definition.setVersionCount(definition.getVersionCount() + 1);

        ApiDefinition updated = apiDefinitionRepository.save(definition);

        createVersionSnapshot(updated, updated.getLatestVersion(), request.getChangeSummary(), request.getMetadata(), operator);

        return convertToDto(updated);
    }

    @Override
    public void deleteApi(Long id, User operator) {
        ApiDefinition definition = loadApi(id);
        Project project = definition.getProject();
        ensurePermission(project, operator, PERMISSION_API_ADMIN);

        if (definition.isDeleted()) {
            return;
        }

        definition.markAsDeleted();
        definition.setUpdatedBy(operator);
        apiDefinitionRepository.save(definition);

        updateProjectApiCount(project);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto getApiById(Long id, User operator) {
        ApiDefinition definition = loadApi(id);
        ensureReadPermission(definition.getProject(), operator);
        return convertToDto(definition);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApiResponseDto> getProjectApis(Long projectId, String method, String keyword, User operator, Pageable pageable) {
        Project project = loadProject(projectId);
        ensureReadPermission(project, operator);

        Page<ApiDefinition> page;
        if (keyword != null && !keyword.isBlank()) {
            page = apiDefinitionRepository.searchByKeyword(project, keyword.trim(), pageable);
        } else if (method != null && !method.isBlank()) {
            page = apiDefinitionRepository.findByProjectAndDeletedAtIsNullAndMethodIgnoreCase(project, method.trim(), pageable);
        } else {
            page = apiDefinitionRepository.findByProjectAndDeletedAtIsNull(project, pageable);
        }

        List<ApiResponseDto> content = page.stream().map(this::convertToDto).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApiResponseDto> getGroupApis(Long groupId, User operator, Pageable pageable) {
        ApiGroup group = apiGroupRepository.findById(groupId)
                .orElseThrow(() -> new InvalidRequestException("API分组不存在"));
        ensureReadPermission(group.getProject(), operator);

        Page<ApiDefinition> page = apiDefinitionRepository.findByProjectAndGroupAndDeletedAtIsNull(
                group.getProject(), group, pageable);
        List<ApiResponseDto> content = page.stream().map(this::convertToDto).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isApiPathAndMethodUnique(Long projectId, String path, String method, Long excludeId) {
        Project project = loadProject(projectId);
        if (excludeId != null) {
            return !apiDefinitionRepository
                    .existsByProjectAndMethodIgnoreCaseAndPathIgnoreCaseAndIdNotAndDeletedAtIsNull(project, method, path, excludeId);
        }
        return !apiDefinitionRepository
                .existsByProjectAndMethodIgnoreCaseAndPathIgnoreCaseAndDeletedAtIsNull(project, method, path);
    }

    @Override
    public void batchDeleteApis(List<Long> apiIds, User operator) {
        if (apiIds == null || apiIds.isEmpty()) {
            return;
        }
        List<ApiDefinition> apis = apiDefinitionRepository.findByIdInAndDeletedAtIsNull(apiIds);
        for (ApiDefinition api : apis) {
            ensurePermission(api.getProject(), operator, PERMISSION_API_ADMIN);
            api.markAsDeleted();
            api.setUpdatedBy(operator);
        }
        apiDefinitionRepository.saveAll(apis);

        apis.stream()
                .map(ApiDefinition::getProject)
                .distinct()
                .forEach(this::updateProjectApiCount);
    }

    @Override
    public void batchMoveApisToGroup(List<Long> apiIds, Long groupId, User operator) {
        if (apiIds == null || apiIds.isEmpty()) {
            return;
        }
        ApiGroup targetGroup = null;
        Project project = null;
        if (groupId != null) {
            targetGroup = apiGroupRepository.findById(groupId)
                    .orElseThrow(() -> new InvalidRequestException("目标分组不存在"));
            project = targetGroup.getProject();
        }

        List<ApiDefinition> apis = apiDefinitionRepository.findByIdInAndDeletedAtIsNull(apiIds);
        for (ApiDefinition api : apis) {
            if (project == null) {
                project = api.getProject();
            }
            ensurePermission(api.getProject(), operator, PERMISSION_API_ADMIN);
            if (targetGroup != null && !api.getProject().getId().equals(targetGroup.getProject().getId())) {
                throw new InvalidRequestException("分组与API不属于同一项目");
            }
            api.setGroup(targetGroup);
            api.setUpdatedBy(operator);
        }
        apiDefinitionRepository.saveAll(apis);
    }

    @Override
    public void updateApiSortOrder(Long id, Integer sortOrder, User operator) {
        ApiDefinition api = loadApi(id);
        ensurePermission(api.getProject(), operator, PERMISSION_API_ADMIN);
        api.setSortOrder(Optional.ofNullable(sortOrder).orElse(0));
        api.setUpdatedBy(operator);
        apiDefinitionRepository.save(api);
    }

    @Override
    public ApiResponseDto copyApi(Long id, String newName, User operator) {
        ApiDefinition source = loadApi(id);
        Project project = source.getProject();
        ensurePermission(project, operator, PERMISSION_API_ADMIN);

        String targetName = (newName == null || newName.isBlank()) ? source.getName() + " Copy" : newName.trim();
        String candidatePath = source.getPath();
        String method = source.getMethod();
        int attempt = 1;
        while (apiDefinitionRepository.existsByProjectAndMethodIgnoreCaseAndPathIgnoreCaseAndDeletedAtIsNull(
                project, method, candidatePath)) {
            candidatePath = source.getPath() + "-copy" + attempt;
            attempt++;
        }

        ApiDefinition copy = new ApiDefinition();
        copy.setProject(project);
        copy.setGroup(source.getGroup());
        copy.setName(targetName);
        copy.setMethod(method);
        copy.setPath(candidatePath);
        copy.setDescription(source.getDescription());
        copy.setStatus(source.getStatus());
        copy.setSortOrder(source.getSortOrder());
        copy.setRequestHeaders(source.getRequestHeaders());
        copy.setPathParameters(source.getPathParameters());
        copy.setQueryParameters(source.getQueryParameters());
        copy.setRequestBodySchema(source.getRequestBodySchema());
        copy.setResponseSchema(source.getResponseSchema());
        copy.setResponseExamples(source.getResponseExamples());
        copy.setMetadata(source.getMetadata());
        copy.setCreatedBy(operator);
        copy.setUpdatedBy(operator);

        ApiDefinition savedCopy = apiDefinitionRepository.save(copy);
        createVersionSnapshot(savedCopy, 1, "复制自API #" + source.getId(), source.getMetadata(), operator);

        updateProjectApiCount(project);
        return convertToDto(savedCopy);
    }

    @Override
    @Transactional(readOnly = true)
    public Object getApiStatistics(Long projectId, User operator) {
        Project project = loadProject(projectId);
        ensureReadPermission(project, operator);

        long total = apiDefinitionRepository.countByProject(project);
        Map<String, Object> result = new HashMap<>();
        result.put("totalApis", total);
        result.put("projectId", projectId);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApiVersionDto> listVersions(Long apiId, User operator) {
        ApiDefinition api = loadApi(apiId);
        ensureReadPermission(api.getProject(), operator);

        return apiDefinitionVersionRepository.findByApiOrderByVersionNumberDesc(api).stream()
                .map(this::convertVersionSummary)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ApiVersionDetailDto getVersionDetail(Long apiId, Integer versionNumber, User operator) {
        ApiDefinition api = loadApi(apiId);
        ensureReadPermission(api.getProject(), operator);
        ApiDefinitionVersion version = apiDefinitionVersionRepository
                .findByApiAndVersionNumber(api, versionNumber)
                .orElseThrow(() -> new ApiNotFoundException("指定的版本不存在"));
        return convertVersionDetail(version);
    }

    @Override
    public ApiResponseDto restoreVersion(Long apiId, Integer versionNumber, String changeSummary, User operator) {
        ApiDefinition api = loadApi(apiId);
        ensurePermission(api.getProject(), operator, PERMISSION_API_ADMIN);

        ApiDefinitionVersion version = apiDefinitionVersionRepository
                .findByApiAndVersionNumber(api, versionNumber)
                .orElseThrow(() -> new ApiNotFoundException("指定的版本不存在"));

        api.setName(version.getName());
        api.setMethod(version.getMethod());
        api.setPath(version.getPath());
        api.setDescription(version.getDescription());
        api.setGroup(version.getGroupSnapshot());
        api.setStatus(version.getStatus());
        api.setRequestHeaders(version.getRequestHeaders());
        api.setPathParameters(version.getPathParameters());
        api.setQueryParameters(version.getQueryParameters());
        api.setRequestBodySchema(version.getRequestBodySchema());
        api.setResponseSchema(version.getResponseSchema());
        api.setResponseExamples(version.getResponseExamples());
        api.setMetadata(version.getMetadata());
        api.setUpdatedBy(operator);

        api.setLatestVersion(api.getLatestVersion() + 1);
        api.setVersionCount(api.getVersionCount() + 1);
        ApiDefinition saved = apiDefinitionRepository.save(api);

        createVersionSnapshot(saved, saved.getLatestVersion(),
                Optional.ofNullable(changeSummary).orElse("回滚至版本 " + versionNumber),
                version.getMetadata(), operator);

        return convertToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiVersionCompareResponse compareVersions(Long apiId, Integer version1, Integer version2, User operator) {
        ApiDefinition api = loadApi(apiId);
        ensureReadPermission(api.getProject(), operator);

        ApiDefinitionVersion v1 = apiDefinitionVersionRepository
                .findByApiAndVersionNumber(api, version1)
                .orElseThrow(() -> new ApiNotFoundException("版本" + version1 + "不存在"));
        ApiDefinitionVersion v2 = apiDefinitionVersionRepository
                .findByApiAndVersionNumber(api, version2)
                .orElseThrow(() -> new ApiNotFoundException("版本" + version2 + "不存在"));

        List<ApiVersionCompareResponse.VersionDifference> diffs = new ArrayList<>();
        compareField("name", v1.getName(), v2.getName(), diffs);
        compareField("method", v1.getMethod(), v2.getMethod(), diffs);
        compareField("path", v1.getPath(), v2.getPath(), diffs);
        compareField("description", v1.getDescription(), v2.getDescription(), diffs);
        compareField("groupId",
                v1.getGroupSnapshot() != null ? String.valueOf(v1.getGroupSnapshot().getId()) : null,
                v2.getGroupSnapshot() != null ? String.valueOf(v2.getGroupSnapshot().getId()) : null,
                diffs);
        compareField("status", String.valueOf(v1.getStatus()), String.valueOf(v2.getStatus()), diffs);
        compareField("requestHeaders", v1.getRequestHeaders(), v2.getRequestHeaders(), diffs);
        compareField("pathParameters", v1.getPathParameters(), v2.getPathParameters(), diffs);
        compareField("queryParameters", v1.getQueryParameters(), v2.getQueryParameters(), diffs);
        compareField("requestBodySchema", v1.getRequestBodySchema(), v2.getRequestBodySchema(), diffs);
        compareField("responseSchema", v1.getResponseSchema(), v2.getResponseSchema(), diffs);
        compareField("responseExamples", v1.getResponseExamples(), v2.getResponseExamples(), diffs);

        ApiVersionCompareResponse response = new ApiVersionCompareResponse();
        response.setHasDifferences(!diffs.isEmpty());
        response.setDifferences(diffs);
        return response;
    }

    private void compareField(String field, String v1, String v2, List<ApiVersionCompareResponse.VersionDifference> diffs) {
        String left = v1 == null ? "" : v1;
        String right = v2 == null ? "" : v2;
        if (!left.equals(right)) {
            diffs.add(new ApiVersionCompareResponse.VersionDifference(field, v1, v2));
        }
    }

    private void createVersionSnapshot(ApiDefinition api, Integer versionNumber, String changeSummary,
                                       String metadata, User operator) {
        ApiDefinitionVersion version = new ApiDefinitionVersion(api, versionNumber);
        version.setName(api.getName());
        version.setMethod(api.getMethod());
        version.setPath(api.getPath());
        version.setDescription(api.getDescription());
        version.setGroupSnapshot(api.getGroup());
        version.setStatus(api.getStatus());
        version.setRequestHeaders(api.getRequestHeaders());
        version.setPathParameters(api.getPathParameters());
        version.setQueryParameters(api.getQueryParameters());
        version.setRequestBodySchema(api.getRequestBodySchema());
        version.setResponseSchema(api.getResponseSchema());
        version.setResponseExamples(api.getResponseExamples());
        version.setMetadata(metadata != null ? metadata : api.getMetadata());
        version.setChangeSummary(changeSummary != null ? changeSummary : "版本 " + versionNumber);
        version.setCreatedBy(operator);

        apiDefinitionVersionRepository.save(version);
    }

    private ApiResponseDto convertToDto(ApiDefinition definition) {
        ApiResponseDto dto = new ApiResponseDto();
        dto.setId(definition.getId());
        dto.setProjectId(definition.getProject().getId());
        dto.setGroupId(definition.getGroup() != null ? definition.getGroup().getId() : null);
        dto.setGroupName(definition.getGroup() != null ? definition.getGroup().getName() : null);
        dto.setName(definition.getName());
        dto.setMethod(definition.getMethod());
        dto.setPath(definition.getPath());
        dto.setDescription(definition.getDescription());
        dto.setStatus(definition.getStatus());
        dto.setSortOrder(definition.getSortOrder());
        dto.setLatestVersion(definition.getLatestVersion());
        dto.setVersionCount(definition.getVersionCount());
        dto.setRequestHeaders(definition.getRequestHeaders());
        dto.setPathParameters(definition.getPathParameters());
        dto.setQueryParameters(definition.getQueryParameters());
        dto.setRequestBodySchema(definition.getRequestBodySchema());
        dto.setResponseSchema(definition.getResponseSchema());
        dto.setResponseExamples(definition.getResponseExamples());
        dto.setMetadata(definition.getMetadata());
        dto.setCreatedAt(definition.getCreatedAt());
        dto.setUpdatedAt(definition.getUpdatedAt());
        return dto;
    }

    private ApiVersionDto convertVersionSummary(ApiDefinitionVersion version) {
        ApiVersionDto dto = new ApiVersionDto();
        dto.setVersionNumber(version.getVersionNumber());
        dto.setName(version.getName());
        dto.setMethod(version.getMethod());
        dto.setPath(version.getPath());
        dto.setStatus(version.getStatus());
        dto.setChangeSummary(version.getChangeSummary());
        dto.setCreatedBy(version.getCreatedBy() != null ? version.getCreatedBy().getUsername() : null);
        dto.setCreatedAt(version.getCreatedAt());
        return dto;
    }

    private ApiVersionDetailDto convertVersionDetail(ApiDefinitionVersion version) {
        ApiVersionDetailDto dto = new ApiVersionDetailDto();
        dto.setVersionNumber(version.getVersionNumber());
        dto.setName(version.getName());
        dto.setMethod(version.getMethod());
        dto.setPath(version.getPath());
        dto.setDescription(version.getDescription());
        dto.setStatus(version.getStatus());
        dto.setGroupId(version.getGroupSnapshot() != null ? version.getGroupSnapshot().getId() : null);
        dto.setGroupName(version.getGroupSnapshot() != null ? version.getGroupSnapshot().getName() : null);
        dto.setRequestHeaders(version.getRequestHeaders());
        dto.setPathParameters(version.getPathParameters());
        dto.setQueryParameters(version.getQueryParameters());
        dto.setRequestBodySchema(version.getRequestBodySchema());
        dto.setResponseSchema(version.getResponseSchema());
        dto.setResponseExamples(version.getResponseExamples());
        dto.setMetadata(version.getMetadata());
        dto.setChangeSummary(version.getChangeSummary());
        dto.setCreatedBy(version.getCreatedBy() != null ? version.getCreatedBy().getUsername() : null);
        dto.setCreatedAt(version.getCreatedAt());
        return dto;
    }

    private ApiDefinition loadApi(Long id) {
        return apiDefinitionRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiNotFoundException("API接口不存在"));
    }

    private Project loadProject(Long projectId) {
        return projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("项目不存在"));
    }

    private ApiGroup resolveGroup(Project project, Long groupId) {
        if (groupId == null) {
            return null;
        }
        return apiGroupRepository.findByIdAndProject(groupId, project)
                .orElseThrow(() -> new InvalidRequestException("分组不存在或不属于该项目"));
    }

    private void ensurePermission(Project project, User user, String permission) {
        if (!projectMemberService.hasPermission(project, user, permission)) {
            throw new UnauthorizedAccessException("没有权限执行此操作");
        }
    }

    private void ensureReadPermission(Project project, User user) {
        if (!projectMemberService.hasAnyPermission(project, user, PERMISSION_API_ADMIN, PERMISSION_API_READ)) {
            throw new UnauthorizedAccessException("没有权限查看API信息");
        }
    }

    private void updateProjectApiCount(Project project) {
        long count = apiDefinitionRepository.countByProject(project);
        project.setApiCount((int) count);
        projectRepository.save(project);
    }

    @Override
    public Optional<ApiDefinition> findByProjectIdAndPath(Long projectId, String path, String method) {
        return apiDefinitionRepository.findByProjectIdAndPathIgnoreCaseAndMethodIgnoreCaseAndDeletedAtIsNull(
                projectId, path, method);
    }
}
