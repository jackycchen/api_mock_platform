package com.apimock.entity.api;

import com.apimock.entity.auth.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_definition_versions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_api_version", columnNames = {"api_id", "version_number"})
})
public class ApiDefinitionVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "api_id", nullable = false)
    private ApiDefinition api;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 16)
    private String method;

    @Column(nullable = false, length = 255)
    private String path;

    @Column(columnDefinition = "TEXT")
    private String description;

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

    @Column(nullable = false)
    private Integer status = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private ApiGroup groupSnapshot;

    @Column(name = "change_summary", length = 255)
    private String changeSummary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ApiDefinitionVersion() {
    }

    public ApiDefinitionVersion(ApiDefinition api, Integer versionNumber) {
        this.api = api;
        this.versionNumber = versionNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ApiDefinition getApi() {
        return api;
    }

    public void setApi(ApiDefinition api) {
        this.api = api;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public ApiGroup getGroupSnapshot() {
        return groupSnapshot;
    }

    public void setGroupSnapshot(ApiGroup groupSnapshot) {
        this.groupSnapshot = groupSnapshot;
    }

    public String getChangeSummary() {
        return changeSummary;
    }

    public void setChangeSummary(String changeSummary) {
        this.changeSummary = changeSummary;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
