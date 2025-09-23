<template>
  <div class="project-basic-settings">
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      size="large"
    >
      <!-- 项目信息 -->
      <div class="settings-section">
        <h3 class="section-title">
          项目信息
        </h3>

        <el-form-item
          label="项目名称"
          prop="name"
        >
          <el-input
            v-model="form.name"
            placeholder="请输入项目名称"
            maxlength="100"
            show-word-limit
            @blur="checkNameAvailability"
          />
          <div
            v-if="nameCheckStatus"
            class="name-check-status"
          >
            <el-icon
              v-if="nameCheckStatus === 'available'"
              color="#67c23a"
            >
              <CircleCheck />
            </el-icon>
            <el-icon
              v-else-if="nameCheckStatus === 'unavailable'"
              color="#f56c6c"
            >
              <CircleClose />
            </el-icon>
            <el-icon
              v-else
              color="#409eff"
            >
              <Loading />
            </el-icon>
            <span :class="`status-${nameCheckStatus}`">
              {{ getNameCheckMessage() }}
            </span>
          </div>
        </el-form-item>

        <el-form-item
          label="项目描述"
          prop="description"
        >
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入项目描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item
          label="项目类型"
          prop="type"
        >
          <el-select
            v-model="form.type"
            placeholder="选择项目类型"
          >
            <el-option
              label="Web API"
              value="WEB_API"
            />
            <el-option
              label="移动应用"
              value="MOBILE_APP"
            />
            <el-option
              label="微服务"
              value="MICROSERVICE"
            />
            <el-option
              label="其他"
              value="OTHER"
            />
          </el-select>
        </el-form-item>

        <el-form-item
          label="项目可见性"
          prop="visibility"
        >
          <el-radio-group v-model="form.visibility">
            <el-radio label="PRIVATE">
              <div class="radio-option">
                <div class="radio-title">
                  <el-icon><Lock /></el-icon>
                  私有项目
                </div>
                <div class="radio-description">
                  只有项目成员可以访问
                </div>
              </div>
            </el-radio>
            <el-radio label="PUBLIC">
              <div class="radio-option">
                <div class="radio-title">
                  <el-icon><Unlock /></el-icon>
                  公开项目
                </div>
                <div class="radio-description">
                  所有用户都可以查看
                </div>
              </div>
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item
          label="项目标签"
          prop="projectTags"
        >
          <el-input
            v-model="form.projectTags"
            placeholder="用逗号分隔多个标签，如：API,Mock,测试"
            maxlength="500"
            show-word-limit
          />
          <div class="form-tip">
            标签有助于项目分类和搜索，多个标签请用英文逗号分隔
          </div>
        </el-form-item>
      </div>

      <!-- 统计信息 -->
      <div class="settings-section">
        <h3 class="section-title">
          项目统计
        </h3>

        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-label">
              成员数量
            </div>
            <div class="stat-value">
              {{ settings?.memberCount || 0 }}
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-label">
              接口数量
            </div>
            <div class="stat-value">
              {{ settings?.apiCount || 0 }}
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-label">
              Mock规则
            </div>
            <div class="stat-value">
              {{ settings?.mockCount || 0 }}
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-label">
              创建时间
            </div>
            <div class="stat-value">
              {{ formatTime(settings?.createdAt) }}
            </div>
          </div>
        </div>
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
import {
  CircleCheck,
  CircleClose,
  Loading,
  Lock,
  Unlock
} from '@element-plus/icons-vue'
import { updateProjectBasicSettings, checkProjectNameAvailability } from '@/api/projectSettings'
import { formatTime } from '@/utils/date'

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
const nameCheckStatus = ref(null) // null, 'checking', 'available', 'unavailable'
const originalName = ref('')

// 表单数据
const form = reactive({
  name: '',
  description: '',
  type: 'WEB_API',
  visibility: 'PRIVATE',
  projectTags: ''
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { min: 2, max: 100, message: '项目名称长度应在2-100个字符之间', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '项目描述不能超过500个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择项目类型', trigger: 'change' }
  ],
  visibility: [
    { required: true, message: '请选择项目可见性', trigger: 'change' }
  ],
  projectTags: [
    { max: 500, message: '项目标签不能超过500个字符', trigger: 'blur' }
  ]
}

// 获取名称检查状态信息
const getNameCheckMessage = () => {
  switch (nameCheckStatus.value) {
    case 'checking':
      return '正在检查名称可用性...'
    case 'available':
      return '名称可用'
    case 'unavailable':
      return '名称已被使用'
    default:
      return ''
  }
}

// 检查名称可用性
const checkNameAvailability = async () => {
  if (!form.name || form.name === originalName.value) {
    nameCheckStatus.value = null
    return
  }

  nameCheckStatus.value = 'checking'
  try {
    const response = await checkProjectNameAvailability(props.projectId, form.name)
    nameCheckStatus.value = response.data ? 'available' : 'unavailable'
  } catch (error) {
    nameCheckStatus.value = null
    console.error('检查名称可用性失败:', error)
  }
}

// 初始化表单
const initForm = () => {
  if (props.settings) {
    form.name = props.settings.name || ''
    form.description = props.settings.description || ''
    form.type = props.settings.type || 'WEB_API'
    form.visibility = props.settings.visibility || 'PRIVATE'
    form.projectTags = props.settings.projectTags || ''

    originalName.value = form.name
    nameCheckStatus.value = null
  }
}

// 重置表单
const resetForm = () => {
  initForm()
  nameCheckStatus.value = null
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()

    // 检查名称可用性
    if (nameCheckStatus.value === 'unavailable') {
      ElMessage.error('项目名称已被使用，请更换其他名称')
      return
    }

    saving.value = true
    const response = await updateProjectBasicSettings(props.projectId, form)
    emit('updated', response.data)
    originalName.value = form.name
    nameCheckStatus.value = null
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
.project-basic-settings {
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

.name-check-status {
  display: flex;
  align-items: center;
  margin-top: 8px;
  font-size: 14px;
}

.name-check-status .el-icon {
  margin-right: 6px;
}

.status-available {
  color: #67c23a;
}

.status-unavailable {
  color: #f56c6c;
}

.status-checking {
  color: #409eff;
}

.radio-option {
  margin-left: 8px;
}

.radio-title {
  display: flex;
  align-items: center;
  font-weight: 500;
  color: #303133;
}

.radio-title .el-icon {
  margin-right: 6px;
}

.radio-description {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
}

.form-tip {
  font-size: 13px;
  color: #909399;
  margin-top: 6px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 20px;
  padding: 20px;
  background-color: #f9fafc;
  border-radius: 6px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.stat-value {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 24px;
  border-top: 1px solid #e4e7ed;
}

/* 自定义单选按钮样式 */
:deep(.el-radio) {
  display: block;
  margin-bottom: 16px;
  height: auto;
  line-height: 1.5;
}

:deep(.el-radio__label) {
  padding-left: 8px;
}
</style>