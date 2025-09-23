-- 项目成员表
CREATE TABLE project_members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role ENUM('OWNER', 'ADMIN', 'DEVELOPER', 'VIEWER') NOT NULL DEFAULT 'VIEWER' COMMENT '项目角色',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1活跃 0已移除',
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    removed_at TIMESTAMP NULL COMMENT '移除时间',

    -- 索引
    INDEX idx_project_id (project_id),
    INDEX idx_user_id (user_id),
    INDEX idx_project_user (project_id, user_id),
    INDEX idx_role (role),
    INDEX idx_status (status),
    INDEX idx_joined_at (joined_at),
    INDEX idx_removed_at (removed_at),

    -- 外键约束
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    -- 唯一约束
    UNIQUE KEY uk_project_user_active (project_id, user_id, removed_at)
) COMMENT='项目成员表';