package com.apimock.entity.project;

public enum ProjectType {
    WEB_API("Web API", "标准的Web API项目"),
    REST_API("REST API", "RESTful API项目"),
    GRAPHQL_API("GraphQL API", "GraphQL接口项目"),
    MICROSERVICE("微服务", "微服务架构项目"),
    MOBILE_API("移动端API", "移动应用后端API"),
    IOT_API("物联网API", "物联网设备API"),
    CUSTOM("自定义", "自定义类型项目");

    private final String displayName;
    private final String description;

    ProjectType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static ProjectType fromString(String type) {
        for (ProjectType projectType : ProjectType.values()) {
            if (projectType.name().equalsIgnoreCase(type)) {
                return projectType;
            }
        }
        return CUSTOM;
    }
}