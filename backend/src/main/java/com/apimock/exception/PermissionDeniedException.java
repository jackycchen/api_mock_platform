package com.apimock.exception;

/**
 * 权限拒绝异常
 *
 * @author AI Assistant
 */
public class PermissionDeniedException extends RuntimeException {

    private String permissionCode;
    private String resource;
    private Long resourceId;

    public PermissionDeniedException(String message) {
        super(message);
    }

    public PermissionDeniedException(String message, Throwable cause) {
        super(message, cause);
    }

    public PermissionDeniedException(String message, String permissionCode) {
        super(message);
        this.permissionCode = permissionCode;
    }

    public PermissionDeniedException(String message, String permissionCode, String resource, Long resourceId) {
        super(message);
        this.permissionCode = permissionCode;
        this.resource = resource;
        this.resourceId = resourceId;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }
}