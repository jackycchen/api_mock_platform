-- Mock规则表
CREATE TABLE mock_rules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    api_id BIGINT NOT NULL COMMENT 'API接口ID',
    name VARCHAR(100) NOT NULL COMMENT 'Mock规则名称',
    description TEXT COMMENT 'Mock规则描述',

    -- Mock配置
    status_code INTEGER DEFAULT 200 COMMENT 'HTTP状态码',
    delay_ms INTEGER DEFAULT 0 COMMENT '延迟时间（毫秒）',
    headers TEXT COMMENT '响应头配置 JSON格式',

    -- Mock数据配置
    mock_type ENUM('STATIC', 'DYNAMIC', 'TEMPLATE') DEFAULT 'STATIC' COMMENT 'Mock类型：静态/动态/模板',
    static_data TEXT COMMENT '静态数据内容',
    dynamic_config TEXT COMMENT '动态数据配置 JSON格式',
    template_id BIGINT COMMENT '模板ID（可选）',

    -- 规则配置
    is_enabled TINYINT DEFAULT 1 COMMENT '是否启用',
    priority INTEGER DEFAULT 0 COMMENT '优先级，数值越大优先级越高',
    match_condition TEXT COMMENT '匹配条件配置 JSON格式',

    -- 审计字段
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    created_by BIGINT COMMENT '创建人',
    updated_by BIGINT COMMENT '更新人',

    -- 索引
    INDEX idx_api_id (api_id),
    INDEX idx_enabled (is_enabled),
    INDEX idx_priority (priority),
    INDEX idx_created_at (created_at),

    -- 外键约束
    FOREIGN KEY (api_id) REFERENCES apis(id),
    FOREIGN KEY (template_id) REFERENCES mock_templates(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
) COMMENT='Mock规则配置表';

-- Mock模板表（可选，用于复用Mock数据）
CREATE TABLE mock_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL COMMENT '项目ID',
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    description TEXT COMMENT '模板描述',
    category VARCHAR(50) COMMENT '模板分类',

    -- 模板内容
    template_data TEXT NOT NULL COMMENT '模板数据内容 JSON格式',
    schema_definition TEXT COMMENT 'JSON Schema定义',

    -- 标签和分类
    tags VARCHAR(500) COMMENT '标签列表，逗号分隔',

    -- 审计字段
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    created_by BIGINT COMMENT '创建人',

    -- 索引
    INDEX idx_project_id (project_id),
    INDEX idx_category (category),
    INDEX idx_created_at (created_at),

    -- 外键约束
    FOREIGN KEY (project_id) REFERENCES projects(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
) COMMENT='Mock数据模板表';

-- Mock调用日志表（用于统计和监控）
CREATE TABLE mock_call_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    mock_rule_id BIGINT NOT NULL COMMENT 'Mock规则ID',
    api_id BIGINT NOT NULL COMMENT 'API接口ID',

    -- 请求信息
    request_method VARCHAR(10) NOT NULL COMMENT '请求方法',
    request_path VARCHAR(500) NOT NULL COMMENT '请求路径',
    request_headers TEXT COMMENT '请求头',
    request_body TEXT COMMENT '请求体',
    request_params TEXT COMMENT '请求参数',

    -- 响应信息
    response_status INTEGER NOT NULL COMMENT '响应状态码',
    response_headers TEXT COMMENT '响应头',
    response_body TEXT COMMENT '响应体',
    response_time INTEGER COMMENT '响应时间（毫秒）',

    -- 元信息
    client_ip VARCHAR(45) COMMENT '客户端IP',
    user_agent TEXT COMMENT 'User Agent',

    -- 时间戳
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- 索引
    INDEX idx_mock_rule_id (mock_rule_id),
    INDEX idx_api_id (api_id),
    INDEX idx_created_at (created_at),
    INDEX idx_response_status (response_status),

    -- 外键约束
    FOREIGN KEY (mock_rule_id) REFERENCES mock_rules(id),
    FOREIGN KEY (api_id) REFERENCES apis(id)
) COMMENT='Mock调用日志表';