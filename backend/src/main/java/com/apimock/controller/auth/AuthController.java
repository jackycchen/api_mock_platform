package com.apimock.controller.auth;

import com.apimock.dto.ApiResponse;
import com.apimock.dto.auth.*;
import com.apimock.exception.AccountLockedException;
import com.apimock.exception.AuthenticationException;
import com.apimock.exception.PasswordValidationException;
import com.apimock.exception.UserAlreadyExistsException;
import com.apimock.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest registerRequest,
            HttpServletRequest request) {

        try {
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");

            RegisterResponse registerResponse = authService.register(registerRequest, ipAddress, userAgent);

            return ResponseEntity.ok(ApiResponse.success("注册成功", registerResponse));

        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(409, e.getMessage()));

        } catch (PasswordValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "系统内部错误"));
        }
    }

    /**
     * 检查用户名或邮箱是否可用
     */
    @GetMapping("/check-availability")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> checkAvailability(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email) {

        try {
            if (username != null && !username.trim().isEmpty()) {
                boolean available = authService.isUsernameAvailable(username);
                AvailabilityResponse response = new AvailabilityResponse(available, "username");
                String message = available ? "用户名可以使用" : "用户名已被占用";
                return ResponseEntity.ok(ApiResponse.success(message, response));
            }

            if (email != null && !email.trim().isEmpty()) {
                boolean available = authService.isEmailAvailable(email);
                AvailabilityResponse response = new AvailabilityResponse(available, "email");
                String message = available ? "邮箱可以使用" : "邮箱已被占用";
                return ResponseEntity.ok(ApiResponse.success(message, response));
            }

            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "请提供用户名或邮箱参数"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "系统内部错误"));
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {

        try {
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");

            LoginResponse loginResponse = authService.login(loginRequest, ipAddress, userAgent);

            return ResponseEntity.ok(ApiResponse.success("登录成功", loginResponse));

        } catch (AccountLockedException e) {
            return ResponseEntity.status(HttpStatus.LOCKED)
                    .body(ApiResponse.error(423, e.getMessage()));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, e.getMessage()));

        } catch (Exception e) {
            // 添加详细的异常日志记录
            System.err.println("Login error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "系统内部错误"));
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // TODO: 实现登出逻辑（清除Token等）
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Void>> getCurrentUser() {
        // TODO: 实现获取当前用户信息
        return ResponseEntity.ok(ApiResponse.successMessage("获取用户信息成功"));
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refreshToken() {
        // TODO: 实现Token刷新
        return ResponseEntity.ok(ApiResponse.successMessage("Token刷新成功"));
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
