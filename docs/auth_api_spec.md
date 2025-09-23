# 用户认证API接口规范

## 登录接口

### POST /api/v1/auth/login

**请求参数：**
```json
{
    "username": "string",  // 用户名或邮箱
    "password": "string",  // 密码
    "rememberMe": "boolean"  // 可选，是否记住登录状态
}
```

**成功响应：**
```json
{
    "code": 200,
    "message": "登录成功",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "tokenType": "Bearer",
        "expiresIn": 86400,
        "user": {
            "id": 1,
            "username": "admin",
            "email": "admin@example.com",
            "role": "SUPER_ADMIN"
        }
    },
    "timestamp": "2025-09-19T10:30:00"
}
```

**失败响应：**
```json
{
    "code": 401,
    "message": "用户名或密码错误",
    "timestamp": "2025-09-19T10:30:00"
}
```

**账户锁定响应：**
```json
{
    "code": 423,
    "message": "账户已被锁定，请30分钟后重试",
    "data": {
        "lockedUntil": "2025-09-19T11:00:00"
    },
    "timestamp": "2025-09-19T10:30:00"
}
```

## 获取当前用户信息

### GET /api/v1/auth/me

**请求头：**
```
Authorization: Bearer {token}
```

**成功响应：**
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "id": 1,
        "username": "admin",
        "email": "admin@example.com",
        "role": "SUPER_ADMIN",
        "lastLoginAt": "2025-09-19T10:30:00"
    },
    "timestamp": "2025-09-19T10:30:00"
}
```

## 登出接口

### POST /api/v1/auth/logout

**请求头：**
```
Authorization: Bearer {token}
```

**成功响应：**
```json
{
    "code": 200,
    "message": "登出成功",
    "timestamp": "2025-09-19T10:30:00"
}
```

## 刷新Token

### POST /api/v1/auth/refresh

**请求头：**
```
Authorization: Bearer {token}
```

**成功响应：**
```json
{
    "code": 200,
    "message": "Token刷新成功",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "tokenType": "Bearer",
        "expiresIn": 86400
    },
    "timestamp": "2025-09-19T10:30:00"
}
```