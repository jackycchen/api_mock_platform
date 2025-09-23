<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>API Mock Platform</h2>
          <p>登录您的账户</p>
        </div>
      </template>

      <el-form
        ref="loginFormRef"
        :model="form"
        :rules="rules"
        label-width="0"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名或邮箱"
            prefix-icon="User"
            size="large"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="form.rememberMe">
            记住我
          </el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 测试账户提示 -->
      <div class="test-accounts">
        <el-divider>测试账户</el-divider>
        <div class="account-tips">
          <p @click="fillTestAccount('testuser2', 'Test123456')">
            <strong>开发者:</strong> testuser2 / Test123456
            <el-icon class="click-icon">
              <Mouse />
            </el-icon>
          </p>
        </div>
        <p class="tips-text">
          点击上方账户信息可自动填充登录表单
        </p>
      </div>

      <!-- 注册链接 -->
      <div class="register-link">
        还没有账户？
        <router-link
          to="/register"
          class="link"
        >
          立即注册
        </router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { Mouse } from '@element-plus/icons-vue'
import { login } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const loginFormRef = ref()

const form = reactive({
  username: '',
  password: '',
  rememberMe: false
})

const rules = {
  username: [
    { required: true, message: '请输入用户名或邮箱', trigger: 'blur' },
    { min: 3, message: '用户名长度不能少于3位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  // 表单验证
  if (!loginFormRef.value) return

  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true

  try {
    const response = await login({
      username: form.username,
      password: form.password,
      rememberMe: form.rememberMe
    })

    if (response.code === 200) {
      // 登录成功
      const { token, user } = response.data

      // 保存用户信息和Token
      userStore.setToken(token)
      userStore.setUserInfo(user)

      ElMessage.success('登录成功！')

      // 跳转到首页
      router.push('/')
    } else {
      ElMessage.error(response.message || '登录失败')
    }

  } catch (error) {
    console.error('登录错误:', error)

    if (error.response) {
      const { status, data } = error.response

      if (status === 401) {
        ElMessage.error(data.message || '用户名或密码错误')
      } else if (status === 423) {
        ElMessage.error(data.message || '账户已被锁定')
      } else {
        ElMessage.error(data.message || '登录失败，请稍后重试')
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
  } finally {
    loading.value = false
  }
}

// 填充测试账户信息
const fillTestAccount = (username, password) => {
  form.username = username
  form.password = password
  ElMessage.success('测试账户信息已自动填充')
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0 0 10px 0;
  color: #303133;
}

.card-header p {
  margin: 0;
  color: #909399;
}

.test-accounts {
  margin-top: 20px;
}

.account-tips {
  font-size: 12px;
  color: #909399;
  line-height: 1.8;
}

.account-tips p {
  margin: 5px 0;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: all 0.3s;
  border: 1px solid #e4e7ed;
  background-color: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.account-tips p:hover {
  background-color: #e6f7ff;
  color: #409eff;
  border-color: #409eff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.click-icon {
  font-size: 14px;
  opacity: 0.6;
}

.tips-text {
  font-size: 11px;
  color: #c0c4cc;
  text-align: center;
  margin-top: 8px;
  font-style: italic;
}

.register-link {
  text-align: center;
  margin-top: 16px;
  color: #909399;
  font-size: 14px;
}

.link {
  color: #409eff;
  text-decoration: none;
  font-weight: 500;
}

.link:hover {
  color: #66b1ff;
}
</style>