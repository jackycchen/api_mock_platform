-- 创建代理配置表
CREATE TABLE IF NOT EXISTS proxy_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    path_pattern VARCHAR(255) NOT NULL,
    mode VARCHAR(20) NOT NULL,
    target_url VARCHAR(255),
    forward_headers TEXT,
    preserve_host BOOLEAN DEFAULT FALSE,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_proxy_config_project FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT uq_proxy_config_project_path UNIQUE (project_id, path_pattern)
);
