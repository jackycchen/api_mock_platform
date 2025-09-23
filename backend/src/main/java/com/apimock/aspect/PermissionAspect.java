package com.apimock.aspect;

import com.apimock.annotation.RequirePermission;
import com.apimock.entity.project.ProjectMember;
import com.apimock.entity.project.ProjectRole;
import com.apimock.entity.auth.User;
import com.apimock.exception.PermissionDeniedException;
import com.apimock.service.auth.PermissionService;
import com.apimock.service.auth.UserOperationLogService;
import com.apimock.service.project.ProjectMemberService;
import com.apimock.util.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Optional;

/**
 * 权限检查切面
 * 基于注解的权限控制实现
 *
 * @author AI Assistant
 */
@Aspect
@Component
@Order(100)
public class PermissionAspect {

    private static final Logger logger = LoggerFactory.getLogger(PermissionAspect.class);

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private ProjectMemberService projectMemberService;

    @Autowired
    private UserOperationLogService operationLogService;

    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        try {
            // 获取当前用户
            User currentUser = SecurityUtils.getCurrentUser();
            if (currentUser == null) {
                throw new PermissionDeniedException("用户未登录");
            }

            // 获取项目ID
            Long projectId = extractProjectId(joinPoint, requirePermission.projectIdParam());

            // 检查是否需要项目成员身份
            ProjectRole userRole = null;
            if (requirePermission.requireProjectMember() && projectId != null) {
                Optional<ProjectMember> memberOpt = projectMemberService.getProjectMember(projectId, currentUser.getId());
                if (memberOpt.isEmpty()) {
                    logPermissionDenied(currentUser, "非项目成员", requirePermission.value());
                    throw new PermissionDeniedException("您不是该项目的成员");
                }
                userRole = memberOpt.get().getRole();
            }

            // 检查权限
            if (requirePermission.value().length > 0) {
                boolean hasPermission = checkUserPermissions(userRole, requirePermission);
                if (!hasPermission) {
                    logPermissionDenied(currentUser, "权限不足", requirePermission.value());
                    throw new PermissionDeniedException(requirePermission.message());
                }
            }

            // 权限检查通过，执行目标方法
            return joinPoint.proceed();

        } catch (PermissionDeniedException e) {
            throw e;
        } catch (Exception e) {
            logger.error("权限检查异常", e);
            throw new PermissionDeniedException("权限检查失败");
        }
    }

    /**
     * 从方法参数中提取项目ID
     */
    private Long extractProjectId(ProceedingJoinPoint joinPoint, String projectIdParam) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Parameter[] parameters = method.getParameters();
            Object[] args = joinPoint.getArgs();

            // 首先尝试从路径参数获取
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String pathProjectId = request.getParameter("projectId");
                if (pathProjectId != null && !pathProjectId.isEmpty()) {
                    return Long.parseLong(pathProjectId);
                }

                // 从路径变量中获取
                String requestURI = request.getRequestURI();
                if (requestURI.contains("/projects/")) {
                    String[] parts = requestURI.split("/");
                    for (int i = 0; i < parts.length - 1; i++) {
                        if ("projects".equals(parts[i]) && i + 1 < parts.length) {
                            try {
                                return Long.parseLong(parts[i + 1]);
                            } catch (NumberFormatException e) {
                                // 继续查找
                            }
                        }
                    }
                }
            }

            // 从方法参数中查找
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                if (projectIdParam.equals(parameter.getName()) || "projectId".equals(parameter.getName())) {
                    Object arg = args[i];
                    if (arg instanceof Long) {
                        return (Long) arg;
                    } else if (arg instanceof String) {
                        return Long.parseLong((String) arg);
                    } else if (arg instanceof Integer) {
                        return ((Integer) arg).longValue();
                    }
                }
            }

            return null;
        } catch (Exception e) {
            logger.debug("提取项目ID失败", e);
            return null;
        }
    }

    /**
     * 检查用户权限
     */
    private boolean checkUserPermissions(ProjectRole userRole, RequirePermission requirePermission) {
        if (userRole == null) {
            return false;
        }

        String[] requiredPermissions = requirePermission.value();
        if (requiredPermissions.length == 0) {
            return true;
        }

        if (requirePermission.type() == RequirePermission.CheckType.ALL) {
            // 必须拥有所有权限
            return permissionService.hasAllPermissions(userRole, requiredPermissions);
        } else {
            // 拥有任意一个权限即可
            return permissionService.hasAnyPermission(userRole, requiredPermissions);
        }
    }

    /**
     * 记录权限拒绝日志
     */
    private void logPermissionDenied(User user, String reason, String[] permissions) {
        try {
            String description = String.format("权限被拒绝: %s, 需要权限: %s", reason, Arrays.toString(permissions));
            operationLogService.logFailedOperation(
                user.getId(),
                user.getUsername(),
                UserOperationLogService.Operations.PERMISSION_DENIED,
                description,
                reason
            );
        } catch (Exception e) {
            logger.error("记录权限拒绝日志失败", e);
        }
    }
}