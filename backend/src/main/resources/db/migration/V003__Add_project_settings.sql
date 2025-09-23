-- 添加项目设置相关字段
-- 为项目表增加环境配置、代理设置、CORS配置等字段

-- 首先检查projects表是否存在，如果不存在则创建
CREATE TABLE IF NOT EXISTS projects (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '项目名称',
    description TEXT COMMENT '项目描述',
    type ENUM('WEB_API', 'MOBILE_APP', 'MICROSERVICE', 'OTHER') DEFAULT 'WEB_API' COMMENT '项目类型',
    visibility ENUM('PRIVATE', 'PUBLIC') DEFAULT 'PRIVATE' COMMENT '项目可见性',
    project_tags VARCHAR(500) COMMENT '项目标签',
    status ENUM('ACTIVE', 'ARCHIVED', 'DELETED') DEFAULT 'ACTIVE' COMMENT '项目状态',
    owner_id BIGINT NOT NULL COMMENT '项目所有者ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL COMMENT '删除时间（软删除）',

    INDEX idx_name (name),
    INDEX idx_owner_id (owner_id),
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_deleted_at (deleted_at)
) COMMENT='项目表';

-- 添加项目设置相关字段
ALTER TABLE projects
ADD COLUMN IF NOT EXISTS environment ENUM('DEVELOPMENT', 'TESTING', 'STAGING', 'PRODUCTION') DEFAULT 'DEVELOPMENT' COMMENT '环境类型',
ADD COLUMN IF NOT EXISTS base_url VARCHAR(255) COMMENT '基础URL',
ADD COLUMN IF NOT EXISTS mock_domain VARCHAR(255) COMMENT 'Mock域名',
ADD COLUMN IF NOT EXISTS proxy_enabled BOOLEAN DEFAULT FALSE COMMENT '是否启用代理',
ADD COLUMN IF NOT EXISTS proxy_target VARCHAR(255) COMMENT '代理目标地址',
ADD COLUMN IF NOT EXISTS allow_cors BOOLEAN DEFAULT TRUE COMMENT '是否允许跨域',
ADD COLUMN IF NOT EXISTS cors_origins VARCHAR(500) DEFAULT '*' COMMENT '允许的跨域域名',
ADD COLUMN IF NOT EXISTS request_timeout INT DEFAULT 30000 COMMENT '请求超时时间（毫秒）',
ADD COLUMN IF NOT EXISTS rate_limit INT DEFAULT 1000 COMMENT '速率限制（次/分钟）',
ADD COLUMN IF NOT EXISTS enable_logging BOOLEAN DEFAULT TRUE COMMENT '是否启用请求日志',
ADD COLUMN IF NOT EXISTS enable_cache BOOLEAN DEFAULT TRUE COMMENT '是否启用数据缓存',
ADD COLUMN IF NOT EXISTS cache_ttl INT DEFAULT 3600 COMMENT '缓存时间（秒）';

-- 创建项目成员表（如果不存在）
CREATE TABLE IF NOT EXISTS project_members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL COMMENT '项目ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role ENUM('OWNER', 'ADMIN', 'DEVELOPER', 'OBSERVER') DEFAULT 'DEVELOPER' COMMENT '角色',
    status ENUM('ACTIVE', 'PENDING', 'SUSPENDED') DEFAULT 'ACTIVE' COMMENT '状态',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    invited_by BIGINT COMMENT '邀请人ID',
    invite_token VARCHAR(255) COMMENT '邀请令牌',
    invite_expires_at TIMESTAMP COMMENT '邀请过期时间',
    last_active_at TIMESTAMP COMMENT '最后活跃时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_project_user (project_id, user_id),
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (invited_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_project_id (project_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role (role),
    INDEX idx_status (status),
    INDEX idx_invite_token (invite_token)
) COMMENT='项目成员表';

-- 创建项目角色表（预定义角色）
CREATE TABLE IF NOT EXISTS project_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) UNIQUE NOT NULL COMMENT '角色代码',
    name VARCHAR(100) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) COMMENT '角色描述',
    permissions JSON COMMENT '权限列表',
    is_system BOOLEAN DEFAULT FALSE COMMENT '是否为系统预定义角色',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_code (code),
    INDEX idx_is_system (is_system),
    INDEX idx_sort_order (sort_order)
) COMMENT='项目角色表';

-- 插入预定义角色
INSERT INTO project_roles (code, name, description, permissions, is_system, sort_order) VALUES
('OWNER', '所有者', '项目所有者，拥有所有权限', JSON_ARRAY('project:admin', 'project:write', 'project:read', 'member:admin', 'api:admin'), TRUE, 1),
('ADMIN', '管理员', '项目管理员，可以管理项目和成员', JSON_ARRAY('project:write', 'project:read', 'member:admin', 'api:admin'), TRUE, 2),
('DEVELOPER', '开发者', '开发者，可以管理API和Mock', JSON_ARRAY('project:read', 'api:admin', 'mock:admin'), TRUE, 3),
('OBSERVER', '观察者', '观察者，只有查看权限', JSON_ARRAY('project:read', 'api:read', 'mock:read'), TRUE, 4)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    description = VALUES(description),
    permissions = VALUES(permissions),
    updated_at = CURRENT_TIMESTAMP;

-- 创建项目操作日志表
CREATE TABLE IF NOT EXISTS project_operation_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL COMMENT '项目ID',
    user_id BIGINT COMMENT '操作用户ID',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    operation_target VARCHAR(100) COMMENT '操作目标',
    operation_details JSON COMMENT '操作详情',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent TEXT COMMENT '用户代理',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_project_id (project_id),
    INDEX idx_user_id (user_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_created_at (created_at)
) COMMENT='项目操作日志表';

-- 创建邀请记录表
CREATE TABLE IF NOT EXISTS project_invitations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL COMMENT '项目ID',
    inviter_id BIGINT NOT NULL COMMENT '邀请人ID',
    invitee_email VARCHAR(100) NOT NULL COMMENT '被邀请人邮箱',
    role VARCHAR(50) NOT NULL COMMENT '邀请角色',
    invite_token VARCHAR(255) UNIQUE NOT NULL COMMENT '邀请令牌',
    message TEXT COMMENT '邀请消息',
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED') DEFAULT 'PENDING' COMMENT '邀请状态',
    expires_at TIMESTAMP NOT NULL COMMENT '过期时间',
    accepted_at TIMESTAMP COMMENT '接受时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (inviter_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_project_id (project_id),
    INDEX idx_inviter_id (inviter_id),
    INDEX idx_invitee_email (invitee_email),
    INDEX idx_invite_token (invite_token),
    INDEX idx_status (status),
    INDEX idx_expires_at (expires_at)
) COMMENT='项目邀请记录表';