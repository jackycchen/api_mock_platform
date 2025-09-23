package com.apimock.service.project;

import com.apimock.dto.project.CreateProjectRequest;
import com.apimock.dto.project.ProjectResponse;
import com.apimock.dto.project.UpdateProjectRequest;
import com.apimock.entity.auth.User;
import com.apimock.entity.project.Project;
import com.apimock.entity.project.ProjectVisibility;
import com.apimock.repository.ProjectRepository;
import com.apimock.service.auth.UserOperationLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberService projectMemberService;

    @Mock
    private UserOperationLogService operationLogService;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private User user;
    private Project project;
    private CreateProjectRequest createRequest;
    private UpdateProjectRequest updateRequest;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "test@example.com", "hashedpassword");
        user.setId(1L);

        project = new Project();
        project.setId(1L);
        project.setName("Test Project");
        project.setDescription("Test Description");
        project.setOwner(user);
        project.setVisibility(ProjectVisibility.PRIVATE);
        project.setType(com.apimock.entity.project.ProjectType.WEB_API); // 使用正确的枚举值
        project.setCreatedAt(LocalDateTime.now());

        createRequest = new CreateProjectRequest();
        createRequest.setName("New Project");
        createRequest.setDescription("New Description");
        createRequest.setVisibility(ProjectVisibility.PRIVATE);

        updateRequest = new UpdateProjectRequest();
        updateRequest.setName("Updated Project");
        updateRequest.setDescription("Updated Description");
        updateRequest.setVisibility(ProjectVisibility.PUBLIC);
    }

    @Test
    void testCreateProject_Success() {
        // Given
        when(projectRepository.existsByNameAndDeletedAtIsNull("New Project"))
                .thenReturn(false);
        when(projectRepository.save(any(Project.class)))
                .thenReturn(project);

        // When
        ProjectResponse result = projectService.createProject(createRequest, user);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(project.getName());
        verify(projectRepository).save(any(Project.class));
        verify(projectMemberService).initializeProjectOwner(any(Project.class), eq(user));
        // 注意：日志记录功能还未实现，所以不验证 operationLogService
    }

    @Test
    void testCreateProject_DuplicateName() {
        // Given
        when(projectRepository.existsByNameAndDeletedAtIsNull("New Project"))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> projectService.createProject(createRequest, user))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("项目名称已存在");
    }

    @Test
    void testGetProjectById_Success() {
        // Given
        when(projectRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(project));
        when(projectMemberService.isProjectMember(project, user))
                .thenReturn(true);

        // When
        ProjectResponse result = projectService.getProjectById(1L, user);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Project");
    }

    @Test
    void testGetProjectById_NotFound() {
        // Given
        when(projectRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> projectService.getProjectById(1L, user))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("项目不存在");
    }

    @Test
    void testGetProjectById_NoAccess() {
        // Given
        when(projectRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(project));
        when(projectMemberService.isProjectMember(project, user))
                .thenReturn(false);
        project.setVisibility(ProjectVisibility.PRIVATE);

        // When & Then
        assertThatThrownBy(() -> projectService.getProjectById(1L, user))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("无权限访问此项目");
    }

    @Test
    void testUpdateProject_Success() {
        // Given
        when(projectRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(project));
        when(projectMemberService.isProjectOwner(project, user))
                .thenReturn(true); // 模拟用户是项目所有者
        when(projectRepository.save(project))
                .thenReturn(project);

        // When
        ProjectResponse result = projectService.updateProject(1L, updateRequest, user);

        // Then
        assertThat(result).isNotNull();
        assertThat(project.getName()).isEqualTo("Updated Project");
        assertThat(project.getDescription()).isEqualTo("Updated Description");
        assertThat(project.getVisibility()).isEqualTo(ProjectVisibility.PUBLIC);
        verify(projectRepository).save(project);
    }

    @Test
    void testUpdateProject_NoPermission() {
        // Given
        when(projectRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(project));
        when(projectMemberService.isProjectOwner(project, user))
                .thenReturn(false); // 模拟用户不是项目所有者

        // When & Then
        assertThatThrownBy(() -> projectService.updateProject(1L, updateRequest, user))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("只有项目所有者可以修改项目信息");
    }

    @Test
    void testDeleteProject_Success() {
        // Given
        when(projectRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(project));
        when(projectMemberService.isProjectOwner(project, user))
                .thenReturn(true); // 模拟用户是项目所有者

        // When
        projectService.deleteProject(1L, user);

        // Then
        assertThat(project.getDeletedAt()).isNotNull();
        verify(projectRepository).save(project);
        verify(projectMemberService).cleanupProjectMembers(project);
    }

    @Test
    void testDeleteProject_NoPermission() {
        // Given
        when(projectRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(project));
        when(projectMemberService.isProjectOwner(project, user))
                .thenReturn(false); // 模拟用户不是项目所有者

        // When & Then
        assertThatThrownBy(() -> projectService.deleteProject(1L, user))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("只有项目所有者可以删除项目");
    }

    @Test
    void testGetAccessibleProjects() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> projectPage = new PageImpl<>(Arrays.asList(project));
        when(projectRepository.findAccessibleProjects(user, ProjectVisibility.PUBLIC, pageable))
                .thenReturn(projectPage);

        // When
        Page<ProjectResponse> result = projectService.getAccessibleProjects(user, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Test Project");
    }

    @Test
    void testSearchProjects() {
        // Given
        String keyword = "test";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> projectPage = new PageImpl<>(Arrays.asList(project));
        when(projectRepository.searchByKeyword(keyword, pageable))
                .thenReturn(projectPage);

        // When
        Page<ProjectResponse> result = projectService.searchProjects(keyword, user, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void testSearchProjects_EmptyKeyword() {
        // Given
        String keyword = "";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> projectPage = new PageImpl<>(Arrays.asList(project));
        when(projectRepository.findAccessibleProjects(user, ProjectVisibility.PUBLIC, pageable))
                .thenReturn(projectPage);

        // When
        Page<ProjectResponse> result = projectService.searchProjects(keyword, user, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(projectRepository).findAccessibleProjects(user, ProjectVisibility.PUBLIC, pageable);
    }

    @Test
    void testGetUserProjects() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> projectPage = new PageImpl<>(Arrays.asList(project));
        when(projectRepository.findByOwnerAndDeletedAtIsNullOrderByUpdatedAtDesc(user, pageable))
                .thenReturn(projectPage);

        // When
        Page<ProjectResponse> result = projectService.getUserProjects(user, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Test Project");
    }

    @Test
    void testIsProjectNameAvailable_Available() {
        // Given
        String projectName = "Available Name";
        when(projectRepository.existsByNameAndDeletedAtIsNull(projectName))
                .thenReturn(false);

        // When
        boolean result = projectService.isProjectNameAvailable(projectName);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void testIsProjectNameAvailable_NotAvailable() {
        // Given
        String projectName = "Taken Name";
        when(projectRepository.existsByNameAndDeletedAtIsNull(projectName))
                .thenReturn(true);

        // When
        boolean result = projectService.isProjectNameAvailable(projectName);

        // Then
        assertThat(result).isFalse();
    }
}