package com.apimock.service.auth;

import com.apimock.dto.auth.LoginRequest;
import com.apimock.dto.auth.LoginResponse;
import com.apimock.dto.auth.RegisterRequest;
import com.apimock.dto.auth.RegisterResponse;
import com.apimock.entity.auth.User;

public interface AuthService {

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest loginRequest, String ipAddress, String userAgent);

    /**
     * 用户注册
     */
    RegisterResponse register(RegisterRequest registerRequest, String ipAddress, String userAgent);

    /**
     * 检查用户名是否可用
     */
    boolean isUsernameAvailable(String username);

    /**
     * 检查邮箱是否可用
     */
    boolean isEmailAvailable(String email);

    /**
     * 根据用户名或邮箱查找用户
     */
    User findByUsernameOrEmail(String usernameOrEmail);

    /**
     * 根据用户名查找用户
     */
    User findByUsername(String username);

    /**
     * 验证密码
     */
    boolean validatePassword(String rawPassword, String hashedPassword);

    /**
     * 加密密码
     */
    String encodePassword(String rawPassword);

    /**
     * 验证密码强度
     */
    boolean validatePasswordStrength(String password);

    /**
     * 处理登录成功
     */
    void handleLoginSuccess(User user, String ipAddress, String userAgent);

    /**
     * 处理登录失败
     */
    void handleLoginFailure(User user, String username, String ipAddress, String userAgent, String reason);

    /**
     * 检查账户是否被锁定
     */
    boolean isAccountLocked(User user);

    /**
     * 锁定账户
     */
    void lockAccount(User user);

    /**
     * 解锁账户
     */
    void unlockAccount(User user);
}