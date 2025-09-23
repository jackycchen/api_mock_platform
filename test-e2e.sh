#!/bin/bash

# 端到端测试脚本 - 登录API验证
echo "=== API Mock Platform 端到端测试 ==="
echo "测试开始时间: $(date)"
echo ""

# 测试配置
BACKEND_URL="http://localhost:8080"
FRONTEND_URL="http://localhost:3000"

# 测试用户数据
TEST_USERNAME="testuser"
TEST_PASSWORD="password123"
TEST_EMAIL="test@example.com"

echo "1. 检查服务状态..."

# 检查后端健康状态
echo "   检查后端服务 ($BACKEND_URL)..."
if curl -sf "$BACKEND_URL/actuator/health" > /dev/null 2>&1; then
    echo "   ✅ 后端服务正常"
else
    echo "   ❌ 后端服务未启动"
    echo "   请先启动后端服务"
    exit 1
fi

# 检查前端服务
echo "   检查前端服务 ($FRONTEND_URL)..."
if curl -sf "$FRONTEND_URL" > /dev/null 2>&1; then
    echo "   ✅ 前端服务正常"
else
    echo "   ❌ 前端服务未启动"
    echo "   请先启动前端服务"
    exit 1
fi

echo ""
echo "2. 测试用户注册..."

# 测试用户注册
REGISTER_RESPONSE=$(curl -s -w "%{http_code}" -o /tmp/register_response.json \
    -X POST "$BACKEND_URL/api/v1/auth/register" \
    -H "Content-Type: application/json" \
    -d "{
        \"username\": \"$TEST_USERNAME\",
        \"email\": \"$TEST_EMAIL\",
        \"password\": \"$TEST_PASSWORD\"
    }")

REGISTER_HTTP_CODE=${REGISTER_RESPONSE: -3}
if [[ "$REGISTER_HTTP_CODE" == "200" || "$REGISTER_HTTP_CODE" == "409" ]]; then
    if [[ "$REGISTER_HTTP_CODE" == "200" ]]; then
        echo "   ✅ 用户注册成功"
    else
        echo "   ℹ️  用户已存在，跳过注册"
    fi
else
    echo "   ❌ 用户注册失败 (HTTP $REGISTER_HTTP_CODE)"
    cat /tmp/register_response.json
    echo ""
fi

echo ""
echo "3. 测试用户登录..."

# 测试用户登录
LOGIN_RESPONSE=$(curl -s -w "%{http_code}" -o /tmp/login_response.json \
    -X POST "$BACKEND_URL/api/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d "{
        \"username\": \"$TEST_USERNAME\",
        \"password\": \"$TEST_PASSWORD\",
        \"rememberMe\": false
    }")

LOGIN_HTTP_CODE=${LOGIN_RESPONSE: -3}
if [[ "$LOGIN_HTTP_CODE" == "200" ]]; then
    echo "   ✅ 登录成功"

    # 提取JWT Token
    TOKEN=$(cat /tmp/login_response.json | grep -o '"token":"[^"]*' | cut -d'"' -f4)
    if [[ -n "$TOKEN" ]]; then
        echo "   ✅ JWT Token获取成功"
        echo "   Token前缀: ${TOKEN:0:50}..."
    else
        echo "   ⚠️  JWT Token未找到"
    fi
else
    echo "   ❌ 登录失败 (HTTP $LOGIN_HTTP_CODE)"
    cat /tmp/login_response.json
    echo ""
fi

echo ""
echo "4. 测试认证保护的API..."

if [[ -n "$TOKEN" ]]; then
    # 测试项目设置API (需要认证)
    SETTINGS_RESPONSE=$(curl -s -w "%{http_code}" -o /tmp/settings_response.json \
        -X GET "$BACKEND_URL/api/v1/projects/1/settings" \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json")

    SETTINGS_HTTP_CODE=${SETTINGS_RESPONSE: -3}
    if [[ "$SETTINGS_HTTP_CODE" == "200" ]]; then
        echo "   ✅ 项目设置API访问成功"
    elif [[ "$SETTINGS_HTTP_CODE" == "404" ]]; then
        echo "   ℹ️  项目不存在（预期行为）"
    else
        echo "   ⚠️  项目设置API响应异常 (HTTP $SETTINGS_HTTP_CODE)"
    fi
else
    echo "   ⏭️  跳过认证API测试（无有效Token）"
fi

echo ""
echo "5. 测试前端页面访问..."

# 测试前端页面
FRONTEND_RESPONSE=$(curl -s -w "%{http_code}" -o /dev/null "$FRONTEND_URL")
FRONTEND_HTTP_CODE=${FRONTEND_RESPONSE: -3}

if [[ "$FRONTEND_HTTP_CODE" == "200" ]]; then
    echo "   ✅ 前端页面访问正常"
else
    echo "   ❌ 前端页面访问失败 (HTTP $FRONTEND_HTTP_CODE)"
fi

echo ""
echo "6. 测试代理配置..."

# 测试前端到后端的代理
PROXY_RESPONSE=$(curl -s -w "%{http_code}" -o /tmp/proxy_response.json \
    -X POST "$FRONTEND_URL/api/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d "{
        \"username\": \"$TEST_USERNAME\",
        \"password\": \"$TEST_PASSWORD\"
    }")

PROXY_HTTP_CODE=${PROXY_RESPONSE: -3}
if [[ "$PROXY_HTTP_CODE" == "200" ]]; then
    echo "   ✅ 前端代理配置正常"
else
    echo "   ❌ 前端代理配置失败 (HTTP $PROXY_HTTP_CODE)"
fi

echo ""
echo "=== 测试总结 ==="
echo "测试完成时间: $(date)"

# 清理临时文件
rm -f /tmp/register_response.json /tmp/login_response.json /tmp/settings_response.json /tmp/proxy_response.json

if [[ "$LOGIN_HTTP_CODE" == "200" && "$FRONTEND_HTTP_CODE" == "200" ]]; then
    echo "🎉 核心功能测试通过！"
    echo ""
    echo "可以继续测试："
    echo "1. 访问 $FRONTEND_URL 进行前端测试"
    echo "2. 访问 $BACKEND_URL/swagger-ui.html 查看API文档"
    echo "3. 测试项目设置功能"
    exit 0
else
    echo "❌ 测试发现问题，请检查服务状态"
    exit 1
fi