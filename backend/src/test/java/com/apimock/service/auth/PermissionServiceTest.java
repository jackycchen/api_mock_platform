package com.apimock.service.auth;

import com.apimock.entity.auth.Permission;
import com.apimock.entity.auth.RolePermission;
import com.apimock.entity.project.ProjectRole;
import com.apimock.repository.auth.PermissionRepository;
import com.apimock.repository.auth.RolePermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @InjectMocks
    private PermissionService permissionService;

    private Permission readPermission;
    private Permission writePermission;
    private Permission deletePermission;
    private RolePermission ownerReadPermission;
    private RolePermission ownerWritePermission;
    private RolePermission ownerDeletePermission;
    private RolePermission devReadPermission;

    @BeforeEach
    void setUp() {
        readPermission = new Permission();
        readPermission.setId(1L);
        readPermission.setName("project:read");
        readPermission.setDescription("Read project");

        writePermission = new Permission();
        writePermission.setId(2L);
        writePermission.setName("project:write");
        writePermission.setDescription("Write project");

        deletePermission = new Permission();
        deletePermission.setId(3L);
        deletePermission.setName("project:delete");
        deletePermission.setDescription("Delete project");

        ownerReadPermission = new RolePermission();
        ownerReadPermission.setRole(ProjectRole.OWNER);
        ownerReadPermission.setPermission(readPermission);

        ownerWritePermission = new RolePermission();
        ownerWritePermission.setRole(ProjectRole.OWNER);
        ownerWritePermission.setPermission(writePermission);

        ownerDeletePermission = new RolePermission();
        ownerDeletePermission.setRole(ProjectRole.OWNER);
        ownerDeletePermission.setPermission(deletePermission);

        devReadPermission = new RolePermission();
        devReadPermission.setRole(ProjectRole.DEVELOPER);
        devReadPermission.setPermission(readPermission);
    }

    @Test
    void testHasPermission_WithPermission() {
        // Given
        when(rolePermissionRepository.hasPermission(ProjectRole.OWNER, "project:read"))
                .thenReturn(true);

        // When
        boolean result = permissionService.hasPermission(ProjectRole.OWNER, "project:read");

        // Then
        assertThat(result).isTrue();
        verify(rolePermissionRepository).hasPermission(ProjectRole.OWNER, "project:read");
    }

    @Test
    void testHasPermission_WithoutPermission() {
        // Given
        when(rolePermissionRepository.hasPermission(ProjectRole.DEVELOPER, "project:delete"))
                .thenReturn(false);

        // When
        boolean result = permissionService.hasPermission(ProjectRole.DEVELOPER, "project:delete");

        // Then
        assertThat(result).isFalse();
        verify(rolePermissionRepository).hasPermission(ProjectRole.DEVELOPER, "project:delete");
    }

    @Test
    void testHasPermission_NullRole() {
        // When
        boolean result = permissionService.hasPermission(null, "project:read");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testHasPermission_NullPermission() {
        // When
        boolean result = permissionService.hasPermission(ProjectRole.OWNER, null);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testHasAnyPermission_WithOnePermission() {
        // Given
        when(rolePermissionRepository.hasPermission(ProjectRole.DEVELOPER, "project:read"))
                .thenReturn(true);
        // 不需要模拟第二个方法，因为第一个返回 true 后就会短路

        // When
        boolean result = permissionService.hasAnyPermission(ProjectRole.DEVELOPER, "project:read", "project:write");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void testHasAnyPermission_WithoutAnyPermission() {
        // Given
        when(rolePermissionRepository.hasPermission(ProjectRole.DEVELOPER, "project:write"))
                .thenReturn(false);
        when(rolePermissionRepository.hasPermission(ProjectRole.DEVELOPER, "project:delete"))
                .thenReturn(false);

        // When
        boolean result = permissionService.hasAnyPermission(ProjectRole.DEVELOPER, "project:write", "project:delete");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testHasAnyPermission_EmptyPermissions() {
        // When
        boolean result = permissionService.hasAnyPermission(ProjectRole.OWNER);

        // Then
        assertThat(result).isFalse(); // No permissions to check, so return false
    }

    @Test
    void testHasAllPermissions_WithAllPermissions() {
        // Given
        when(rolePermissionRepository.hasPermission(ProjectRole.OWNER, "project:read"))
                .thenReturn(true);
        when(rolePermissionRepository.hasPermission(ProjectRole.OWNER, "project:write"))
                .thenReturn(true);

        // When
        boolean result = permissionService.hasAllPermissions(ProjectRole.OWNER, "project:read", "project:write");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void testHasAllPermissions_MissingOnePermission() {
        // Given
        when(rolePermissionRepository.hasPermission(ProjectRole.DEVELOPER, "project:read"))
                .thenReturn(true);
        when(rolePermissionRepository.hasPermission(ProjectRole.DEVELOPER, "project:write"))
                .thenReturn(false);

        // When
        boolean result = permissionService.hasAllPermissions(ProjectRole.DEVELOPER, "project:read", "project:write");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testHasAllPermissions_EmptyPermissions() {
        // When
        boolean result = permissionService.hasAllPermissions(ProjectRole.OWNER);

        // Then
        assertThat(result).isTrue(); // No permissions to check, so return true
    }

    @Test
    void testGetRolePermissions() {
        // Given
        Set<String> expectedPermissions = Set.of("project:read", "project:write");
        when(rolePermissionRepository.findPermissionCodesByRole(ProjectRole.OWNER))
                .thenReturn(Arrays.asList("project:read", "project:write"));

        // When
        Set<String> result = permissionService.getRolePermissions(ProjectRole.OWNER);

        // Then
        assertThat(result).isEqualTo(expectedPermissions);
        verify(rolePermissionRepository).findPermissionCodesByRole(ProjectRole.OWNER);
    }

    @Test
    void testGetRolePermissions_EmptyResult() {
        // Given
        when(rolePermissionRepository.findPermissionCodesByRole(ProjectRole.VIEWER))
                .thenReturn(Collections.emptyList());

        // When
        Set<String> result = permissionService.getRolePermissions(ProjectRole.VIEWER);

        // Then
        assertThat(result).isEmpty();
        verify(rolePermissionRepository).findPermissionCodesByRole(ProjectRole.VIEWER);
    }

    @Test
    void testGetAllPermissions() {
        // Given
        List<Permission> expectedPermissions = Arrays.asList(readPermission, writePermission, deletePermission);
        when(permissionRepository.findByEnabledTrue())
                .thenReturn(expectedPermissions);

        // When
        List<Permission> result = permissionService.getAllPermissions();

        // Then
        assertThat(result).isEqualTo(expectedPermissions);
        verify(permissionRepository).findByEnabledTrue();
    }

    @Test
    void testHasPermission_CaseInsensitive() {
        // Given
        when(rolePermissionRepository.hasPermission(ProjectRole.OWNER, "PROJECT:READ"))
                .thenReturn(true);

        // When
        boolean result = permissionService.hasPermission(ProjectRole.OWNER, "PROJECT:READ");

        // Then
        assertThat(result).isTrue(); // Should be case insensitive
    }

    @Test
    void testSearchPermissions() {
        // Given
        String keyword = "project";
        List<Permission> expectedPermissions = Arrays.asList(readPermission, writePermission);
        when(permissionRepository.findByNameContaining(keyword))
                .thenReturn(expectedPermissions);

        // When
        List<Permission> result = permissionService.searchPermissions(keyword);

        // Then
        assertThat(result).isEqualTo(expectedPermissions);
        verify(permissionRepository).findByNameContaining(keyword);
    }
}