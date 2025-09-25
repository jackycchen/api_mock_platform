package com.apimock.dto.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateApiRequest {

    private Long groupId;

    @NotBlank(message = "接口名称不能为空")
    @Size(min = 2, max = 150, message = "接口名称长度应在2到150个字符之间")
    private String name;

    @NotBlank(message = "请求方法不能为空")
    @Pattern(regexp = "GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS|TRACE", message = "请求方法不合法")
    private String method;

    @NotBlank(message = "请求路径不能为空")
    @Size(max = 255, message = "请求路径长度不能超过255个字符")
    private String path;

    private String description;

    private String requestHeaders;

    private String pathParameters;

    private String queryParameters;

    private String requestBodySchema;

    private String responseSchema;

    private String responseExamples;

    private Integer status = 1;

    private Integer sortOrder = 0;

    private String metadata;

    private String changeSummary;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethod() {
        return method != null ? method.toUpperCase() : null;
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

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getChangeSummary() {
        return changeSummary;
    }

    public void setChangeSummary(String changeSummary) {
        this.changeSummary = changeSummary;
    }
}
