package com.apimock.service.api;

import com.apimock.dto.api.ApiResponseDto;
import com.apimock.dto.api.ApiVersionDetailDto;
import com.apimock.dto.api.CreateApiRequest;
import com.apimock.entity.api.ApiDefinition;
import com.apimock.entity.api.ApiDefinitionVersion;
import com.apimock.entity.auth.User;
import com.apimock.entity.project.Project;
import com.apimock.exception.ApiNotFoundException;
import com.apimock.exception.ApiPathConflictException;
import com.apimock.repository.ProjectRepository;
import com.apimock.repository.api.ApiDefinitionRepository;
import com.apimock.repository.api.ApiDefinitionVersionRepository;
import com.apimock.repository.api.ApiGroupRepository;
import com.apimock.service.project.ProjectMemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiServiceImplTest {

    @Mock
    private ApiDefinitionRepository apiDefinitionRepository;

    @Mock
    private ApiDefinitionVersionRepository apiDefinitionVersionRepository;

    @Mock
    private ApiGroupRepository apiGroupRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberService projectMemberService;

    @InjectMocks
    private ApiServiceImpl apiService;

    private Project project;
    private User operator;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setId(1L);
        project.setName("Demo");
        project.setCreatedAt(LocalDateTime.now());

        operator = new User("tester", "tester@example.com", "hash");
        operator.setId(99L);

        when(projectRepository.findByIdAndDeletedAtIsNull(project.getId())).thenReturn(Optional.of(project));
        when(projectMemberService.hasPermission(eq(project), eq(operator), anyString())).thenReturn(true);
        when(projectMemberService.hasAnyPermission(eq(project), eq(operator), any())).thenReturn(true);
    }

    @Test
    void createApi_ShouldPersistDefinitionAndVersion() {
        CreateApiRequest request = new CreateApiRequest();
        request.setProjectId(project.getId());
        request.setName("Get Users");
        request.setMethod("GET");
        request.setPath("/users");
        request.setDescription("List users");

        when(apiDefinitionRepository.existsByProjectAndMethodIgnoreCaseAndPathIgnoreCaseAndDeletedAtIsNull(project, "GET", "/users"))
                .thenReturn(false);
        when(apiDefinitionRepository.save(any(ApiDefinition.class))).thenAnswer(invocation -> {
            ApiDefinition entity = invocation.getArgument(0);
            entity.setId(10L);
            return entity;
        });
        when(apiDefinitionRepository.countByProject(project)).thenReturn(1L);
        when(projectRepository.save(project)).thenReturn(project);

        ApiResponseDto response = apiService.createApi(request, operator);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Get Users");
        assertThat(response.getLatestVersion()).isEqualTo(1);

        ArgumentCaptor<ApiDefinitionVersion> versionCaptor = ArgumentCaptor.forClass(ApiDefinitionVersion.class);
        verify(apiDefinitionVersionRepository).save(versionCaptor.capture());
        assertThat(versionCaptor.getValue().getVersionNumber()).isEqualTo(1);
        assertThat(versionCaptor.getValue().getName()).isEqualTo("Get Users");

        verify(projectRepository).save(project);
    }

    @Test
    void createApi_ShouldThrowWhenPathConflict() {
        CreateApiRequest request = new CreateApiRequest();
        request.setProjectId(project.getId());
        request.setName("Get Users");
        request.setMethod("GET");
        request.setPath("/users");

        when(apiDefinitionRepository.existsByProjectAndMethodIgnoreCaseAndPathIgnoreCaseAndDeletedAtIsNull(project, "GET", "/users"))
                .thenReturn(true);

        assertThatThrownBy(() -> apiService.createApi(request, operator))
                .isInstanceOf(ApiPathConflictException.class);
    }

    @Test
    void restoreVersion_ShouldCopyVersionFieldsAndCreateSnapshot() {
        ApiDefinition stored = new ApiDefinition();
        stored.setId(11L);
        stored.setProject(project);
        stored.setName("Original");
        stored.setMethod("GET");
        stored.setPath("/origin");
        stored.setLatestVersion(2);
        stored.setVersionCount(2);

        ApiDefinitionVersion history = new ApiDefinitionVersion();
        history.setVersionNumber(1);
        history.setName("Legacy");
        history.setMethod("POST");
        history.setPath("/legacy");
        history.setStatus(1);

        when(apiDefinitionRepository.findByIdAndDeletedAtIsNull(11L)).thenReturn(Optional.of(stored));
        when(apiDefinitionVersionRepository.findByApiAndVersionNumber(stored, 1)).thenReturn(Optional.of(history));
        when(apiDefinitionRepository.save(any(ApiDefinition.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApiResponseDto result = apiService.restoreVersion(11L, 1, "回滚测试", operator);

        assertThat(result.getName()).isEqualTo("Legacy");
        assertThat(result.getMethod()).isEqualTo("POST");
        assertThat(result.getPath()).isEqualTo("/legacy");
        assertThat(result.getLatestVersion()).isEqualTo(3);

        ArgumentCaptor<ApiDefinitionVersion> snapshotCaptor = ArgumentCaptor.forClass(ApiDefinitionVersion.class);
        verify(apiDefinitionVersionRepository, times(2)).save(snapshotCaptor.capture());
        ApiDefinitionVersion newSnapshot = snapshotCaptor.getAllValues().get(1);
        assertThat(newSnapshot.getVersionNumber()).isEqualTo(3);
        assertThat(newSnapshot.getChangeSummary()).contains("回滚");
    }

    @Test
    void getVersionDetail_ShouldThrowWhenMissing() {
        ApiDefinition stored = new ApiDefinition();
        stored.setId(15L);
        stored.setProject(project);

        when(apiDefinitionRepository.findByIdAndDeletedAtIsNull(15L)).thenReturn(Optional.of(stored));
        when(projectMemberService.hasAnyPermission(eq(project), eq(operator), any())).thenReturn(true);
        when(apiDefinitionVersionRepository.findByApiAndVersionNumber(stored, 9)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> apiService.getVersionDetail(15L, 9, operator))
                .isInstanceOf(ApiNotFoundException.class);
    }

    @Test
    void getVersionDetail_ShouldReturnDto() {
        ApiDefinition stored = new ApiDefinition();
        stored.setId(20L);
        stored.setProject(project);

        ApiDefinitionVersion version = new ApiDefinitionVersion();
        version.setVersionNumber(2);
        version.setName("Snapshot");
        version.setMethod("GET");
        version.setPath("/snap");
        version.setStatus(1);

        when(apiDefinitionRepository.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.of(stored));
        when(projectMemberService.hasAnyPermission(eq(project), eq(operator), any())).thenReturn(true);
        when(apiDefinitionVersionRepository.findByApiAndVersionNumber(stored, 2)).thenReturn(Optional.of(version));

        ApiVersionDetailDto detail = apiService.getVersionDetail(20L, 2, operator);
        assertThat(detail.getName()).isEqualTo("Snapshot");
        assertThat(detail.getVersionNumber()).isEqualTo(2);
    }
}
