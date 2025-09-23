-- 插入测试用户数据
INSERT INTO users (username, email, password_hash, role, status, created_at, updated_at) VALUES
('admin', 'admin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J6hMSDMfpUE4KYXJSZpOLqPQ4FepzC', 'SUPER_ADMIN', 1, NOW(), NOW()),
('testuser', 'test@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J6hMSDMfpUE4KYXJSZpOLqPQ4FepzC', 'DEVELOPER', 1, NOW(), NOW());

-- 密码都是: password123