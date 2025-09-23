<template>
  <div class="project-proxy-settings">
    <div class="toolbar">
      <el-button
        v-permission="['project:write']"
        type="primary"
        @click="openCreateDialog"
      >
        <el-icon><Plus /></el-icon>
        新建代理规则
      </el-button>
    </div>

    <el-table
      :data="proxyConfigs"
      :loading="loading"
      class="proxy-table"
    >
      <el-table-column
        prop="name"
        label="规则名称"
        min-width="140"
      />
      <el-table-column
        prop="pathPattern"
        label="路径匹配"
        min-width="160"
      />
      <el-table-column
        prop="mode"
        label="模式"
        width="120"
      >
        <template #default="scope">
          <el-tag :type="getModeType(scope.row.mode)">
            {{ modeText(scope.row.mode) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        prop="targetUrl"
        label="目标地址"
        min-width="200"
      >
        <template #default="scope">
          {{ scope.row.targetUrl || '-' }}
        </template>
      </el-table-column>
      <el-table-column
        prop="forwardHeaders"
        label="转发请求头"
        min-width="200"
      >
        <template #default="scope">
          <el-tag
            v-for="header in scope.row.forwardHeaders"
            :key="header"
            type="info"
            size="small"
            class="header-tag"
          >
            {{ header }}
          </el-tag>
          <span v-if="scope.row.forwardHeaders.length === 0">-</span>
        </template>
      </el-table-column>
      <el-table-column
        label="保留Host"
        width="120"
      >
        <template #default="scope">
          <el-tag :type="scope.row.preserveHost ? 'success' : 'info'">
            {{ scope.row.preserveHost ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        prop="enabled"
        label="状态"
        width="120"
      >
        <template #default="scope">
          <template v-if="canEdit">
            <el-switch
              v-model="scope.row.enabled"
              size="small"
              @change="(value) => handleToggle(scope.row, value)"
            />
          </template>
          <template v-else>
            <el-tag
              :type="scope.row.enabled ? 'success' : 'danger'"
              size="small"
            >
              {{ scope.row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        width="180"
      >
        <template #default="scope">
          <el-button
            v-permission="['project:write']"
            type="text"
            @click="openEditDialog(scope.row)"
          >
            编辑
          </el-button>
          <el-button
            v-permission="['project:write']"
            type="text"
            style="color: #f56c6c;"
            @click="handleDelete(scope.row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialog.visible"
      :title="dialog.isEdit ? '编辑代理规则' : '新建代理规则'"
      width="640px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-form-item
          label="规则名称"
          prop="name"
        >
          <el-input
            v-model="form.name"
            placeholder="例如：用户服务代理"
            maxlength="100"
          />
        </el-form-item>

        <el-form-item
          label="路径匹配"
          prop="pathPattern"
        >
          <el-input
            v-model="form.pathPattern"
            placeholder="例如：/api/user/**"
            maxlength="255"
          />
          <div class="form-tip">
            支持Ant风格通配，例如：/api/**，/auth/*
          </div>
        </el-form-item>

        <el-form-item
          label="代理模式"
          prop="mode"
        >
          <el-radio-group v-model="form.mode">
            <el-radio-button label="MOCK">
              仅Mock
            </el-radio-button>
            <el-radio-button label="PROXY">
              仅代理
            </el-radio-button>
            <el-radio-button label="AUTO">
              自动（优先Mock，失败时代理）
            </el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item
          v-if="showTargetUrl"
          label="目标地址"
          prop="targetUrl"
        >
          <el-input
            v-model="form.targetUrl"
            placeholder="https://api.example.com"
            maxlength="255"
          />
        </el-form-item>

        <el-form-item label="转发请求头">
          <el-select
            v-model="form.forwardHeaders"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="输入并回车添加请求头"
          >
            <el-option
              v-for="header in commonHeaders"
              :key="header"
              :label="header"
              :value="header"
            />
          </el-select>
          <div class="form-tip">
            常用示例：Authorization、X-Real-IP、X-Request-ID
          </div>
        </el-form-item>

        <el-form-item label="保留Host">
          <el-switch v-model="form.preserveHost" />
          <div class="form-tip">
            开启后将原请求的Host头传递给目标服务
          </div>
        </el-form-item>

        <el-form-item label="启用状态">
          <el-switch v-model="form.enabled" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialog.visible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="dialog.loading"
          @click="handleSubmit"
        >
          确认
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getProxyConfigs,
  createProxyConfig,
  updateProxyConfig,
  toggleProxyConfig,
  deleteProxyConfig
} from '@/api/proxyConfig'
import { usePermission } from '@/composables/usePermission'

const props = defineProps({
  projectId: {
    type: Number,
    required: true
  }
})

const proxyConfigs = ref([])
const loading = ref(false)
const { hasPermission } = usePermission()
const canEdit = computed(() => hasPermission('project:write', props.projectId))

const dialog = reactive({
  visible: false,
  isEdit: false,
  loading: false,
  editingId: null
})

const formRef = ref()
const form = reactive({
  name: '',
  pathPattern: '/**',
  mode: 'MOCK',
  targetUrl: '',
  forwardHeaders: [],
  preserveHost: false,
  enabled: true
})

const rules = {
  name: [
    { required: true, message: '请输入规则名称', trigger: 'blur' },
    { min: 2, max: 100, message: '名称长度应在2-100个字符之间', trigger: 'blur' }
  ],
  pathPattern: [
    { required: true, message: '请输入路径匹配规则', trigger: 'blur' },
    { pattern: /^\/.*/, message: '路径必须以/开头', trigger: 'blur' }
  ],
  mode: [
    { required: true, message: '请选择代理模式', trigger: 'change' }
  ],
  targetUrl: [
    {
      validator: (rule, value, callback) => {
        if (showTargetUrl.value) {
          if (!value) {
            callback(new Error('请填写目标地址'))
            return
          }
          if (!/^https?:\/\//.test(value)) {
            callback(new Error('目标地址必须以http://或https://开头'))
            return
          }
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

const commonHeaders = [
  'Authorization',
  'X-Request-ID',
  'X-Real-IP',
  'X-Forwarded-For',
  'User-Agent'
]

const showTargetUrl = computed(() => ['PROXY', 'AUTO'].includes(form.mode))

const modeText = (mode) => {
  switch (mode) {
    case 'MOCK':
      return '仅Mock'
    case 'PROXY':
      return '仅代理'
    case 'AUTO':
      return '自动'
    default:
      return mode
  }
}

const getModeType = (mode) => {
  switch (mode) {
    case 'MOCK':
      return 'success'
    case 'PROXY':
      return 'warning'
    case 'AUTO':
      return 'info'
    default:
      return 'info'
  }
}

const loadProxyConfigs = async () => {
  loading.value = true
  try {
    const response = await getProxyConfigs(props.projectId)
    proxyConfigs.value = (response.data || []).map(item => ({
      ...item,
      forwardHeaders: item.forwardHeaders || []
    }))
  } catch (error) {
    ElMessage.error('加载代理配置失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  form.name = ''
  form.pathPattern = '/**'
  form.mode = 'MOCK'
  form.targetUrl = ''
  form.forwardHeaders = []
  form.preserveHost = false
  form.enabled = true
  dialog.editingId = null
  dialog.isEdit = false
  formRef.value?.clearValidate()
}

const openCreateDialog = () => {
  resetForm()
  dialog.visible = true
}

const openEditDialog = (config) => {
  resetForm()
  dialog.visible = true
  dialog.isEdit = true
  dialog.editingId = config.id
  form.name = config.name
  form.pathPattern = config.pathPattern
  form.mode = config.mode
  form.targetUrl = config.targetUrl || ''
  form.forwardHeaders = [...(config.forwardHeaders || [])]
  form.preserveHost = !!config.preserveHost
  form.enabled = !!config.enabled
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    dialog.loading = true

    const payload = {
      name: form.name,
      pathPattern: form.pathPattern,
      mode: form.mode,
      targetUrl: form.targetUrl,
      forwardHeaders: form.forwardHeaders,
      preserveHost: form.preserveHost,
      enabled: form.enabled
    }

    if (dialog.isEdit && dialog.editingId) {
      await updateProxyConfig(props.projectId, dialog.editingId, payload)
      ElMessage.success('代理配置更新成功')
    } else {
      await createProxyConfig(props.projectId, payload)
      ElMessage.success('代理配置创建成功')
    }

    dialog.visible = false
    await loadProxyConfigs()
  } catch (error) {
    if (error?.message) {
      ElMessage.error(error.message)
    }
  } finally {
    dialog.loading = false
  }
}

const handleToggle = async (config, enabled) => {
  if (!canEdit.value) {
    return
  }
  try {
    await toggleProxyConfig(props.projectId, config.id, enabled)
    ElMessage.success(enabled ? '已启用' : '已禁用')
  } catch (error) {
    config.enabled = !enabled
    ElMessage.error('操作失败：' + (error.message || '未知错误'))
  }
}

const handleDelete = async (config) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除代理规则 "${config.name}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteProxyConfig(props.projectId, config.id)
    ElMessage.success('代理规则已删除')
    await loadProxyConfigs()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error('删除失败：' + (error.message || '未知错误'))
    }
  }
}

onMounted(() => {
  loadProxyConfigs()
})
</script>

<style scoped>
.project-proxy-settings {
  max-width: 960px;
}

.toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.proxy-table {
  width: 100%;
}

.header-tag {
  margin-right: 6px;
  margin-bottom: 4px;
}

.form-tip {
  font-size: 13px;
  color: #909399;
  margin-top: 6px;
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-radio-button) {
  margin-right: 8px;
}

.proxy-table :deep(.el-switch.is-disabled .el-switch__core) {
  cursor: not-allowed;
}
</style>
