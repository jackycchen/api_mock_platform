<template>
  <div class="project-member-management">
    <!-- 成员列表 -->
    <div class="members-section">
      <div class="section-header">
        <h3 class="section-title">
          项目成员
        </h3>
        <el-button
          v-permission="['project:admin']"
          type="primary"
          @click="showInviteDialog"
        >
          <el-icon><Plus /></el-icon>
          邀请成员
        </el-button>
      </div>

      <!-- 搜索和筛选 -->
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索成员姓名或邮箱"
          clearable
          style="width: 300px;"
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select
          v-model="roleFilter"
          placeholder="筛选角色"
          clearable
          style="width: 150px; margin-left: 16px;"
          @change="handleRoleFilter"
        >
          <el-option
            label="全部角色"
            value=""
          />
          <el-option
            v-for="role in projectRoles"
            :key="role.code"
            :label="role.name"
            :value="role.code"
          />
        </el-select>
      </div>

      <!-- 成员表格 -->
      <el-table
        :data="filteredMembers"
        :loading="loading"
        class="members-table"
      >
        <el-table-column
          prop="avatar"
          label="头像"
          width="80"
        >
          <template #default="scope">
            <el-avatar
              :src="scope.row.avatar"
              :size="40"
              style="border: 2px solid #f0f0f0;"
            >
              {{ scope.row.nickname?.charAt(0) || scope.row.username?.charAt(0) }}
            </el-avatar>
          </template>
        </el-table-column>

        <el-table-column
          prop="userInfo"
          label="用户信息"
          min-width="200"
        >
          <template #default="scope">
            <div class="user-info">
              <div class="user-name">
                {{ scope.row.nickname || scope.row.username }}
              </div>
              <div class="user-email">
                {{ scope.row.email }}
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column
          prop="role"
          label="角色"
          width="120"
        >
          <template #default="scope">
            <el-tag
              :type="getRoleType(scope.row.role)"
              size="small"
            >
              {{ getRoleName(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          prop="joinedAt"
          label="加入时间"
          width="180"
        >
          <template #default="scope">
            {{ formatTime(scope.row.joinedAt) }}
          </template>
        </el-table-column>

        <el-table-column
          prop="lastActiveAt"
          label="最近活跃"
          width="180"
        >
          <template #default="scope">
            {{ formatTime(scope.row.lastActiveAt) }}
          </template>
        </el-table-column>

        <el-table-column
          prop="status"
          label="状态"
          width="100"
        >
          <template #default="scope">
            <el-tag
              :type="scope.row.status === 'ACTIVE' ? 'success' : 'warning'"
              size="small"
            >
              {{ scope.row.status === 'ACTIVE' ? '正常' : '待激活' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          label="操作"
          width="160"
          fixed="right"
        >
          <template #default="scope">
            <el-dropdown
              v-if="canManageMember(scope.row)"
              @command="(command) => handleMemberAction(command, scope.row)"
            >
              <el-button
                text
                type="primary"
              >
                管理
                <el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="changeRole">
                    <el-icon><Edit /></el-icon>
                    修改角色
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="scope.row.role !== 'OWNER'"
                    command="remove"
                    divided
                  >
                    <el-icon><Delete /></el-icon>
                    移除成员
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <span
              v-else
              class="no-action"
            >-</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadMembers"
          @size-change="loadMembers"
        />
      </div>
    </div>

    <!-- 邀请成员对话框 -->
    <el-dialog
      v-model="inviteDialog.visible"
      title="邀请成员"
      width="600px"
      @close="resetInviteDialog"
    >
      <el-form
        ref="inviteFormRef"
        :model="inviteForm"
        :rules="inviteRules"
        label-width="100px"
      >
        <el-form-item
          label="邀请方式"
          prop="inviteType"
        >
          <el-radio-group v-model="inviteForm.inviteType">
            <el-radio label="email">
              邮箱邀请
            </el-radio>
            <el-radio label="link">
              邀请链接
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item
          v-if="inviteForm.inviteType === 'email'"
          label="邮箱地址"
          prop="emails"
        >
          <el-input
            v-model="inviteForm.emails"
            type="textarea"
            :rows="3"
            placeholder="输入邮箱地址，多个邮箱用英文逗号分隔"
          />
          <div class="form-tip">
            支持批量邀请，多个邮箱地址请用英文逗号分隔
          </div>
        </el-form-item>

        <el-form-item
          label="分配角色"
          prop="role"
        >
          <el-select
            v-model="inviteForm.role"
            placeholder="选择角色"
          >
            <el-option
              v-for="role in invitableRoles"
              :key="role.code"
              :label="role.name"
              :value="role.code"
            >
              <div class="role-option">
                <span class="role-name">{{ role.name }}</span>
                <span class="role-desc">{{ role.description }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item
          v-if="inviteForm.inviteType === 'link'"
          label="邀请链接"
        >
          <div class="invite-link-container">
            <el-input
              v-model="inviteLink"
              readonly
              placeholder="点击生成邀请链接"
            >
              <template #append>
                <el-button
                  :loading="generatingLink"
                  @click="copyInviteLink"
                >
                  <el-icon><CopyDocument /></el-icon>
                  复制
                </el-button>
              </template>
            </el-input>
            <div class="form-tip">
              邀请链接有效期为7天，过期后需要重新生成
            </div>
          </div>
        </el-form-item>

        <el-form-item
          label="邀请消息"
          prop="message"
        >
          <el-input
            v-model="inviteForm.message"
            type="textarea"
            :rows="3"
            placeholder="可选：添加邀请消息"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="inviteDialog.visible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="inviteDialog.loading"
          @click="handleInvite"
        >
          {{ inviteForm.inviteType === 'email' ? '发送邀请' : '生成链接' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 修改角色对话框 -->
    <el-dialog
      v-model="roleDialog.visible"
      title="修改成员角色"
      width="400px"
      @close="resetRoleDialog"
    >
      <el-form
        ref="roleFormRef"
        :model="roleForm"
        :rules="roleRules"
        label-width="80px"
      >
        <div class="member-info">
          <el-avatar
            :src="roleDialog.member?.avatar"
            :size="50"
          >
            {{ roleDialog.member?.nickname?.charAt(0) }}
          </el-avatar>
          <div class="member-details">
            <div class="member-name">
              {{ roleDialog.member?.nickname }}
            </div>
            <div class="member-email">
              {{ roleDialog.member?.email }}
            </div>
          </div>
        </div>

        <el-form-item
          label="新角色"
          prop="role"
        >
          <el-select
            v-model="roleForm.role"
            placeholder="选择新角色"
          >
            <el-option
              v-for="role in assignableRoles"
              :key="role.code"
              :label="role.name"
              :value="role.code"
              :disabled="role.code === roleDialog.member?.role"
            >
              <div class="role-option">
                <span class="role-name">{{ role.name }}</span>
                <span class="role-desc">{{ role.description }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item
          label="变更原因"
          prop="reason"
        >
          <el-input
            v-model="roleForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入角色变更原因"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="roleDialog.visible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="roleDialog.loading"
          @click="handleRoleChange"
        >
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Search,
  Edit,
  Delete,
  ArrowDown,
  CopyDocument
} from '@element-plus/icons-vue'
import {
  getProjectMembers,
  inviteProjectMember,
  updateMemberRole,
  removeProjectMember,
  getProjectRoles,
  searchUsers,
  getInviteLink
} from '@/api/projectMembers'
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

const userStore = useUserStore()

const loading = ref(false)
const members = ref([])
const projectRoles = ref([])
const searchKeyword = ref('')
const roleFilter = ref('')

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 邀请对话框
const inviteDialog = reactive({
  visible: false,
  loading: false
})

const inviteFormRef = ref()
const inviteForm = reactive({
  inviteType: 'email',
  emails: '',
  role: '',
  message: ''
})

const inviteRules = {
  emails: [
    {
      validator: (rule, value, callback) => {
        if (inviteForm.inviteType === 'email' && (!value || !value.trim())) {
          callback(new Error('请输入邮箱地址'))
        } else if (inviteForm.inviteType === 'email' && value) {
          const emails = value.split(',').map(email => email.trim())
          const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
          const invalidEmails = emails.filter(email => !emailRegex.test(email))
          if (invalidEmails.length > 0) {
            callback(new Error(`邮箱格式不正确: ${invalidEmails.join(', ')}`))
          } else {
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

const inviteLink = ref('')
const generatingLink = ref(false)

// 角色修改对话框
const roleDialog = reactive({
  visible: false,
  loading: false,
  member: null
})

const roleFormRef = ref()
const roleForm = reactive({
  role: '',
  reason: ''
})

const roleRules = {
  role: [
    { required: true, message: '请选择新角色', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请输入变更原因', trigger: 'blur' },
    { min: 5, max: 100, message: '变更原因长度应在5-100个字符之间', trigger: 'blur' }
  ]
}

// 计算属性
const filteredMembers = computed(() => {
  let filtered = members.value

  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(member =>
      member.nickname?.toLowerCase().includes(keyword) ||
      member.username?.toLowerCase().includes(keyword) ||
      member.email?.toLowerCase().includes(keyword)
    )
  }

  // 角色筛选
  if (roleFilter.value) {
    filtered = filtered.filter(member => member.role === roleFilter.value)
  }

  return filtered
})

const invitableRoles = computed(() => {
  return projectRoles.value.filter(role => role.code !== 'OWNER')
})

const assignableRoles = computed(() => {
  const currentUserRole = getCurrentUserRole()
  if (currentUserRole === 'OWNER') {
    return projectRoles.value.filter(role => role.code !== 'OWNER')
  } else if (currentUserRole === 'ADMIN') {
    return projectRoles.value.filter(role =>
      !['OWNER', 'ADMIN'].includes(role.code)
    )
  }
  return []
})

// 方法
const loadMembers = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size
    }
    const response = await getProjectMembers(props.projectId, params)
    members.value = response.data.content
    pagination.total = response.data.totalElements
  } catch (error) {
    ElMessage.error('加载成员列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const loadProjectRoles = async () => {
  try {
    const response = await getProjectRoles()
    projectRoles.value = response.data
  } catch (error) {
    console.error('加载角色列表失败:', error)
  }
}

const getCurrentUserRole = () => {
  const currentUser = userStore.user
  const currentMember = members.value.find(member => member.userId === currentUser.id)
  return currentMember?.role || 'MEMBER'
}

const canManageMember = (member) => {
  const currentUserRole = getCurrentUserRole()
  const currentUser = userStore.user

  // 不能管理自己
  if (member.userId === currentUser.id) {
    return false
  }

  // 只有管理员和所有者可以管理成员
  if (!['OWNER', 'ADMIN'].includes(currentUserRole)) {
    return false
  }

  // 管理员不能管理所有者和其他管理员
  if (currentUserRole === 'ADMIN' && ['OWNER', 'ADMIN'].includes(member.role)) {
    return false
  }

  return true
}

const getRoleType = (role) => {
  switch (role) {
    case 'OWNER': return 'danger'
    case 'ADMIN': return 'warning'
    case 'DEVELOPER': return 'success'
    default: return 'info'
  }
}

const getRoleName = (roleCode) => {
  const role = projectRoles.value.find(r => r.code === roleCode)
  return role?.name || roleCode
}

const handleSearch = debounce(() => {
  // 搜索逻辑已在computed中处理
}, 300)

const handleRoleFilter = () => {
  // 筛选逻辑已在computed中处理
}

const showInviteDialog = () => {
  inviteDialog.visible = true
}

const resetInviteDialog = () => {
  inviteForm.inviteType = 'email'
  inviteForm.emails = ''
  inviteForm.role = ''
  inviteForm.message = ''
  inviteLink.value = ''
  inviteFormRef.value?.clearValidate()
}

const handleInvite = async () => {
  try {
    await inviteFormRef.value.validate()

    if (inviteForm.inviteType === 'link') {
      await generateInviteLink()
      return
    }

    inviteDialog.loading = true
    const emails = inviteForm.emails.split(',').map(email => email.trim())

    const response = await inviteProjectMember(props.projectId, {
      emails,
      role: inviteForm.role,
      message: inviteForm.message
    })

    ElMessage.success(`邀请已发送给 ${emails.length} 个邮箱`)
    inviteDialog.visible = false
    loadMembers()
  } catch (error) {
    if (error.message) {
      ElMessage.error('邀请失败: ' + error.message)
    }
  } finally {
    inviteDialog.loading = false
  }
}

const generateInviteLink = async () => {
  if (!inviteForm.role) {
    ElMessage.error('请先选择角色')
    return
  }

  generatingLink.value = true
  try {
    const response = await getInviteLink(props.projectId, inviteForm.role)
    inviteLink.value = `${window.location.origin}/join?token=${response.data}`
    ElMessage.success('邀请链接生成成功')
  } catch (error) {
    ElMessage.error('生成邀请链接失败: ' + error.message)
  } finally {
    generatingLink.value = false
  }
}

const copyInviteLink = async () => {
  if (!inviteLink.value) {
    await generateInviteLink()
  }

  if (inviteLink.value) {
    try {
      await navigator.clipboard.writeText(inviteLink.value)
      ElMessage.success('邀请链接已复制到剪贴板')
    } catch (error) {
      ElMessage.error('复制失败，请手动复制')
    }
  }
}

const handleMemberAction = (command, member) => {
  switch (command) {
    case 'changeRole':
      showRoleDialog(member)
      break
    case 'remove':
      handleRemoveMember(member)
      break
  }
}

const showRoleDialog = (member) => {
  roleDialog.member = member
  roleForm.role = ''
  roleForm.reason = ''
  roleDialog.visible = true
}

const resetRoleDialog = () => {
  roleDialog.member = null
  roleForm.role = ''
  roleForm.reason = ''
  roleFormRef.value?.clearValidate()
}

const handleRoleChange = async () => {
  try {
    await roleFormRef.value.validate()

    roleDialog.loading = true
    await updateMemberRole(props.projectId, roleDialog.member.id, {
      role: roleForm.role,
      reason: roleForm.reason
    })

    ElMessage.success('成员角色修改成功')
    roleDialog.visible = false
    loadMembers()
  } catch (error) {
    if (error.message) {
      ElMessage.error('角色修改失败: ' + error.message)
    }
  } finally {
    roleDialog.loading = false
  }
}

const handleRemoveMember = async (member) => {
  try {
    await ElMessageBox.confirm(
      `确定要移除成员 "${member.nickname || member.username}" 吗？移除后该成员将无法访问此项目。`,
      '确认移除',
      {
        confirmButtonText: '确定移除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await removeProjectMember(props.projectId, member.id)
    ElMessage.success('成员已移除')
    loadMembers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('移除成员失败: ' + error.message)
    }
  }
}

// 防抖函数
function debounce(func, wait) {
  let timeout
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout)
      func(...args)
    }
    clearTimeout(timeout)
    timeout = setTimeout(later, wait)
  }
}

// 监听项目变化
watch(() => props.projectId, (newId) => {
  if (newId) {
    loadMembers()
  }
})

onMounted(() => {
  loadMembers()
  loadProjectRoles()
})
</script>

<style scoped>
.project-member-management {
  max-width: 1000px;
}

.members-section {
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-title {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
  color: #303133;
}

.search-bar {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.members-table {
  margin-bottom: 20px;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.user-email {
  font-size: 13px;
  color: #909399;
}

.no-action {
  color: #c0c4cc;
  font-size: 14px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.form-tip {
  font-size: 13px;
  color: #909399;
  margin-top: 6px;
  line-height: 1.4;
}

.role-option {
  display: flex;
  flex-direction: column;
}

.role-name {
  font-weight: 500;
  color: #303133;
}

.role-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.invite-link-container {
  width: 100%;
}

.member-info {
  display: flex;
  align-items: center;
  padding: 16px;
  background-color: #f9fafc;
  border-radius: 6px;
  margin-bottom: 20px;
}

.member-details {
  margin-left: 12px;
}

.member-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.member-email {
  font-size: 13px;
  color: #909399;
}

/* 表格行悬停效果 */
:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

/* 角色标签样式调整 */
:deep(.el-tag) {
  font-weight: 500;
}

/* 下拉菜单样式 */
:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
}

:deep(.el-dropdown-menu__item .el-icon) {
  margin-right: 6px;
}
</style>