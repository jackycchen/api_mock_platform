package com.apimock.dto.project;

import jakarta.validation.constraints.*;

/**
 * 项目转让请求DTO
 *
 * @author AI Assistant
 */
public class ProjectTransferRequest {

    @NotNull(message = "新所有者ID不能为空")
    private Long newOwnerId;

    @NotBlank(message = "新所有者用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度必须在2-50个字符之间")
    private String newOwnerUsername;

    @Size(max = 500, message = "转让说明不能超过500个字符")
    private String transferReason;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    // 构造函数
    public ProjectTransferRequest() {}

    public ProjectTransferRequest(Long newOwnerId, String newOwnerUsername, String transferReason) {
        this.newOwnerId = newOwnerId;
        this.newOwnerUsername = newOwnerUsername;
        this.transferReason = transferReason;
    }

    // Getters and Setters
    public Long getNewOwnerId() {
        return newOwnerId;
    }

    public void setNewOwnerId(Long newOwnerId) {
        this.newOwnerId = newOwnerId;
    }

    public String getNewOwnerUsername() {
        return newOwnerUsername;
    }

    public void setNewOwnerUsername(String newOwnerUsername) {
        this.newOwnerUsername = newOwnerUsername;
    }

    public String getTransferReason() {
        return transferReason;
    }

    public void setTransferReason(String transferReason) {
        this.transferReason = transferReason;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}