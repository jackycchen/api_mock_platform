package com.apimock.repository.project;

import com.apimock.entity.auth.User;
import com.apimock.entity.project.Project;
import com.apimock.entity.project.ProjectMember;
import com.apimock.entity.project.ProjectRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ProjectMemberRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    private User user;
    private User anotherUser;
    private Project project;
    private ProjectMember projectMember;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "test@example.com", "hashedpassword");
        user = entityManager.persistAndFlush(user);

        anotherUser = new User("anotheruser", "another@example.com", "hashedpassword");
        anotherUser = entityManager.persistAndFlush(anotherUser);

        project = new Project();
        project.setName("Test Project");
        project.setDescription("Test Description");
        project.setOwner(user);
        project.setType(com.apimock.entity.project.ProjectType.WEB_API); // 使用正确的枚举值
        project.setVisibility(com.apimock.entity.project.ProjectVisibility.PRIVATE); // 添加必需的 visibility 字段
        project = entityManager.persistAndFlush(project);

        projectMember = new ProjectMember(project, user, ProjectRole.OWNER);
        projectMember = entityManager.persistAndFlush(projectMember);

        entityManager.clear();
    }

    @Test
    void testFindByProjectAndUserAndRemovedAtIsNull() {
        // When
        Optional<ProjectMember> result = projectMemberRepository
                .findByProjectAndUserAndRemovedAtIsNull(project, user);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUser().getId()).isEqualTo(user.getId());
        assertThat(result.get().getProject().getId()).isEqualTo(project.getId());
        assertThat(result.get().getRole()).isEqualTo(ProjectRole.OWNER);
    }

    @Test
    void testFindByProjectAndUserAndRemovedAtIsNull_NotFound() {
        // When
        Optional<ProjectMember> result = projectMemberRepository
                .findByProjectAndUserAndRemovedAtIsNull(project, anotherUser);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void testFindByProjectAndRemovedAtIsNullOrderByJoinedAtAsc() {
        // Given
        ProjectMember anotherMember = new ProjectMember(project, anotherUser, ProjectRole.DEVELOPER);
        entityManager.persistAndFlush(anotherMember);

        // When
        List<ProjectMember> result = projectMemberRepository
                .findByProjectAndRemovedAtIsNullOrderByJoinedAtAsc(project);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUser().getId()).isEqualTo(user.getId());
        assertThat(result.get(1).getUser().getId()).isEqualTo(anotherUser.getId());
    }

    @Test
    void testExistsByProjectAndUserAndRemovedAtIsNull() {
        // When
        boolean exists = projectMemberRepository
                .existsByProjectAndUserAndRemovedAtIsNull(project, user);
        boolean notExists = projectMemberRepository
                .existsByProjectAndUserAndRemovedAtIsNull(project, anotherUser);

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void testFindUserRoleInProject() {
        // When
        Optional<ProjectRole> result = projectMemberRepository
                .findUserRoleInProject(project, user);
        Optional<ProjectRole> notFound = projectMemberRepository
                .findUserRoleInProject(project, anotherUser);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(ProjectRole.OWNER);
        assertThat(notFound).isEmpty();
    }

    @Test
    void testIsProjectOwner() {
        // When
        boolean isOwner = projectMemberRepository
                .isProjectOwner(project.getId(), user.getId());
        boolean isNotOwner = projectMemberRepository
                .isProjectOwner(project.getId(), anotherUser.getId());

        // Then
        assertThat(isOwner).isTrue();
        assertThat(isNotOwner).isFalse();
    }

    @Test
    void testIsProjectAdmin() {
        // Given
        ProjectMember adminMember = new ProjectMember(project, anotherUser, ProjectRole.ADMIN);
        entityManager.persistAndFlush(adminMember);

        // When
        boolean ownerIsAdmin = projectMemberRepository
                .isProjectAdmin(project.getId(), user.getId());
        boolean adminIsAdmin = projectMemberRepository
                .isProjectAdmin(project.getId(), anotherUser.getId());

        // Then
        assertThat(ownerIsAdmin).isTrue(); // Owner is also admin
        assertThat(adminIsAdmin).isTrue();
    }

    @Test
    void testCountByProjectAndRemovedAtIsNull() {
        // Given
        ProjectMember anotherMember = new ProjectMember(project, anotherUser, ProjectRole.DEVELOPER);
        entityManager.persistAndFlush(anotherMember);

        // When
        long count = projectMemberRepository.countByProjectAndRemovedAtIsNull(project);

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testFindByProjectIdAndUserIdAndRemovedAtIsNull() {
        // When
        Optional<ProjectMember> result = projectMemberRepository
                .findByProjectIdAndUserIdAndRemovedAtIsNull(project.getId(), user.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getRole()).isEqualTo(ProjectRole.OWNER);
    }

    @Test
    void testSoftDeleteScenario() {
        // Given - Mark member as removed
        projectMember.markAsRemoved();
        entityManager.merge(projectMember); // 使用 merge 而不是 persistAndFlush
        entityManager.flush();

        // When
        Optional<ProjectMember> result = projectMemberRepository
                .findByProjectAndUserAndRemovedAtIsNull(project, user);
        boolean exists = projectMemberRepository
                .existsByProjectAndUserAndRemovedAtIsNull(project, user);

        // Then
        assertThat(result).isEmpty();
        assertThat(exists).isFalse();
    }

    @Test
    void testFindByRoleAndProject() {
        // Given
        ProjectMember devMember = new ProjectMember(project, anotherUser, ProjectRole.DEVELOPER);
        entityManager.persistAndFlush(devMember);

        // When
        List<ProjectMember> owners = projectMemberRepository
                .findByProjectAndRoleAndRemovedAtIsNullOrderByJoinedAtAsc(project, ProjectRole.OWNER);
        List<ProjectMember> developers = projectMemberRepository
                .findByProjectAndRoleAndRemovedAtIsNullOrderByJoinedAtAsc(project, ProjectRole.DEVELOPER);

        // Then
        assertThat(owners).hasSize(1);
        assertThat(owners.get(0).getUser().getId()).isEqualTo(user.getId());
        assertThat(developers).hasSize(1);
        assertThat(developers.get(0).getUser().getId()).isEqualTo(anotherUser.getId());
    }

    @Test
    void testFindUserProjects() {
        // Given
        Project anotherProject = new Project();
        anotherProject.setName("Another Project");
        anotherProject.setOwner(anotherUser);
        anotherProject.setType(com.apimock.entity.project.ProjectType.WEB_API); // 使用正确的枚举值
        anotherProject.setVisibility(com.apimock.entity.project.ProjectVisibility.PRIVATE); // 添加必需的 visibility 字段
        anotherProject = entityManager.persistAndFlush(anotherProject);

        ProjectMember memberInAnotherProject = new ProjectMember(anotherProject, user, ProjectRole.DEVELOPER);
        entityManager.persistAndFlush(memberInAnotherProject);

        // When
        List<Project> userProjects = projectMemberRepository.findUserProjects(user);

        // Then
        assertThat(userProjects).hasSize(2);
        assertThat(userProjects).extracting(Project::getId)
                .containsExactlyInAnyOrder(project.getId(), anotherProject.getId());
    }
}