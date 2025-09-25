package com.apimock.entity.api;

import com.apimock.entity.auth.User;
import com.apimock.entity.project.Project;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_definitions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_api_project_method_path", columnNames = {"project_id", "method", "path"})
})
public class ApiDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private ApiGroup group;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 16)
    private String method;

    @Column(nullable = false, length = 255)
    private String path;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer status = 1;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "latest_version", nullable = false)
    private Integer latestVersion = 1;

    @Column(name = "version_count", nullable = false)
    private Integer versionCount = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Lob
    @Column(name = "request_headers", columnDefinition = "LONGTEXT")
    private String requestHeaders;

    @Lob
    @Column(name = "path_parameters", columnDefinition = "LONGTEXT")
    private String pathParameters;

    @Lob
    @Column(name = "query_parameters", columnDefinition = "LONGTEXT")
    private String queryParameters;

    @Lob
    @Column(name = "request_body_schema", columnDefinition = "LONGTEXT")
    private String requestBodySchema;

    @Lob
    @Column(name = "response_schema", columnDefinition = "LONGTEXT")
    private String responseSchema;

    @Lob
    @Column(name = "response_examples", columnDefinition = "LONGTEXT")
    private String responseExamples;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String metadata;

    public ApiDefinition() {
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void markAsDeleted() {
        this.deletedAt = LocalDateTime.now();
        this.status = 0;
    }

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

    public ApiGroup getGroup() {
        return group;
    }

    public void setGroup(ApiGroup group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(Integer latestVersion) {
        this.latestVersion = latestVersion;
    }

    public Integer getVersionCount() {
        return versionCount;
    }

    public void setVersionCount(Integer versionCount) {
        this.versionCount = versionCount;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(String requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public String getPathParameters() {
        return pathParameters;
    }

    public void setPathParameters(String pathParameters) {
        this.pathParameters = pathParameters;
    }

    public String getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(String queryParameters) {
        this.queryParameters = queryParameters;
    }

    public String getRequestBodySchema() {
        return requestBodySchema;
    }

    public void setRequestBodySchema(String requestBodySchema) {
        this.requestBodySchema = requestBodySchema;
    }

    public String getResponseSchema() {
        return responseSchema;
    }

    public void setResponseSchema(String responseSchema) {
        this.responseSchema = responseSchema;
    }

    public String getResponseExamples() {
        return responseExamples;
    }

    public void setResponseExamples(String responseExamples) {
        this.responseExamples = responseExamples;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}
