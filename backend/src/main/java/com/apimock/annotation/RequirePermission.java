package com.apimock.annotation;

import java.lang.annotation.*;

/**
 * 权限检查注解
 * 用于方法级别的权限控制
 *
 * @author AI Assistant
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 所需的权限代码
     */
    String[] value() default {};

    /**
     * 权限检查类型
     * ANY: 拥有任意一个权限即可
     * ALL: 必须拥有所有权限
     */
    CheckType type() default CheckType.ANY;

    /**
     * 项目ID参数名称
     * 用于获取项目上下文中的项目ID
     */
    String projectIdParam() default "projectId";

    /**
     * 是否必须是项目成员
     */
    boolean requireProjectMember() default true;

    /**
     * 错误消息
     */
    String message() default "权限不足";

    enum CheckType {
        ANY,  // 任意一个权限
        ALL   // 所有权限
    }
}