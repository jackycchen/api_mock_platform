package com.apimock.entity.project;

/**
 * 项目环境枚举
 *
 * @author AI Assistant
 */
public enum ProjectEnvironment {
    DEVELOPMENT("development", "开发环境"),
    TESTING("testing", "测试环境"),
    STAGING("staging", "预发布环境"),
    PRODUCTION("production", "生产环境");

    private final String code;
    private final String description;

    ProjectEnvironment(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ProjectEnvironment fromCode(String code) {
        for (ProjectEnvironment env : values()) {
            if (env.getCode().equals(code)) {
                return env;
            }
        }
        return DEVELOPMENT; // 默认返回开发环境
    }

    @Override
    public String toString() {
        return this.code;
    }
}