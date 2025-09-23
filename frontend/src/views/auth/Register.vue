<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <h2>用户注册</h2>
          <p class="subtitle">
            创建您的 API Mock Platform 账户
          </p>
        </div>
      </template>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="80px"
        @submit.prevent="handleRegister"
      >
        <!-- 用户名输入 -->
        <el-form-item
          label="用户名"
          prop="username"
        >
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名（3-20位字符）"
            clearable
            @blur="checkUsernameAvailability"
          >
            <template #suffix>
              <el-icon
                v-if="usernameCheckLoading"
                class="is-loading"
              >
                <Loading />
              </el-icon>
              <el-icon
                v-else-if="usernameCheckResult === 'available'"
                style="color: #67c23a"
              >
                <SuccessFilled />
              </el-icon>
              <el-icon
                v-else-if="usernameCheckResult === 'unavailable'"
                style="color: #f56c6c"
              >
                <CircleCloseFilled />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <!-- 邮箱输入 -->
        <el-form-item
          label="邮箱"
          prop="email"
        >
          <el-input
            v-model="registerForm.email"
            type="email"
            placeholder="请输入邮箱地址"
            clearable
            @blur="checkEmailAvailability"
          >
            <template #suffix>
              <el-icon
                v-if="emailCheckLoading"
                class="is-loading"
              >
                <Loading />
              </el-icon>
              <el-icon
                v-else-if="emailCheckResult === 'available'"
                style="color: #67c23a"
              >
                <SuccessFilled />
              </el-icon>
              <el-icon
                v-else-if="emailCheckResult === 'unavailable'"
                style="color: #f56c6c"
              >
                <CircleCloseFilled />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <!-- 密码输入 -->
        <el-form-item
          label="密码"
          prop="password"
        >
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码（至少8位，包含字母和数字）"
            show-password
            clearable
          />
          <div
            v-if="registerForm.password"
            class="password-strength"
          >
            <div class="strength-label">
              密码强度：
            </div>
            <div class="strength-bar">
              <div
                class="strength-fill"
                :class="passwordStrengthClass"
                :style="{ width: passwordStrengthWidth }"
              />
            </div>
            <div
              class="strength-text"
              :class="passwordStrengthClass"
            >
              {{ passwordStrengthText }}
            </div>
          </div>
        </el-form-item>

        <!-- 确认密码输入 -->
        <el-form-item
          label="确认密码"
          prop="confirmPassword"
        >
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
            clearable
          />
        </el-form-item>

        <!-- 注册按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%"
            :loading="registerLoading"
            :disabled="!isFormValid"
            @click="handleRegister"
          >
            注册账户
          </el-button>
        </el-form-item>

        <!-- 登录链接 -->
        <div class="login-link">
          已有账户？
          <router-link
            to="/login"
            class="link"
          >
            立即登录
          </router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Loading,
  SuccessFilled,
  CircleCloseFilled
} from '@element-plus/icons-vue'
import { register, checkAvailability } from '@/api/auth'

const router = useRouter()

// 表单引用
const registerFormRef = ref()

// 注册表单数据
const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

// 加载状态
const registerLoading = ref(false)
const usernameCheckLoading = ref(false)
const emailCheckLoading = ref(false)

// 可用性检查结果
const usernameCheckResult = ref('')
const emailCheckResult = ref('')

// 表单验证规则
const registerRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3到20个字符之间', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, message: '密码长度不能少于8位', trigger: 'blur' },
    {
      pattern: /^(?=.*[a-zA-Z])(?=.*\d).{8,}$/,
      message: '密码必须包含字母和数字，且至少8位',
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

// 密码强度计算
const passwordStrength = computed(() => {
  const password = registerForm.password
  if (!password) return 0

  let score = 0

  // 长度检查
  if (password.length >= 8) score += 20
  if (password.length >= 12) score += 10

  // 字符类型检查
  if (/[a-z]/.test(password)) score += 20
  if (/[A-Z]/.test(password)) score += 20
  if (/\d/.test(password)) score += 20
  if (/[^a-zA-Z0-9]/.test(password)) score += 10

  return Math.min(score, 100)
})

const passwordStrengthClass = computed(() => {
  const strength = passwordStrength.value
  if (strength < 40) return 'weak'
  if (strength < 70) return 'medium'
  return 'strong'
})

const passwordStrengthWidth = computed(() => {
  return `${passwordStrength.value}%`
})

const passwordStrengthText = computed(() => {
  const strength = passwordStrength.value
  if (strength < 40) return '弱'
  if (strength < 70) return '中等'
  return '强'
})

// 表单有效性检查
const isFormValid = computed(() => {
  return registerForm.username &&
         registerForm.email &&
         registerForm.password &&
         registerForm.confirmPassword &&
         usernameCheckResult.value === 'available' &&
         emailCheckResult.value === 'available' &&
         registerForm.password === registerForm.confirmPassword &&
         /^(?=.*[a-zA-Z])(?=.*\d).{8,}$/.test(registerForm.password)
})

// 防抖函数
const debounce = (func, delay) => {
  let timeoutId
  return (...args) => {
    clearTimeout(timeoutId)
    timeoutId = setTimeout(() => func.apply(null, args), delay)
  }
}

// 检查用户名可用性
const checkUsernameAvailability = debounce(async () => {
  if (!registerForm.username || registerForm.username.length < 3) {
    usernameCheckResult.value = ''
    return
  }

  usernameCheckLoading.value = true
  try {
    const response = await checkAvailability({ username: registerForm.username })
    if (response.data.available) {
      usernameCheckResult.value = 'available'
    } else {
      usernameCheckResult.value = 'unavailable'
      ElMessage.warning('用户名已被占用')
    }
  } catch (error) {
    console.error('检查用户名可用性失败:', error)
    usernameCheckResult.value = ''
  } finally {
    usernameCheckLoading.value = false
  }
}, 500)

// 检查邮箱可用性
const checkEmailAvailability = debounce(async () => {
  if (!registerForm.email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(registerForm.email)) {
    emailCheckResult.value = ''
    return
  }

  emailCheckLoading.value = true
  try {
    const response = await checkAvailability({ email: registerForm.email })
    if (response.data.available) {
      emailCheckResult.value = 'available'
    } else {
      emailCheckResult.value = 'unavailable'
      ElMessage.warning('邮箱已被注册')
    }
  } catch (error) {
    console.error('检查邮箱可用性失败:', error)
    emailCheckResult.value = ''
  } finally {
    emailCheckLoading.value = false
  }
}, 500)

// 监听表单变化，重置检查结果
watch(() => registerForm.username, () => {
  usernameCheckResult.value = ''
})

watch(() => registerForm.email, () => {
  emailCheckResult.value = ''
})

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) return

  try {
    await registerFormRef.value.validate()
  } catch (error) {
    console.log('表单验证失败:', error)
    return
  }

  registerLoading.value = true
  try {
    const response = await register({
      username: registerForm.username,
      email: registerForm.email,
      password: registerForm.password,
      confirmPassword: registerForm.confirmPassword
    })

    ElMessage.success('注册成功！请登录您的账户')

    // 跳转到登录页面
    router.push({
      path: '/login',
      query: { username: registerForm.username }
    })
  } catch (error) {
    console.error('注册失败:', error)

    if (error.response && error.response.data) {
      const { message, data } = error.response.data

      if (data && data.field) {
        if (data.field === 'username') {
          usernameCheckResult.value = 'unavailable'
        } else if (data.field === 'email') {
          emailCheckResult.value = 'unavailable'
        }
      }

      ElMessage.error(message || '注册失败，请重试')
    } else {
      ElMessage.error('网络错误，请重试')
    }
  } finally {
    registerLoading.value = false
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-card {
  width: 100%;
  max-width: 440px;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.card-header {
  text-align: center;
  margin-bottom: 20px;
}

.card-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.subtitle {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.password-strength {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.strength-label {
  color: #606266;
  min-width: 60px;
}

.strength-bar {
  flex: 1;
  height: 4px;
  background-color: #f0f0f0;
  border-radius: 2px;
  overflow: hidden;
}

.strength-fill {
  height: 100%;
  border-radius: 2px;
  transition: all 0.3s ease;
}

.strength-fill.weak {
  background-color: #f56c6c;
}

.strength-fill.medium {
  background-color: #e6a23c;
}

.strength-fill.strong {
  background-color: #67c23a;
}

.strength-text {
  min-width: 20px;
  font-weight: 500;
}

.strength-text.weak {
  color: #f56c6c;
}

.strength-text.medium {
  color: #e6a23c;
}

.strength-text.strong {
  color: #67c23a;
}

.login-link {
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

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

:deep(.el-input__inner) {
  border-radius: 6px;
}

:deep(.el-button) {
  border-radius: 6px;
  font-weight: 500;
}

.is-loading {
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style>