package com.apimock.entity.project;

public enum ProjectVisibility {
    PUBLIC("公开", "所有人可见"),
    PRIVATE("私有", "仅项目成员可见"),
    INTERNAL("内部", "组织内部可见");

    private final String displayName;
    private final String description;

    ProjectVisibility(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static ProjectVisibility fromString(String visibility) {
        for (ProjectVisibility projectVisibility : ProjectVisibility.values()) {
            if (projectVisibility.name().equalsIgnoreCase(visibility)) {
                return projectVisibility;
            }
        }
        return PRIVATE;
    }
}