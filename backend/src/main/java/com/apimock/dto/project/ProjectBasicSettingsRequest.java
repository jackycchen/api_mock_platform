package com.apimock.dto.project;

import jakarta.validation.constraints.*;

/**
 * 项目基本设置请求DTO
 *
 * @author AI Assistant
 */
public class ProjectBasicSettingsRequest {

    @NotBlank(message = "项目名称不能为空")
    @Size(min = 2, max = 100, message = "项目名称长度必须在2-100个字符之间")
    private String name;

    @Size(max = 500, message = "项目描述不能超过500个字符")
    private String description;

    @NotBlank(message = "项目类型不能为空")
    private String type;

    @NotBlank(message = "项目可见性不能为空")
    private String visibility;

    @Size(max = 500, message = "项目标签不能超过500个字符")
    private String projectTags;

    // 构造函数
    public ProjectBasicSettingsRequest() {}

    public ProjectBasicSettingsRequest(String name, String description, String type, String visibility) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.visibility = visibility;
    }

    // Getters and Setters
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getProjectTags() {
        return projectTags;
    }

    public void setProjectTags(String projectTags) {
        this.projectTags = projectTags;
    }
}