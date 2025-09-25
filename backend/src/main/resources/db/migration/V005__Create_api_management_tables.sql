-- API 分组表
CREATE TABLE api_groups (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_api_group_project_name (project_id, name),
    CONSTRAINT fk_api_group_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
) COMMENT='API 分组表';

-- API 定义表
CREATE TABLE api_definitions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    group_id BIGINT NULL,
    name VARCHAR(150) NOT NULL,
    method VARCHAR(16) NOT NULL,
    path VARCHAR(255) NOT NULL,
    description TEXT,
    status TINYINT DEFAULT 1,
    sort_order INT DEFAULT 0,
    latest_version INT DEFAULT 1,
    version_count INT DEFAULT 1,
    request_headers LONGTEXT,
    path_parameters LONGTEXT,
    query_parameters LONGTEXT,
    request_body_schema LONGTEXT,
    response_schema LONGTEXT,
    response_examples LONGTEXT,
    metadata LONGTEXT,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    UNIQUE KEY uk_api_project_method_path (project_id, method, path),
    INDEX idx_api_project (project_id),
    INDEX idx_api_group (group_id),
    INDEX idx_api_method (method),
    INDEX idx_api_status (status),
    CONSTRAINT fk_api_definition_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_api_definition_group FOREIGN KEY (group_id) REFERENCES api_groups(id) ON DELETE SET NULL,
    CONSTRAINT fk_api_definition_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_api_definition_updated_by FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
) COMMENT='API 定义表';

-- API 版本表
CREATE TABLE api_definition_versions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    api_id BIGINT NOT NULL,
    version_number INT NOT NULL,
    name VARCHAR(150) NOT NULL,
    method VARCHAR(16) NOT NULL,
    path VARCHAR(255) NOT NULL,
    description TEXT,
    request_headers LONGTEXT,
    path_parameters LONGTEXT,
    query_parameters LONGTEXT,
    request_body_schema LONGTEXT,
    response_schema LONGTEXT,
    response_examples LONGTEXT,
    metadata LONGTEXT,
    status TINYINT DEFAULT 1,
    group_id BIGINT NULL,
    change_summary VARCHAR(255),
    created_by BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_api_version (api_id, version_number),
    INDEX idx_api_version_api (api_id),
    CONSTRAINT fk_api_version_definition FOREIGN KEY (api_id) REFERENCES api_definitions(id) ON DELETE CASCADE,
    CONSTRAINT fk_api_version_group FOREIGN KEY (group_id) REFERENCES api_groups(id) ON DELETE SET NULL,
    CONSTRAINT fk_api_version_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
) COMMENT='API 版本表';

-- API 分组初始数据（默认未分组）
INSERT INTO api_groups (project_id, name, description, sort_order)
SELECT p.id, '未分组', '默认分组', 0 FROM projects p;
