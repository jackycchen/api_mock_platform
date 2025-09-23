-- 权限系统表结构
-- 权限表
CREATE TABLE permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限代码',
    name VARCHAR(100) NOT NULL COMMENT '权限名称',
    description VARCHAR(255) COMMENT '权限描述',
    category VARCHAR(50) NOT NULL COMMENT '权限分类',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_category (category),
    INDEX idx_enabled (enabled),
    INDEX idx_created_at (created_at)
) COMMENT='权限表';

-- 角色权限关联表
CREATE TABLE role_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    role VARCHAR(20) NOT NULL COMMENT '角色',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',

    INDEX idx_role (role),
    INDEX idx_permission_id (permission_id),
    INDEX idx_enabled (enabled),
    UNIQUE KEY uk_role_permission (role, permission_id),

    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
) COMMENT='角色权限关联表';

-- 用户操作日志表
CREATE TABLE user_operation_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    username VARCHAR(100) COMMENT '用户名',
    operation VARCHAR(50) NOT NULL COMMENT '操作类型',
    description VARCHAR(255) COMMENT '操作描述',
    resource VARCHAR(100) COMMENT '资源类型',
    resource_id BIGINT COMMENT '资源ID',
    result VARCHAR(50) NOT NULL COMMENT '操作结果',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    details TEXT COMMENT '详细信息',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_user_id (user_id),
    INDEX idx_operation (operation),
    INDEX idx_resource (resource, resource_id),
    INDEX idx_result (result),
    INDEX idx_created_at (created_at),

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT='用户操作日志表';

-- 初始化基础权限数据
INSERT INTO permissions (code, name, description, category) VALUES
-- 项目管理权限
('project:manage', '项目管理', '管理项目基本信息', 'project'),
('project:delete', '删除项目', '删除项目', 'project'),
('project:settings', '项目设置', '修改项目设置', 'project'),

-- 成员管理权限
('member:manage', '成员管理', '管理项目成员', 'member'),
('member:invite', '邀请成员', '邀请新成员加入项目', 'member'),
('member:remove', '移除成员', '从项目中移除成员', 'member'),
('member:role', '角色管理', '修改成员角色', 'member'),

-- API接口权限
('api:create', '创建接口', '创建新的API接口', 'api'),
('api:read', '查看接口', '查看API接口信息', 'api'),
('api:update', '修改接口', '修改API接口信息', 'api'),
('api:delete', '删除接口', '删除API接口', 'api'),
('api:test', '测试接口', '测试API接口', 'api'),

-- Mock规则权限
('mock:create', '创建Mock', '创建Mock规则', 'mock'),
('mock:read', '查看Mock', '查看Mock规则', 'mock'),
('mock:update', '修改Mock', '修改Mock规则', 'mock'),
('mock:delete', '删除Mock', '删除Mock规则', 'mock'),
('mock:test', '测试Mock', '测试Mock规则', 'mock'),

-- 文档权限
('doc:read', '查看文档', '查看API文档', 'doc'),
('doc:export', '导出文档', '导出API文档', 'doc'),
('doc:generate', '生成文档', '生成API文档', 'doc'),

-- 统计权限
('stats:view', '查看统计', '查看项目统计信息', 'stats'),
('stats:export', '导出统计', '导出统计数据', 'stats'),

-- 系统权限
('system:admin', '系统管理', '系统管理员权限', 'system');

-- 初始化角色权限关联
INSERT INTO role_permissions (role, permission_id)
SELECT 'OWNER', id FROM permissions WHERE category IN ('project', 'member', 'api', 'mock', 'doc', 'stats');

INSERT INTO role_permissions (role, permission_id)
SELECT 'ADMIN', id FROM permissions WHERE code IN (
    'project:settings', 'member:invite', 'member:remove', 'member:role',
    'api:create', 'api:read', 'api:update', 'api:delete', 'api:test',
    'mock:create', 'mock:read', 'mock:update', 'mock:delete', 'mock:test',
    'doc:read', 'doc:export', 'doc:generate', 'stats:view'
);

INSERT INTO role_permissions (role, permission_id)
SELECT 'DEVELOPER', id FROM permissions WHERE code IN (
    'api:create', 'api:read', 'api:update', 'api:delete', 'api:test',
    'mock:create', 'mock:read', 'mock:update', 'mock:delete', 'mock:test',
    'doc:read', 'doc:export'
);

INSERT INTO role_permissions (role, permission_id)
SELECT 'VIEWER', id FROM permissions WHERE code IN (
    'api:read', 'mock:read', 'doc:read'
);