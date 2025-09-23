<template>
  <div class="permission-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>权限管理</h3>
          <p class="subtitle">
            管理系统权限和角色配置
          </p>
        </div>
      </template>

      <el-tabs
        v-model="activeTab"
        type="border-card"
      >
        <!-- 权限列表 -->
        <el-tab-pane
          label="权限列表"
          name="permissions"
        >
          <div class="permission-list">
            <!-- 搜索和筛选 -->
            <div class="search-bar">
              <el-row :gutter="20">
                <el-col :span="6">
                  <el-input
                    v-model="searchKeyword"
                    placeholder="搜索权限名称或代码"
                    clearable
                    @input="handleSearch"
                  >
                    <template #prefix>
                      <el-icon><Search /></el-icon>
                    </template>
                  </el-input>
                </el-col>
                <el-col :span="4">
                  <el-select
                    v-model="filterCategory"
                    placeholder="权限分类"
                    clearable
                    @change="handleFilter"
                  >
                    <el-option
                      label="全部"
                      value=""
                    />
                    <el-option
                      v-for="category in categories"
                      :key="category"
                      :label="category"
                      :value="category"
                    />
                  </el-select>
                </el-col>
                <el-col :span="4">
                  <el-button
                    type="primary"
                    @click="showCreatePermissionDialog"
                  >
                    <el-icon><Plus /></el-icon>
                    新建权限
                  </el-button>
                </el-col>
              </el-row>
            </div>

            <!-- 权限表格 -->
            <el-table
              :data="permissionList"
              :loading="loading"
              style="width: 100%"
            >
              <el-table-column
                prop="code"
                label="权限代码"
                min-width="150"
              />
              <el-table-column
                prop="name"
                label="权限名称"
                min-width="120"
              />
              <el-table-column
                prop="category"
                label="分类"
                width="100"
              />
              <el-table-column
                prop="description"
                label="描述"
                min-width="200"
                show-overflow-tooltip
              />
              <el-table-column
                prop="enabled"
                label="状态"
                width="80"
              >
                <template #default="scope">
                  <el-tag :type="scope.row.enabled ? 'success' : 'danger'">
                    {{ scope.row.enabled ? '启用' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column
                label="操作"
                width="180"
              >
                <template #default="scope">
                  <el-button
                    type="text"
                    @click="editPermission(scope.row)"
                  >
                    编辑
                  </el-button>
                  <el-button
                    type="text"
                    @click="togglePermissionStatus(scope.row)"
                  >
                    {{ scope.row.enabled ? '禁用' : '启用' }}
                  </el-button>
                  <el-button
                    type="text"
                    style="color: #f56c6c;"
                    @click="handleDeletePermission(scope.row)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 角色权限配置 -->
        <el-tab-pane
          label="角色权限"
          name="roles"
        >
          <div class="role-permissions">
            <el-row :gutter="20">
              <el-col :span="6">
                <el-card>
                  <template #header>
                    <span>角色列表</span>
                  </template>
                  <div class="role-list">
                    <div
                      v-for="role in roles"
                      :key="role.code"
                      class="role-item"
                      :class="{ active: selectedRole === role.code }"
                      @click="selectRole(role.code)"
                    >
                      <div class="role-name">
                        {{ role.name }}
                      </div>
                      <div class="role-description">
                        {{ role.description }}
                      </div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="18">
                <el-card v-if="selectedRole">
                  <template #header>
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                      <span>{{ getRoleDisplayName(selectedRole) }} - 权限配置</span>
                      <el-button
                        type="primary"
                        @click="saveRolePermissions"
                      >
                        保存配置
                      </el-button>
                    </div>
                  </template>
                  <div class="permission-config">
                    <div
                      v-for="category in categories"
                      :key="category"
                      class="permission-category"
                    >
                      <h4>{{ getCategoryDisplayName(category) }}</h4>
                      <el-checkbox-group v-model="rolePermissions">
                        <el-row :gutter="10">
                          <el-col
                            v-for="permission in getPermissionsByCategory(category)"
                            :key="permission.code"
                            :span="12"
                          >
                            <el-checkbox :label="permission.code">
                              {{ permission.name }}
                            </el-checkbox>
                          </el-col>
                        </el-row>
                      </el-checkbox-group>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>

        <!-- 操作日志 -->
        <el-tab-pane
          label="操作日志"
          name="logs"
        >
          <div class="operation-logs">
            <!-- 筛选条件 -->
            <div class="log-filters">
              <el-row :gutter="20">
                <el-col :span="4">
                  <el-select
                    v-model="logFilter.operation"
                    placeholder="操作类型"
                    clearable
                  >
                    <el-option
                      label="全部"
                      value=""
                    />
                    <el-option
                      label="邀请成员"
                      value="INVITE_MEMBER"
                    />
                    <el-option
                      label="移除成员"
                      value="REMOVE_MEMBER"
                    />
                    <el-option
                      label="修改角色"
                      value="CHANGE_ROLE"
                    />
                    <el-option
                      label="权限拒绝"
                      value="PERMISSION_DENIED"
                    />
                  </el-select>
                </el-col>
                <el-col :span="4">
                  <el-date-picker
                    v-model="logFilter.dateRange"
                    type="daterange"
                    range-separator="至"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                  />
                </el-col>
                <el-col :span="4">
                  <el-button @click="loadOperationLogs">
                    查询
                  </el-button>
                </el-col>
              </el-row>
            </div>

            <!-- 日志表格 -->
            <el-table
              :data="logList"
              :loading="logLoading"
              style="width: 100%"
            >
              <el-table-column
                prop="username"
                label="用户"
                width="120"
              />
              <el-table-column
                prop="operation"
                label="操作"
                width="120"
              />
              <el-table-column
                prop="description"
                label="描述"
                min-width="200"
                show-overflow-tooltip
              />
              <el-table-column
                prop="result"
                label="结果"
                width="80"
              >
                <template #default="scope">
                  <el-tag :type="scope.row.result === 'SUCCESS' ? 'success' : 'danger'">
                    {{ scope.row.result === 'SUCCESS' ? '成功' : '失败' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column
                prop="ipAddress"
                label="IP地址"
                width="120"
              />
              <el-table-column
                prop="createdAt"
                label="时间"
                width="180"
              >
                <template #default="scope">
                  {{ formatDate(scope.row.createdAt) }}
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination-container">
              <el-pagination
                v-model:current-page="logPagination.page"
                v-model:page-size="logPagination.size"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="logPagination.total"
                @size-change="handleLogSizeChange"
                @current-change="handleLogCurrentChange"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 权限编辑对话框 -->
    <el-dialog
      v-model="permissionDialogVisible"
      :title="isEditingPermission ? '编辑权限' : '新建权限'"
      width="500px"
    >
      <el-form
        ref="permissionFormRef"
        :model="permissionForm"
        :rules="permissionRules"
        label-width="100px"
      >
        <el-form-item
          label="权限代码"
          prop="code"
        >
          <el-input
            v-model="permissionForm.code"
            :disabled="isEditingPermission"
          />
        </el-form-item>
        <el-form-item
          label="权限名称"
          prop="name"
        >
          <el-input v-model="permissionForm.name" />
        </el-form-item>
        <el-form-item
          label="权限分类"
          prop="category"
        >
          <el-select
            v-model="permissionForm.category"
            placeholder="选择分类"
          >
            <el-option
              v-for="category in categories"
              :key="category"
              :label="category"
              :value="category"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="permissionForm.description"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="permissionDialogVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="submitting"
          @click="savePermission"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import {
  getAllPermissions,
  getAllCategories,
  getRolePermissions,
  createPermission,
  updatePermission,
  deletePermission as deletePermissionApi,
  togglePermission,
  resetRolePermissions,
  getUserOperationLogs,
  searchPermissions
} from '@/api/permission'
import { formatDate } from '@/utils/format'

// 状态管理
const loading = ref(false)
const logLoading = ref(false)
const submitting = ref(false)
const activeTab = ref('permissions')

// 权限列表相关
const permissionList = ref([])
const categories = ref([])
const searchKeyword = ref('')
const filterCategory = ref('')

// 角色权限配置相关
const selectedRole = ref('')
const rolePermissions = ref([])
const roles = ref([
  { code: 'OWNER', name: '所有者', description: '项目所有者，拥有所有权限' },
  { code: 'ADMIN', name: '管理员', description: '项目管理员，拥有大部分权限' },
  { code: 'DEVELOPER', name: '开发者', description: '开发者，可以管理API和Mock' },
  { code: 'VIEWER', name: '观察者', description: '只读权限，只能查看' }
])

// 操作日志相关
const logList = ref([])
const logFilter = reactive({
  operation: '',
  dateRange: []
})
const logPagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 权限编辑对话框
const permissionDialogVisible = ref(false)
const isEditingPermission = ref(false)
const permissionFormRef = ref()
const permissionForm = reactive({
  id: null,
  code: '',
  name: '',
  category: '',
  description: ''
})

const permissionRules = {
  code: [
    { required: true, message: '请输入权限代码', trigger: 'blur' },
    { pattern: /^[a-z]+:[a-z_]+$/, message: '权限代码格式为：category:action', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入权限名称', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择权限分类', trigger: 'change' }
  ]
}

// 计算属性
const getPermissionsByCategory = (category) => {
  return permissionList.value.filter(permission => permission.category === category)
}

// 方法定义
const loadPermissions = async () => {
  loading.value = true
  try {
    const [permissionsRes, categoriesRes] = await Promise.all([
      getAllPermissions(),
      getAllCategories()
    ])
    permissionList.value = permissionsRes.data
    categories.value = categoriesRes.data
  } catch (error) {
    ElMessage.error('加载权限列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    await loadPermissions()
    return
  }

  loading.value = true
  try {
    const response = await searchPermissions(searchKeyword.value)
    permissionList.value = response.data
  } catch (error) {
    ElMessage.error('搜索权限失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const handleFilter = () => {
  if (!filterCategory.value) {
    loadPermissions()
    return
  }

  permissionList.value = permissionList.value.filter(
    permission => permission.category === filterCategory.value
  )
}

const selectRole = async (roleCode) => {
  selectedRole.value = roleCode
  try {
    const response = await getRolePermissions(roleCode)
    rolePermissions.value = response.data
  } catch (error) {
    ElMessage.error('加载角色权限失败：' + error.message)
  }
}

const saveRolePermissions = async () => {
  try {
    await resetRolePermissions(selectedRole.value, rolePermissions.value)
    ElMessage.success('角色权限配置保存成功')
  } catch (error) {
    ElMessage.error('保存角色权限失败：' + error.message)
  }
}

const showCreatePermissionDialog = () => {
  isEditingPermission.value = false
  resetPermissionForm()
  permissionDialogVisible.value = true
}

const editPermission = (permission) => {
  isEditingPermission.value = true
  Object.assign(permissionForm, permission)
  permissionDialogVisible.value = true
}

const resetPermissionForm = () => {
  Object.assign(permissionForm, {
    id: null,
    code: '',
    name: '',
    category: '',
    description: ''
  })
  permissionFormRef.value?.clearValidate()
}

const savePermission = async () => {
  try {
    await permissionFormRef.value.validate()
    submitting.value = true

    if (isEditingPermission.value) {
      await updatePermission(permissionForm.id, {
        name: permissionForm.name,
        category: permissionForm.category,
        description: permissionForm.description
      })
      ElMessage.success('权限更新成功')
    } else {
      await createPermission(permissionForm)
      ElMessage.success('权限创建成功')
    }

    permissionDialogVisible.value = false
    await loadPermissions()
  } catch (error) {
    ElMessage.error('保存权限失败：' + error.message)
  } finally {
    submitting.value = false
  }
}

const togglePermissionStatus = async (permission) => {
  try {
    await togglePermission(permission.id, !permission.enabled)
    permission.enabled = !permission.enabled
    ElMessage.success(`权限已${permission.enabled ? '启用' : '禁用'}`)
  } catch (error) {
    ElMessage.error('操作失败：' + error.message)
  }
}

const handleDeletePermission = async (permission) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除权限"${permission.name}"吗？此操作不可撤销。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deletePermissionApi(permission.id)
    ElMessage.success('权限删除成功')
    await loadPermissions()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除权限失败：' + error.message)
    }
  }
}

const loadOperationLogs = async () => {
  logLoading.value = true
  try {
    // 这里需要根据实际API调整
    const response = await getUserOperationLogs(null, logPagination.page - 1, logPagination.size)
    logList.value = response.data.content
    logPagination.total = response.data.totalElements
  } catch (error) {
    ElMessage.error('加载操作日志失败：' + error.message)
  } finally {
    logLoading.value = false
  }
}

const handleLogSizeChange = (size) => {
  logPagination.size = size
  logPagination.page = 1
  loadOperationLogs()
}

const handleLogCurrentChange = (page) => {
  logPagination.page = page
  loadOperationLogs()
}

const getRoleDisplayName = (roleCode) => {
  const role = roles.value.find(r => r.code === roleCode)
  return role?.name || roleCode
}

const getCategoryDisplayName = (category) => {
  const categoryNames = {
    'project': '项目管理',
    'member': '成员管理',
    'api': 'API接口',
    'mock': 'Mock规则',
    'doc': '文档管理',
    'stats': '统计分析'
  }
  return categoryNames[category] || category
}

// 生命周期
onMounted(() => {
  loadPermissions()
  loadOperationLogs()
})
</script>

<style scoped>
.permission-management {
  padding: 20px;
}

.card-header h3 {
  margin: 0 0 8px 0;
  color: #303133;
}

.subtitle {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.search-bar {
  margin-bottom: 20px;
}

.role-list {
  max-height: 400px;
  overflow-y: auto;
}

.role-item {
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.role-item:hover {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.role-item.active {
  border-color: #409eff;
  background-color: #409eff;
  color: white;
}

.role-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.role-description {
  font-size: 12px;
  opacity: 0.8;
}

.permission-config {
  max-height: 500px;
  overflow-y: auto;
}

.permission-category {
  margin-bottom: 24px;
}

.permission-category h4 {
  margin: 0 0 12px 0;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 8px;
}

.log-filters {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style>
