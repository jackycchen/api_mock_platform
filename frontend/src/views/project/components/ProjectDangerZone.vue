<template>
  <div class="project-danger-zone">
    <!-- 项目转让 -->
    <div class="danger-section">
      <div class="section-header">
        <div class="header-info">
          <h3 class="section-title">
            <el-icon><Switch /></el-icon>
            转让项目所有权
          </h3>
          <p class="section-description">
            将项目的所有权转让给其他成员。转让后您将失去项目的管理权限。
          </p>
        </div>
        <el-button
          v-permission="['project:admin']"
          type="warning"
          :disabled="!canTransferProject"
          @click="showTransferDialog"
        >
          转让项目
        </el-button>
      </div>
      <div class="section-warning">
        <el-icon><Warning /></el-icon>
        <span>此操作不可逆，请谨慎操作。转让后新所有者将拥有项目的完全控制权。</span>
      </div>
    </div>

    <!-- 项目删除 -->
    <div class="danger-section">
      <div class="section-header">
        <div class="header-info">
          <h3 class="section-title">
            <el-icon><Delete /></el-icon>
            删除项目
          </h3>
          <p class="section-description">
            永久删除此项目及其所有相关数据，包括API接口、Mock规则、成员信息等。
          </p>
        </div>
        <el-button
          v-permission="['project:admin']"
          type="danger"
          :disabled="!canDeleteProject"
          @click="showDeleteDialog"
        >
          删除项目
        </el-button>
      </div>
      <div class="section-warning">
        <el-icon><Warning /></el-icon>
        <span>此操作不可逆！删除后所有数据将永久丢失，无法恢复。</span>
      </div>
    </div>

    <!-- 项目归档 -->
    <div class="danger-section">
      <div class="section-header">
        <div class="header-info">
          <h3 class="section-title">
            <el-icon><Box /></el-icon>
            归档项目
          </h3>
          <p class="section-description">
            将项目标记为归档状态。归档后项目将变为只读模式，可以随时恢复。
          </p>
        </div>
        <el-button
          v-permission="['project:admin']"
          type="info"
          :disabled="project?.status === 'ARCHIVED'"
          :loading="archiving"
          @click="handleArchiveProject"
        >
          {{ project?.status === 'ARCHIVED' ? '已归档' : '归档项目' }}
        </el-button>
      </div>
      <div
        v-if="project?.status !== 'ARCHIVED'"
        class="section-info"
      >
        <el-icon><InfoFilled /></el-icon>
        <span>归档后可以随时恢复，不会丢失任何数据。</span>
      </div>
    </div>

    <!-- 转让对话框 -->
    <el-dialog
      v-model="transferDialog.visible"
      title="转让项目所有权"
      width="500px"
      @close="resetTransferDialog"
    >
      <div class="dialog-warning">
        <el-icon><Warning /></el-icon>
        <span>此操作将永久转让项目所有权，转让后您将失去管理权限！</span>
      </div>

      <el-form
        ref="transferFormRef"
        :model="transferForm"
        :rules="transferRules"
        label-width="120px"
      >
        <el-form-item label="当前所有者">
          <div class="current-owner">
            <el-avatar
              :src="currentOwner?.avatar"
              :size="40"
            >
              {{ currentOwner?.nickname?.charAt(0) }}
            </el-avatar>
            <div class="owner-info">
              <div class="owner-name">
                {{ currentOwner?.nickname }}
              </div>
              <div class="owner-email">
                {{ currentOwner?.email }}
              </div>
            </div>
          </div>
        </el-form-item>

        <el-form-item
          label="新所有者"
          prop="newOwnerId"
        >
          <el-select
            v-model="transferForm.newOwnerId"
            placeholder="选择新的项目所有者"
            filterable
            @change="handleOwnerChange"
          >
            <el-option
              v-for="member in transferableMembers"
              :key="member.id"
              :label="member.nickname || member.username"
              :value="member.userId"
            >
              <div class="member-option">
                <el-avatar
                  :src="member.avatar"
                  :size="30"
                >
                  {{ member.nickname?.charAt(0) }}
                </el-avatar>
                <div class="member-info">
                  <span class="member-name">{{ member.nickname || member.username }}</span>
                  <span class="member-email">{{ member.email }}</span>
                </div>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item
          label="转让原因"
          prop="reason"
        >
          <el-input
            v-model="transferForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入转让原因"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item
          label="确认密码"
          prop="password"
        >
          <el-input
            v-model="transferForm.password"
            type="password"
            placeholder="请输入您的登录密码以确认操作"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认转让">
          <el-checkbox v-model="transferForm.confirmed">
            我确认要将项目所有权转让给所选用户，并理解此操作不可逆
          </el-checkbox>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="transferDialog.visible = false">
          取消
        </el-button>
        <el-button
          type="danger"
          :loading="transferDialog.loading"
          :disabled="!transferForm.confirmed"
          @click="handleTransferProject"
        >
          确认转让
        </el-button>
      </template>
    </el-dialog>

    <!-- 删除对话框 -->
    <el-dialog
      v-model="deleteDialog.visible"
      title="永久删除项目"
      width="500px"
      @close="resetDeleteDialog"
    >
      <div class="dialog-danger">
        <el-icon><Warning /></el-icon>
        <span>此操作将永久删除项目及所有相关数据，且无法恢复！</span>
      </div>

      <div class="project-info">
        <h4>即将删除的项目信息：</h4>
        <ul class="project-details">
          <li><strong>项目名称：</strong>{{ project?.name }}</li>
          <li><strong>项目类型：</strong>{{ project?.type }}</li>
          <li><strong>创建时间：</strong>{{ formatTime(project?.createdAt) }}</li>
          <li><strong>成员数量：</strong>{{ project?.memberCount || 0 }} 人</li>
          <li><strong>接口数量：</strong>{{ project?.apiCount || 0 }} 个</li>
          <li><strong>Mock规则：</strong>{{ project?.mockCount || 0 }} 条</li>
        </ul>
      </div>

      <el-form
        ref="deleteFormRef"
        :model="deleteForm"
        :rules="deleteRules"
        label-width="120px"
      >
        <el-form-item
          label="项目名称"
          prop="projectName"
        >
          <el-input
            v-model="deleteForm.projectName"
            placeholder="请输入项目名称以确认删除"
          />
          <div class="form-tip">
            请输入项目名称 "<strong>{{ project?.name }}</strong>" 以确认删除
          </div>
        </el-form-item>

        <el-form-item
          label="删除原因"
          prop="reason"
        >
          <el-input
            v-model="deleteForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请说明删除原因"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item
          label="确认密码"
          prop="password"
        >
          <el-input
            v-model="deleteForm.password"
            type="password"
            placeholder="请输入您的登录密码以确认操作"
            show-password
          />
        </el-form-item>

        <el-form-item label="最终确认">
          <el-checkbox v-model="deleteForm.confirmed">
            我已阅读并理解上述内容，确认要永久删除此项目
          </el-checkbox>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="deleteDialog.visible = false">
          取消
        </el-button>
        <el-button
          type="danger"
          :loading="deleteDialog.loading"
          :disabled="!deleteForm.confirmed"
          @click="handleDeleteProject"
        >
          永久删除
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Switch,
  Delete,
  Box,
  Warning,
  InfoFilled
} from '@element-plus/icons-vue'
import {
  transferProjectOwnership,
  permanentDeleteProject,
  verifyPassword
} from '@/api/projectSettings'
import { getProjectMembers } from '@/api/projectMembers'
import { formatTime } from '@/utils/date'
import { useUserStore } from '@/store/user'

const props = defineProps({
  projectId: {
    type: Number,
    required: true
  },
  project: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['project-deleted', 'ownership-transferred'])

const userStore = useUserStore()

const members = ref([])
const archiving = ref(false)

// 转让对话框
const transferDialog = reactive({
  visible: false,
  loading: false
})

const transferFormRef = ref()
const transferForm = reactive({
  newOwnerId: '',
  newOwnerUsername: '',
  reason: '',
  password: '',
  confirmed: false
})

const transferRules = {
  newOwnerId: [
    { required: true, message: '请选择新的所有者', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请输入转让原因', trigger: 'blur' },
    { min: 10, max: 200, message: '转让原因长度应在10-200个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入登录密码', trigger: 'blur' }
  ]
}

// 删除对话框
const deleteDialog = reactive({
  visible: false,
  loading: false
})

const deleteFormRef = ref()
const deleteForm = reactive({
  projectName: '',
  reason: '',
  password: '',
  confirmed: false
})

const deleteRules = {
  projectName: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== props.project?.name) {
          callback(new Error('项目名称不匹配'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  reason: [
    { required: true, message: '请说明删除原因', trigger: 'blur' },
    { min: 10, max: 200, message: '删除原因长度应在10-200个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入登录密码', trigger: 'blur' }
  ]
}

// 计算属性
const currentOwner = computed(() => {
  return members.value.find(member => member.role === 'OWNER')
})

const transferableMembers = computed(() => {
  return members.value.filter(member =>
    member.role !== 'OWNER' &&
    member.status === 'ACTIVE' &&
    ['ADMIN', 'DEVELOPER'].includes(member.role)
  )
})

const canTransferProject = computed(() => {
  return transferableMembers.value.length > 0 &&
         currentOwner.value?.userId === userStore.user?.id
})

const canDeleteProject = computed(() => {
  return currentOwner.value?.userId === userStore.user?.id
})

// 方法
const loadMembers = async () => {
  try {
    const response = await getProjectMembers(props.projectId, { page: 0, size: 100 })
    members.value = response.data.content
  } catch (error) {
    console.error('加载成员列表失败:', error)
  }
}

const showTransferDialog = () => {
  if (!canTransferProject.value) {
    ElMessage.warning('当前没有可转让的成员，请先邀请管理员或开发者角色的成员')
    return
  }
  transferDialog.visible = true
}

const resetTransferDialog = () => {
  transferForm.newOwnerId = ''
  transferForm.newOwnerUsername = ''
  transferForm.reason = ''
  transferForm.password = ''
  transferForm.confirmed = false
  transferFormRef.value?.clearValidate()
}

const handleOwnerChange = (userId) => {
  const selectedMember = transferableMembers.value.find(member => member.userId === userId)
  transferForm.newOwnerUsername = selectedMember?.username || ''
}

const handleTransferProject = async () => {
  try {
    await transferFormRef.value.validate()

    if (!transferForm.confirmed) {
      ElMessage.warning('请确认转让操作')
      return
    }

    if (!transferForm.newOwnerUsername) {
      ElMessage.error('无法获取新所有者用户名，请重新选择成员')
      return
    }

    // 验证密码
    transferDialog.loading = true
    const verifyResponse = await verifyPassword(props.projectId, transferForm.password)
    if (!verifyResponse.data) {
      throw new Error('密码验证失败')
    }

    // 执行转让
    await transferProjectOwnership(props.projectId, {
      newOwnerId: transferForm.newOwnerId,
      newOwnerUsername: transferForm.newOwnerUsername,
      transferReason: transferForm.reason,
      confirmPassword: transferForm.password
    })

    ElMessage.success('项目所有权转让成功')
    transferDialog.visible = false
    emit('ownership-transferred')
  } catch (error) {
    if (error.message.includes('密码')) {
      ElMessage.error('密码验证失败，请重新输入')
    } else {
      ElMessage.error('转让失败: ' + error.message)
    }
  } finally {
    transferDialog.loading = false
  }
}

const showDeleteDialog = () => {
  if (!canDeleteProject.value) {
    ElMessage.warning('只有项目所有者才能删除项目')
    return
  }
  deleteDialog.visible = true
}

const resetDeleteDialog = () => {
  deleteForm.projectName = ''
  deleteForm.reason = ''
  deleteForm.password = ''
  deleteForm.confirmed = false
  deleteFormRef.value?.clearValidate()
}

const handleDeleteProject = async () => {
  try {
    await deleteFormRef.value.validate()

    if (!deleteForm.confirmed) {
      ElMessage.warning('请确认删除操作')
      return
    }

    // 最终确认
    await ElMessageBox.confirm(
      '这是最后的确认机会！删除后所有数据将永久丢失且无法恢复。确定要继续吗？',
      '最终确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error',
        buttonSize: 'default'
      }
    )

    // 验证密码并执行删除
    deleteDialog.loading = true
    const verifyResponse = await verifyPassword(props.projectId, deleteForm.password)
    if (!verifyResponse.data) {
      throw new Error('密码验证失败')
    }

    await permanentDeleteProject(props.projectId, deleteForm.password)

    ElMessage.success('项目已永久删除')
    deleteDialog.visible = false
    emit('project-deleted')
  } catch (error) {
    if (error === 'cancel') {
      return
    }
    if (error.message.includes('密码')) {
      ElMessage.error('密码验证失败，请重新输入')
    } else {
      ElMessage.error('删除失败: ' + error.message)
    }
  } finally {
    deleteDialog.loading = false
  }
}

const handleArchiveProject = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要归档此项目吗？归档后项目将变为只读模式，但可以随时恢复。',
      '确认归档',
      {
        confirmButtonText: '确定归档',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    archiving.value = true
    // 这里应该调用归档API，暂时模拟
    await new Promise(resolve => setTimeout(resolve, 1000))

    ElMessage.success('项目已归档')
    // 触发项目状态更新
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('归档失败: ' + error.message)
    }
  } finally {
    archiving.value = false
  }
}

onMounted(() => {
  loadMembers()
})
</script>

<style scoped>
.project-danger-zone {
  max-width: 800px;
}

.danger-section {
  padding: 24px;
  margin-bottom: 24px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  background-color: #fff;
}

.danger-section:last-child {
  margin-bottom: 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.header-info {
  flex: 1;
  margin-right: 20px;
}

.section-title {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 500;
  color: #303133;
  display: flex;
  align-items: center;
}

.section-title .el-icon {
  margin-right: 8px;
}

.section-description {
  margin: 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

.section-warning {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background-color: #fdf6ec;
  border: 1px solid #faecd8;
  border-radius: 6px;
  color: #e6a23c;
  font-size: 14px;
}

.section-warning .el-icon {
  margin-right: 8px;
  color: #e6a23c;
}

.section-info {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background-color: #ecf5ff;
  border: 1px solid #d9ecff;
  border-radius: 6px;
  color: #409eff;
  font-size: 14px;
}

.section-info .el-icon {
  margin-right: 8px;
  color: #409eff;
}

.dialog-warning {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  margin-bottom: 20px;
  background-color: #fdf6ec;
  border: 1px solid #faecd8;
  border-radius: 6px;
  color: #e6a23c;
  font-size: 14px;
}

.dialog-warning .el-icon {
  margin-right: 8px;
  color: #e6a23c;
}

.dialog-danger {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  margin-bottom: 20px;
  background-color: #fef0f0;
  border: 1px solid #fde2e2;
  border-radius: 6px;
  color: #f56c6c;
  font-size: 14px;
}

.dialog-danger .el-icon {
  margin-right: 8px;
  color: #f56c6c;
}

.current-owner {
  display: flex;
  align-items: center;
  padding: 12px;
  background-color: #f9fafc;
  border-radius: 6px;
}

.owner-info {
  margin-left: 12px;
}

.owner-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.owner-email {
  font-size: 13px;
  color: #909399;
}

.member-option {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.member-info {
  margin-left: 12px;
  display: flex;
  flex-direction: column;
}

.member-name {
  font-weight: 500;
  color: #303133;
}

.member-email {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.project-info {
  margin: 20px 0;
  padding: 16px;
  background-color: #f9fafc;
  border-radius: 6px;
}

.project-info h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 14px;
}

.project-details {
  margin: 0;
  padding-left: 20px;
  color: #606266;
  font-size: 14px;
}

.project-details li {
  margin-bottom: 8px;
  line-height: 1.5;
}

.form-tip {
  font-size: 13px;
  color: #909399;
  margin-top: 6px;
  line-height: 1.4;
}

.form-tip strong {
  color: #303133;
}

/* 按钮样式调整 */
.danger-section .el-button {
  min-width: 100px;
}

/* 复选框样式 */
:deep(.el-checkbox__label) {
  font-size: 14px;
  line-height: 1.5;
}

/* 下拉选择器样式 */
:deep(.el-select-dropdown__item) {
  height: auto;
  padding: 8px 20px;
}
</style>
