package com.apimock.dto.project;

import com.apimock.entity.project.ProjectType;
import com.apimock.entity.project.ProjectVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateProjectRequest {

    @NotBlank(message = "项目名称不能为空")
    @Size(min = 2, max = 100, message = "项目名称长度在2到100个字符之间")
    private String name;

    @Size(max = 500, message = "项目描述不能超过500个字符")
    private String description;

    @NotNull(message = "项目类型不能为空")
    private ProjectType type;

    @NotNull(message = "项目可见性不能为空")
    private ProjectVisibility visibility;

    @Size(max = 255, message = "基础URL不能超过255个字符")
    private String baseUrl;

    @Size(max = 50, message = "环境名称不能超过50个字符")
    private String environment;

    // 构造函数
    public CreateProjectRequest() {}

    public CreateProjectRequest(String name, String description, ProjectType type,
                               ProjectVisibility visibility) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.visibility = visibility;
    }

    // Getter和Setter方法
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProjectType getType() {
        return type;
    }

    public void setType(ProjectType type) {
        this.type = type;
    }

    public ProjectVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(ProjectVisibility visibility) {
        this.visibility = visibility;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    @Override
    public String toString() {
        return "CreateProjectRequest{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", visibility=" + visibility +
                '}';
    }
}