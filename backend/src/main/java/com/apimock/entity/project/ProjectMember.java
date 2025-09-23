package com.apimock.entity.project;

import com.apimock.entity.auth.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 项目成员实体
 * 管理项目成员关系和权限
 *
 * @author AI Assistant
 */
@Entity
@Table(name = "project_members")
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectRole role;

    @Column(nullable = false)
    private Integer status = 1; // 1: 活跃, 0: 已移除

    @Column(name = "joined_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime joinedAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "removed_at")
    private LocalDateTime removedAt;

    // 构造函数
    public ProjectMember() {}

    public ProjectMember(Project project, User user, ProjectRole role) {
        this.project = project;
        this.user = user;
        this.role = role;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ProjectRole getRole() {
        return role;
    }

    public void setRole(ProjectRole role) {
        this.role = role;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getRemovedAt() {
        return removedAt;
    }

    public void setRemovedAt(LocalDateTime removedAt) {
        this.removedAt = removedAt;
    }

    // 业务方法
    public boolean isActive() {
        return status == 1 && removedAt == null;
    }

    public boolean isRemoved() {
        return removedAt != null || status == 0;
    }

    public void markAsRemoved() {
        this.removedAt = LocalDateTime.now();
        this.status = 0;
    }

    public boolean hasPermission(String permission) {
        return role.hasPermission(permission);
    }

    public boolean isOwner() {
        return role == ProjectRole.OWNER;
    }

    public boolean isAdmin() {
        return role == ProjectRole.ADMIN || role == ProjectRole.OWNER;
    }

    public boolean canManageProject() {
        return role == ProjectRole.OWNER || role == ProjectRole.ADMIN;
    }

    public boolean canManageApis() {
        return role != ProjectRole.VIEWER;
    }

    public boolean canManageMocks() {
        return role != ProjectRole.VIEWER;
    }

    @Override
    public String toString() {
        return "ProjectMember{" +
                "id=" + id +
                ", projectId=" + (project != null ? project.getId() : null) +
                ", userId=" + (user != null ? user.getId() : null) +
                ", role=" + role +
                ", status=" + status +
                '}';
    }
}