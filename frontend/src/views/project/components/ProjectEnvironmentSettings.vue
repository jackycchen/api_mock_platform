<template>
  <div class="project-environment-settings">
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="140px"
      size="large"
    >
      <!-- 环境配置 -->
      <div class="settings-section">
        <h3 class="section-title">
          环境配置
        </h3>

        <el-form-item
          label="环境类型"
          prop="environment"
        >
          <el-select
            v-model="form.environment"
            placeholder="选择环境类型"
          >
            <el-option
              label="开发环境"
              value="development"
            />
            <el-option
              label="测试环境"
              value="testing"
            />
            <el-option
              label="预发布环境"
              value="staging"
            />
            <el-option
              label="生产环境"
              value="production"
            />
          </el-select>
          <div class="form-tip">
            不同环境可以有不同的配置策略和数据处理方式
          </div>
        </el-form-item>

        <el-form-item
          label="基础URL"
          prop="baseUrl"
        >
          <el-input
            v-model="form.baseUrl"
            placeholder="https://api.example.com"
            maxlength="255"
          />
          <div class="form-tip">
            用于API文档生成和代理转发的基础地址
          </div>
        </el-form-item>

        <el-form-item
          label="Mock域名"
          prop="mockDomain"
        >
          <el-input
            v-model="form.mockDomain"
            maxlength="255"
          >
            <template #append>
              <el-button
                :loading="generatingDomain"
                @click="generateMockDomain"
              >
                生成
              </el-button>
            </template>
          </el-input>
          <div class="form-tip">
            Mock服务的访问域名，可以自动生成或手动设置
          </div>
        </el-form-item>
      </div>

      <!-- 代理设置 -->
      <div class="settings-section">
        <h3 class="section-title">
          代理设置
        </h3>

        <el-form-item label="启用代理">
          <el-switch
            v-model="form.proxyEnabled"
            active-text="启用"
            inactive-text="禁用"
          />
          <div class="form-tip">
            启用后可以将请求转发到真实的后端服务
          </div>
        </el-form-item>

        <el-form-item
          v-show="form.proxyEnabled"
          label="代理目标"
          prop="proxyTarget"
        >
          <el-input
            v-model="form.proxyTarget"
            placeholder="https://api.backend.com"
            maxlength="255"
          />
          <div class="form-tip">
            代理转发的目标服务器地址
          </div>
        </el-form-item>
      </div>

      <!-- CORS设置 -->
      <div class="settings-section">
        <h3 class="section-title">
          跨域设置
        </h3>

        <el-form-item label="允许跨域">
          <el-switch
            v-model="form.allowCors"
            active-text="允许"
            inactive-text="禁止"
          />
        </el-form-item>

        <el-form-item
          v-show="form.allowCors"
          label="允许的域名"
          prop="corsOrigins"
        >
          <el-input
            v-model="form.corsOrigins"
            placeholder="*（允许所有域名）或 https://example.com,https://app.com"
            maxlength="500"
          />
          <div class="form-tip">
            多个域名用英文逗号分隔，使用 * 允许所有域名
          </div>
        </el-form-item>
      </div>

      <!-- 性能设置 -->
      <div class="settings-section">
        <h3 class="section-title">
          性能设置
        </h3>

        <el-form-item
          label="请求超时"
          prop="requestTimeout"
        >
          <el-input-number
            v-model="form.requestTimeout"
            :min="1000"
            :max="300000"
            :step="1000"
            controls-position="right"
          />
          <span class="unit">毫秒</span>
          <div class="form-tip">
            API请求的超时时间，范围：1-300秒
          </div>
        </el-form-item>

        <el-form-item
          label="速率限制"
          prop="rateLimit"
        >
          <el-input-number
            v-model="form.rateLimit"
            :min="1"
            :max="100000"
            :step="100"
            controls-position="right"
          />
          <span class="unit">次/分钟</span>
          <div class="form-tip">
            每分钟允许的最大请求次数
          </div>
        </el-form-item>
      </div>

      <!-- 功能开关 -->
      <div class="settings-section">
        <h3 class="section-title">
          功能开关
        </h3>

        <el-form-item label="请求日志">
          <el-switch
            v-model="form.enableLogging"
            active-text="启用"
            inactive-text="禁用"
          />
          <div class="form-tip">
            记录所有API请求的详细日志
          </div>
        </el-form-item>

        <el-form-item label="数据缓存">
          <el-switch
            v-model="form.enableCache"
            active-text="启用"
            inactive-text="禁用"
          />
          <div class="form-tip">
            缓存Mock数据以提高响应速度
          </div>
        </el-form-item>

        <el-form-item
          v-show="form.enableCache"
          label="缓存时间"
          prop="cacheTtl"
        >
          <el-input-number
            v-model="form.cacheTtl"
            :min="60"
            :max="86400"
            :step="60"
            controls-position="right"
          />
          <span class="unit">秒</span>
          <div class="form-tip">
            缓存数据的有效时间，范围：1分钟-24小时
          </div>
        </el-form-item>
      </div>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <el-button @click="resetForm">
          重置
        </el-button>
        <el-button
          v-permission="['project:write']"
          type="primary"
          :loading="saving"
          @click="handleSubmit"
        >
          保存设置
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { updateProjectEnvironmentSettings, generateMockDomain as apiGenerateMockDomain } from '@/api/projectSettings'

const props = defineProps({
  projectId: {
    type: Number,
    required: true
  },
  settings: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['updated'])

const formRef = ref()
const saving = ref(false)
const generatingDomain = ref(false)

// 表单数据
const form = reactive({
  environment: 'development',
  baseUrl: '',
  mockDomain: '',
  proxyEnabled: false,
  proxyTarget: '',
  allowCors: true,
  corsOrigins: '*',
  requestTimeout: 30000,
  rateLimit: 1000,
  enableLogging: true,
  enableCache: true,
  cacheTtl: 3600
})

// 表单验证规则
const rules = {
  environment: [
    { required: true, message: '请选择环境类型', trigger: 'change' }
  ],
  baseUrl: [
    { pattern: /^$|^https?:\/\/.*/, message: '请输入有效的URL地址', trigger: 'blur' }
  ],
  proxyTarget: [
    {
      validator: (rule, value, callback) => {
        if (form.proxyEnabled && (!value || !value.trim())) {
          callback(new Error('启用代理时必须设置代理目标'))
        } else if (value && !/^https?:\/\/.*/.test(value)) {
          callback(new Error('请输入有效的URL地址'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  corsOrigins: [
    { max: 500, message: '域名列表不能超过500个字符', trigger: 'blur' }
  ],
  requestTimeout: [
    { required: true, message: '请设置请求超时时间', trigger: 'blur' },
    { type: 'number', min: 1000, max: 300000, message: '超时时间范围：1-300秒', trigger: 'blur' }
  ],
  rateLimit: [
    { required: true, message: '请设置速率限制', trigger: 'blur' },
    { type: 'number', min: 1, max: 100000, message: '速率限制范围：1-100000次/分钟', trigger: 'blur' }
  ],
  cacheTtl: [
    {
      validator: (rule, value, callback) => {
        if (form.enableCache && (!value || value < 60 || value > 86400)) {
          callback(new Error('缓存时间范围：60-86400秒'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 生成Mock域名
const generateMockDomain = async () => {
  generatingDomain.value = true
  try {
    const response = await apiGenerateMockDomain(props.projectId)
    form.mockDomain = response.data
    ElMessage.success('Mock域名生成成功')
  } catch (error) {
    ElMessage.error('生成Mock域名失败: ' + error.message)
  } finally {
    generatingDomain.value = false
  }
}

// 初始化表单
const initForm = () => {
  if (props.settings) {
    form.environment = props.settings.environment || 'development'
    form.baseUrl = props.settings.baseUrl || ''
    form.mockDomain = props.settings.mockDomain || ''
    form.proxyEnabled = props.settings.proxyEnabled || false
    form.proxyTarget = props.settings.proxyTarget || ''
    form.allowCors = props.settings.allowCors !== false
    form.corsOrigins = props.settings.corsOrigins || '*'
    form.requestTimeout = props.settings.requestTimeout || 30000
    form.rateLimit = props.settings.rateLimit || 1000
    form.enableLogging = props.settings.enableLogging !== false
    form.enableCache = props.settings.enableCache !== false
    form.cacheTtl = props.settings.cacheTtl || 3600
  }
}

// 重置表单
const resetForm = () => {
  initForm()
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()

    saving.value = true
    const response = await updateProjectEnvironmentSettings(props.projectId, form)
    emit('updated', response.data)
    ElMessage.success('环境设置保存成功')
  } catch (error) {
    if (error.message) {
      ElMessage.error('保存失败: ' + error.message)
    }
  } finally {
    saving.value = false
  }
}

// 监听设置变化
watch(() => props.settings, initForm, { immediate: true, deep: true })
</script>

<style scoped>
.project-environment-settings {
  max-width: 600px;
}

.settings-section {
  margin-bottom: 40px;
}

.section-title {
  margin: 0 0 24px 0;
  padding-bottom: 12px;
  font-size: 18px;
  font-weight: 500;
  color: #303133;
  border-bottom: 1px solid #e4e7ed;
}

.form-tip {
  font-size: 13px;
  color: #909399;
  margin-top: 6px;
  line-height: 1.4;
}

.unit {
  margin-left: 12px;
  color: #909399;
  font-size: 14px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 24px;
  border-top: 1px solid #e4e7ed;
}

/* 开关组件样式调整 */
:deep(.el-switch) {
  margin-right: 12px;
}

/* 数字输入框样式 */
:deep(.el-input-number) {
  width: 180px;
}

/* 选择器样式 */
:deep(.el-select) {
  width: 200px;
}
</style>