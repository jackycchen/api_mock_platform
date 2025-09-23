package com.apimock.service.auth;

import com.apimock.dto.auth.LoginRequest;
import com.apimock.dto.auth.LoginResponse;
import com.apimock.dto.auth.RegisterRequest;
import com.apimock.dto.auth.RegisterResponse;
import com.apimock.entity.auth.User;
import com.apimock.entity.auth.UserRole;
import com.apimock.exception.AccountLockedException;
import com.apimock.exception.AuthenticationException;
import com.apimock.exception.PasswordValidationException;
import com.apimock.exception.UserAlreadyExistsException;
import com.apimock.repository.UserRepository;
import com.apimock.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final int LOCK_TIME_DURATION = 30; // 30分钟

    // 密码强度验证正则：至少8位，包含字母和数字
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d).{8,}$");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public RegisterResponse register(RegisterRequest registerRequest, String ipAddress, String userAgent) {
        // 1. 验证密码确认
        if (!registerRequest.isPasswordConfirmed()) {
            throw new PasswordValidationException("两次输入的密码不一致");
        }

        // 2. 验证密码强度
        if (!validatePasswordStrength(registerRequest.getPassword())) {
            throw new PasswordValidationException("密码必须至少8位，包含字母和数字");
        }

        // 3. 检查用户名是否已存在
        if (!isUsernameAvailable(registerRequest.getUsername())) {
            throw new UserAlreadyExistsException("用户名已存在", "username");
        }

        // 4. 检查邮箱是否已存在
        if (!isEmailAvailable(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException("邮箱已被注册", "email");
        }

        // 5. 创建新用户
        User newUser = new User();
        newUser.setUsername(registerRequest.getUsername());
        newUser.setEmail(registerRequest.getEmail());
        newUser.setPasswordHash(encodePassword(registerRequest.getPassword()));
        newUser.setRole(UserRole.DEVELOPER); // 默认角色为开发者
        newUser.setStatus(1); // 启用状态

        // 6. 保存用户
        User savedUser = userRepository.save(newUser);

        // 7. 记录注册日志
        // TODO: 实现注册日志记录

        // 8. 构建响应
        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    @Override
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean validatePasswordStrength(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest, String ipAddress, String userAgent) {
        String usernameOrEmail = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        // 查找用户
        User user = findByUsernameOrEmail(usernameOrEmail);
        if (user == null) {
            handleLoginFailure(null, usernameOrEmail, ipAddress, userAgent, "用户不存在");
            throw new AuthenticationException("用户名或密码错误");
        }

        // 检查用户状态
        if (!user.isActive()) {
            handleLoginFailure(user, usernameOrEmail, ipAddress, userAgent, "账户已禁用");
            throw new AuthenticationException("账户已被禁用");
        }

        // 检查账户是否被锁定
        if (isAccountLocked(user)) {
            handleLoginFailure(user, usernameOrEmail, ipAddress, userAgent, "账户被锁定");
            throw new AccountLockedException("账户已被锁定，请稍后重试", user.getLockedUntil());
        }

        // 验证密码
        if (!validatePassword(password, user.getPasswordHash())) {
            handleLoginFailure(user, usernameOrEmail, ipAddress, userAgent, "密码错误");

            // 增加失败次数
            int failedAttempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(failedAttempts);
            userRepository.updateFailedLoginAttempts(user.getId(), failedAttempts, LocalDateTime.now());

            // 检查是否需要锁定账户
            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                lockAccount(user);
                throw new AccountLockedException("登录失败次数过多，账户已被锁定30分钟", user.getLockedUntil());
            }

            throw new AuthenticationException("用户名或密码错误");
        }

        // 登录成功
        handleLoginSuccess(user, ipAddress, userAgent);

        // 生成JWT Token
        String token = jwtUtil.generateToken(user.getUsername(), user.getId());

        // 构建响应
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getId(), user.getUsername(), user.getEmail(), user.getRole()
        );

        return new LoginResponse(token, jwtUtil.getExpirationSeconds(), userInfo);
    }

    @Override
    public User findByUsernameOrEmail(String usernameOrEmail) {
        Optional<User> userOpt = userRepository.findByUsernameOrEmail(usernameOrEmail);
        return userOpt.orElse(null);
    }

    @Override
    public User findByUsername(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        return userOpt.orElse(null);
    }

    @Override
    public boolean validatePassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }

    @Override
    public void handleLoginSuccess(User user, String ipAddress, String userAgent) {
        // 更新最后登录时间，重置失败次数
        userRepository.updateLastLoginTime(user.getId(), LocalDateTime.now(), LocalDateTime.now());

        // 解锁账户（如果之前被锁定）
        if (user.getLockedUntil() != null) {
            unlockAccount(user);
        }

        // 记录登录日志
        // TODO: 实现登录日志记录
    }

    @Override
    public void handleLoginFailure(User user, String username, String ipAddress, String userAgent, String reason) {
        // 记录失败日志
        // TODO: 实现登录失败日志记录
    }

    @Override
    public boolean isAccountLocked(User user) {
        return user.isLocked();
    }

    @Override
    public void lockAccount(User user) {
        LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(LOCK_TIME_DURATION);
        userRepository.lockUser(user.getId(), lockUntil, LocalDateTime.now());
        user.setLockedUntil(lockUntil);
    }

    @Override
    public void unlockAccount(User user) {
        userRepository.unlockUser(user.getId(), LocalDateTime.now());
        user.setLockedUntil(null);
        user.setFailedLoginAttempts(0);
    }
}