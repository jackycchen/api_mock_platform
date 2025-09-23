package com.apimock.service.project;

import com.apimock.entity.auth.User;
import com.apimock.entity.project.Project;
import com.apimock.entity.project.ProjectMember;
import com.apimock.entity.project.ProjectRole;
import com.apimock.repository.project.ProjectMemberRepository;
import com.apimock.service.auth.PermissionService;
import com.apimock.service.auth.UserOperationLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectMemberServiceTest {

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private PermissionService permissionService;

    @Mock
    private UserOperationLogService operationLogService;

    @InjectMocks
    private ProjectMemberService projectMemberService;

    private User user;
    private Project project;
    private ProjectMember projectMember;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "test@example.com", "hashedpassword");
        user.setId(1L);

        project = new Project();
        project.setId(1L);
        project.setName("Test Project");

        projectMember = new ProjectMember(project, user, ProjectRole.DEVELOPER);
        projectMember.setId(1L);
    }

    @Test
    void testInitializeProjectOwner_NewMember() {
        // Given
        when(projectMemberRepository.findByProjectAndUserAndRemovedAtIsNull(project, user))
                .thenReturn(Optional.empty());
        when(projectMemberRepository.save(any(ProjectMember.class)))
                .thenReturn(projectMember);

        // When
        ProjectMember result = projectMemberService.initializeProjectOwner(project, user);

        // Then
        assertThat(result).isNotNull();
        verify(projectMemberRepository).save(any(ProjectMember.class));
    }

    @Test
    void testInitializeProjectOwner_ExistingMember() {
        // Given
        ProjectMember existingMember = new ProjectMember(project, user, ProjectRole.DEVELOPER);
        when(projectMemberRepository.findByProjectAndUserAndRemovedAtIsNull(project, user))
                .thenReturn(Optional.of(existingMember));
        when(projectMemberRepository.save(existingMember))
                .thenReturn(existingMember);

        // When
        ProjectMember result = projectMemberService.initializeProjectOwner(project, user);

        // Then
        assertThat(result).isNotNull();
        assertThat(existingMember.getRole()).isEqualTo(ProjectRole.OWNER);
        verify(projectMemberRepository).save(existingMember);
    }

    @Test
    void testAddMember_Success() {
        // Given
        when(projectMemberRepository.existsByProjectAndUserAndRemovedAtIsNull(project, user))
                .thenReturn(false);
        when(projectMemberRepository.save(any(ProjectMember.class)))
                .thenReturn(projectMember);

        // When
        ProjectMember result = projectMemberService.addMember(project, user, ProjectRole.DEVELOPER);

        // Then
        assertThat(result).isNotNull();
        verify(projectMemberRepository).save(any(ProjectMember.class));
    }

    @Test
    void testAddMember_UserAlreadyMember() {
        // Given
        when(projectMemberRepository.existsByProjectAndUserAndRemovedAtIsNull(project, user))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> projectMemberService.addMember(project, user, ProjectRole.DEVELOPER))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("用户已经是项目成员");
    }

    @Test
    void testRemoveMember_Success() {
        // Given
        ProjectMember member = new ProjectMember(project, user, ProjectRole.DEVELOPER);
        when(projectMemberRepository.findByProjectAndUserAndRemovedAtIsNull(project, user))
                .thenReturn(Optional.of(member));

        // When
        projectMemberService.removeMember(project, user);

        // Then
        verify(projectMemberRepository).save(member);
    }

    @Test
    void testRemoveMember_CannotRemoveOwner() {
        // Given
        ProjectMember ownerMember = new ProjectMember(project, user, ProjectRole.OWNER);
        when(projectMemberRepository.findByProjectAndUserAndRemovedAtIsNull(project, user))
                .thenReturn(Optional.of(ownerMember));

        // When & Then
        assertThatThrownBy(() -> projectMemberService.removeMember(project, user))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("不能移除项目所有者");
    }

    @Test
    void testUpdateMemberRole_Success() {
        // Given
        ProjectMember member = new ProjectMember(project, user, ProjectRole.DEVELOPER);
        when(projectMemberRepository.findByProjectAndUserAndRemovedAtIsNull(project, user))
                .thenReturn(Optional.of(member));
        when(projectMemberRepository.save(member))
                .thenReturn(member);

        // When
        ProjectMember result = projectMemberService.updateMemberRole(project, user, ProjectRole.ADMIN);

        // Then
        assertThat(result).isNotNull();
        assertThat(member.getRole()).isEqualTo(ProjectRole.ADMIN);
        verify(projectMemberRepository).save(member);
    }

    @Test
    void testUpdateMemberRole_UserNotMember() {
        // Given
        when(projectMemberRepository.findByProjectAndUserAndRemovedAtIsNull(project, user))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> projectMemberService.updateMemberRole(project, user, ProjectRole.ADMIN))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("用户不是项目成员");
    }

    @Test
    void testUpdateMemberRole_CannotChangeOwnerRole() {
        // Given
        ProjectMember ownerMember = new ProjectMember(project, user, ProjectRole.OWNER);
        when(projectMemberRepository.findByProjectAndUserAndRemovedAtIsNull(project, user))
                .thenReturn(Optional.of(ownerMember));

        // When & Then
        assertThatThrownBy(() -> projectMemberService.updateMemberRole(project, user, ProjectRole.ADMIN))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("不能修改项目所有者的角色");
    }

    @Test
    void testIsProjectMember() {
        // Given
        when(projectMemberRepository.existsByProjectAndUserAndRemovedAtIsNull(project, user))
                .thenReturn(true);

        // When
        boolean result = projectMemberService.isProjectMember(project, user);

        // Then
        assertThat(result).isTrue();
        verify(projectMemberRepository).existsByProjectAndUserAndRemovedAtIsNull(project, user);
    }

    @Test
    void testIsProjectOwner() {
        // Given
        when(projectMemberRepository.isProjectOwner(project.getId(), user.getId()))
                .thenReturn(true);

        // When
        boolean result = projectMemberService.isProjectOwner(project, user);

        // Then
        assertThat(result).isTrue();
        verify(projectMemberRepository).isProjectOwner(project.getId(), user.getId());
    }

    @Test
    void testIsProjectAdmin() {
        // Given
        when(projectMemberRepository.isProjectAdmin(project.getId(), user.getId()))
                .thenReturn(true);

        // When
        boolean result = projectMemberService.isProjectAdmin(project, user);

        // Then
        assertThat(result).isTrue();
        verify(projectMemberRepository).isProjectAdmin(project.getId(), user.getId());
    }

    @Test
    void testHasPermission() {
        // Given
        String permission = "project:read";
        when(projectMemberRepository.findUserRoleInProject(project, user))
                .thenReturn(Optional.of(ProjectRole.DEVELOPER));
        when(permissionService.hasPermission(ProjectRole.DEVELOPER, permission))
                .thenReturn(true);

        // When
        boolean result = projectMemberService.hasPermission(project, user, permission);

        // Then
        assertThat(result).isTrue();
        verify(permissionService).hasPermission(ProjectRole.DEVELOPER, permission);
    }

    @Test
    void testHasPermission_UserNotMember() {
        // Given
        String permission = "project:read";
        when(projectMemberRepository.findUserRoleInProject(project, user))
                .thenReturn(Optional.empty());

        // When
        boolean result = projectMemberService.hasPermission(project, user, permission);

        // Then
        assertThat(result).isFalse();
        verify(permissionService, never()).hasPermission(any(), any());
    }

    @Test
    void testGetProjectMember() {
        // Given
        Long projectId = 1L;
        Long userId = 1L;
        when(projectMemberRepository.findByProjectIdAndUserIdAndRemovedAtIsNull(projectId, userId))
                .thenReturn(Optional.of(projectMember));

        // When
        Optional<ProjectMember> result = projectMemberService.getProjectMember(projectId, userId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(projectMember);
    }

    @Test
    void testTransferOwnership_Success() {
        // Given
        User newOwner = new User("newowner", "newowner@example.com", "hash");
        newOwner.setId(2L);

        ProjectMember currentOwnerMember = new ProjectMember(project, user, ProjectRole.OWNER);
        ProjectMember newOwnerMember = new ProjectMember(project, newOwner, ProjectRole.DEVELOPER);

        when(projectMemberRepository.isProjectOwner(project.getId(), user.getId()))
                .thenReturn(true);
        when(projectMemberRepository.findByProjectAndUserAndRemovedAtIsNull(project, newOwner))
                .thenReturn(Optional.of(newOwnerMember));
        when(projectMemberRepository.findByProjectAndUserAndRemovedAtIsNull(project, user))
                .thenReturn(Optional.of(currentOwnerMember));

        // When
        projectMemberService.transferOwnership(project, user, newOwner);

        // Then
        assertThat(newOwnerMember.getRole()).isEqualTo(ProjectRole.OWNER);
        assertThat(currentOwnerMember.getRole()).isEqualTo(ProjectRole.ADMIN);
        verify(projectMemberRepository, times(2)).save(any(ProjectMember.class));
    }

    @Test
    void testTransferOwnership_NotOwner() {
        // Given
        User newOwner = new User("newowner", "newowner@example.com", "hash");
        when(projectMemberRepository.isProjectOwner(project.getId(), user.getId()))
                .thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> projectMemberService.transferOwnership(project, user, newOwner))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("只有项目所有者可以转移所有权");
    }
}