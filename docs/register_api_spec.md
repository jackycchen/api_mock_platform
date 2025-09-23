# 用户注册API接口规范

## 注册接口

### POST /api/v1/auth/register

**请求参数：**
```json
{
    "username": "string",     // 用户名，3-50字符，唯一
    "email": "string",        // 邮箱地址，必须是有效邮箱格式，唯一
    "password": "string",     // 密码，8位以上，包含字母数字
    "confirmPassword": "string", // 确认密码，必须与password一致
    "phone": "string"         // 手机号，可选，11位数字
}
```

**成功响应：**
```json
{
    "code": 200,
    "message": "注册成功",
    "data": {
        "id": 3,
        "username": "newuser",
        "email": "newuser@example.com",
        "role": "DEVELOPER"
    },
    "timestamp": "2025-09-19T11:30:00"
}
```

**失败响应示例：**

**1. 参数验证失败：**
```json
{
    "code": 400,
    "message": "参数校验失败",
    "data": {
        "username": "用户名长度不能少于3位",
        "password": "密码必须包含字母和数字"
    },
    "timestamp": "2025-09-19T11:30:00"
}
```

**2. 用户名已存在：**
```json
{
    "code": 409,
    "message": "用户名已存在",
    "timestamp": "2025-09-19T11:30:00"
}
```

**3. 邮箱已存在：**
```json
{
    "code": 409,
    "message": "邮箱已被注册",
    "timestamp": "2025-09-19T11:30:00"
}
```

## 检查用户名/邮箱是否可用

### GET /api/v1/auth/check-availability

**请求参数：**
```
?username=testuser  或  ?email=test@example.com
```

**成功响应：**
```json
{
    "code": 200,
    "message": "可以使用",
    "data": {
        "available": true
    },
    "timestamp": "2025-09-19T11:30:00"
}
```

**已被占用响应：**
```json
{
    "code": 200,
    "message": "已被占用",
    "data": {
        "available": false
    },
    "timestamp": "2025-09-19T11:30:00"
}
```