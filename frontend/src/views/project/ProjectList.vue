<template>
  <div class="project-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <h3>项目管理</h3>
            <p class="subtitle">
              管理您的API Mock项目
            </p>
          </div>
          <el-button
            type="primary"
            @click="showCreateDialog"
          >
            <el-icon><Plus /></el-icon>
            新建项目
          </el-button>
        </div>
      </template>

      <!-- 搜索和筛选 -->
      <div class="search-bar">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索项目名称或描述"
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
              v-model="filterType"
              placeholder="项目类型"
              clearable
              @change="handleFilter"
            >
              <el-option
                label="全部"
                value=""
              />
              <el-option
                label="Web API"
                value="WEB_API"
              />
              <el-option
                label="REST API"
                value="REST_API"
              />
              <el-option
                label="GraphQL"
                value="GRAPHQL_API"
              />
              <el-option
                label="微服务"
                value="MICROSERVICE"
              />
              <el-option
                label="移动端API"
                value="MOBILE_API"
              />
              <el-option
                label="物联网API"
                value="IOT_API"
              />
              <el-option
                label="自定义"
                value="CUSTOM"
              />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select
              v-model="sortBy"
              @change="handleSort"
            >
              <el-option
                label="最近更新"
                value="updatedAt"
              />
              <el-option
                label="创建时间"
                value="createdAt"
              />
              <el-option
                label="项目名称"
                value="name"
              />
              <el-option
                label="API数量"
                value="apiCount"
              />
            </el-select>
          </el-col>
        </el-row>
      </div>

      <!-- 项目列表 -->
      <el-table
        v-loading="loading"
        :data="projectList"
        style="width: 100%"
        empty-text="暂无项目数据"
      >
        <el-table-column
          prop="name"
          label="项目名称"
          min-width="150"
        >
          <template #default="scope">
            <div class="project-name">
              <el-icon class="project-icon">
                <FolderOpened />
              </el-icon>
              <span
                class="name-link"
                @click="viewProject(scope.row)"
              >{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column
          prop="description"
          label="项目描述"
          min-width="200"
          show-overflow-tooltip
        />

        <el-table-column
          prop="type"
          label="项目类型"
          width="120"
        >
          <template #default="scope">
            <el-tag
              :type="getTypeTagType(scope.row.type)"
              size="small"
            >
              {{ getTypeDisplayName(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          prop="visibility"
          label="可见性"
          width="100"
        >
          <template #default="scope">
            <el-tag
              :type="scope.row.visibility === 'PUBLIC' ? 'success' : 'info'"
              size="small"
            >
              {{ scope.row.visibility === 'PUBLIC' ? '公开' : '私有' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          prop="owner"
          label="创建者"
          width="120"
        >
          <template #default="scope">
            <div class="owner-info">
              <el-avatar
                :size="24"
                class="owner-avatar"
              >
                {{ scope.row.owner?.username?.charAt(0)?.toUpperCase() }}
              </el-avatar>
              <span class="owner-name">{{ scope.row.owner?.username }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column
          label="统计信息"
          width="150"
        >
          <template #default="scope">
            <div class="stats-info">
              <div><span class="stat-label">API:</span> {{ scope.row.apiCount || 0 }}</div>
              <div><span class="stat-label">成员:</span> {{ scope.row.memberCount || 0 }}</div>
              <div><span class="stat-label">Mock:</span> {{ scope.row.mockCount || 0 }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column
          prop="updatedAt"
          label="最近更新"
          width="120"
        >
          <template #default="scope">
            {{ formatDate(scope.row.updatedAt) }}
          </template>
        </el-table-column>

        <el-table-column
          label="操作"
          width="180"
          fixed="right"
        >
          <template #default="scope">
            <el-button
              type="text"
              @click="viewProject(scope.row)"
            >
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button
              v-if="canEdit(scope.row)"
              type="text"
              @click="editProject(scope.row)"
            >
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-dropdown
              v-if="canEdit(scope.row)"
              @command="handleMenuCommand"
            >
              <el-button type="text">
                更多<el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="{action: 'delete', row: scope.row}">
                    <el-icon><Delete /></el-icon>
                    删除项目
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 创建/编辑项目对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑项目' : '创建项目'"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="projectFormRef"
        :model="projectForm"
        :rules="projectRules"
        label-width="100px"
      >
        <el-form-item
          label="项目名称"
          prop="name"
        >
          <el-input
            v-model="projectForm.name"
            placeholder="请输入项目名称"
            clearable
            @blur="checkProjectNameAvailability"
          >
            <template #suffix>
              <el-icon
                v-if="nameCheckLoading"
                class="is-loading"
              >
                <Loading />
              </el-icon>
              <el-icon
                v-else-if="nameCheckResult === 'available'"
                style="color: #67c23a"
              >
                <SuccessFilled />
              </el-icon>
              <el-icon
                v-else-if="nameCheckResult === 'unavailable'"
                style="color: #f56c6c"
              >
                <CircleCloseFilled />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item
          label="项目描述"
          prop="description"
        >
          <el-input
            v-model="projectForm.description"
            type="textarea"
            :rows="3"
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
            v-model="projectForm.type"
            placeholder="请选择项目类型"
          >
            <el-option
              label="Web API"
              value="WEB_API"
            />
            <el-option
              label="REST API"
              value="REST_API"
            />
            <el-option
              label="GraphQL API"
              value="GRAPHQL_API"
            />
            <el-option
              label="微服务"
              value="MICROSERVICE"
            />
            <el-option
              label="移动端API"
              value="MOBILE_API"
            />
            <el-option
              label="物联网API"
              value="IOT_API"
            />
            <el-option
              label="自定义"
              value="CUSTOM"
            />
          </el-select>
        </el-form-item>

        <el-form-item
          label="可见性"
          prop="visibility"
        >
          <el-radio-group v-model="projectForm.visibility">
            <el-radio label="PRIVATE">
              <div class="radio-option">
                <div class="radio-title">
                  私有项目
                </div>
                <div class="radio-desc">
                  仅项目成员可见
                </div>
              </div>
            </el-radio>
            <el-radio label="PUBLIC">
              <div class="radio-option">
                <div class="radio-title">
                  公开项目
                </div>
                <div class="radio-desc">
                  所有人可见
                </div>
              </div>
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item
          label="基础URL"
          prop="baseUrl"
        >
          <el-input
            v-model="projectForm.baseUrl"
            placeholder="例如: https://api.example.com"
            clearable
          />
        </el-form-item>

        <el-form-item
          label="环境"
          prop="environment"
        >
          <el-select
            v-model="projectForm.environment"
            placeholder="请选择环境"
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
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="submitLoading"
            :disabled="!isFormValid"
            @click="handleSubmit"
          >
            {{ isEdit ? '更新' : '创建' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Search, View, Edit, Delete, ArrowDown, FolderOpened,
  Loading, SuccessFilled, CircleCloseFilled
} from '@element-plus/icons-vue'
import {
  getProjects,
  createProject,
  updateProject,
  deleteProject as deleteProjectAPI,
  checkProjectName
} from '@/api/project'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const nameCheckLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const nameCheckResult = ref('')
const projectFormRef = ref()

// 项目列表相关
const projectList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 搜索筛选相关
const searchKeyword = ref('')
const filterType = ref('')
const sortBy = ref('updatedAt')
const sortDir = ref('desc')

// 项目表单
const projectForm = reactive({
  name: '',
  description: '',
  type: 'WEB_API',
  visibility: 'PRIVATE',
  baseUrl: '',
  environment: 'development'
})

const currentProject = ref(null)

// 表单验证规则
const projectRules = reactive({
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { min: 2, max: 100, message: '项目名称长度在2到100个字符之间', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择项目类型', trigger: 'change' }
  ],
  visibility: [
    { required: true, message: '请选择项目可见性', trigger: 'change' }
  ],
  baseUrl: [
    { pattern: /^https?:\/\/.+/, message: '基础URL格式不正确', trigger: 'blur' }
  ]
})

// 计算属性
const isFormValid = computed(() => {
  return projectForm.name &&
         projectForm.type &&
         projectForm.visibility &&
         (!isEdit.value || nameCheckResult.value === 'available' || projectForm.name === currentProject.value?.name)
})

// 防抖函数
const debounce = (func, delay) => {
  let timeoutId
  return (...args) => {
    clearTimeout(timeoutId)
    timeoutId = setTimeout(() => func.apply(null, args), delay)
  }
}

// 方法
const loadProjects = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      sortBy: sortBy.value,
      sortDir: sortDir.value
    }

    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }

    const response = await getProjects(params)
    const data = response.data

    projectList.value = data.content || []
    total.value = data.totalElements || 0
  } catch (error) {
    console.error('加载项目列表失败:', error)
    ElMessage.error('加载项目列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = debounce(() => {
  currentPage.value = 1
  loadProjects()
}, 500)

const handleFilter = () => {
  currentPage.value = 1
  loadProjects()
}

const handleSort = () => {
  loadProjects()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  loadProjects()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadProjects()
}

const showCreateDialog = () => {
  isEdit.value = false
  currentProject.value = null
  resetForm()
  dialogVisible.value = true
}

const resetForm = () => {
  if (projectFormRef.value) {
    projectFormRef.value.resetFields()
  }

  Object.assign(projectForm, {
    name: '',
    description: '',
    type: 'WEB_API',
    visibility: 'PRIVATE',
    baseUrl: '',
    environment: 'development'
  })

  nameCheckResult.value = ''
}

const checkProjectNameAvailability = debounce(async () => {
  if (!projectForm.name || projectForm.name.length < 2) {
    nameCheckResult.value = ''
    return
  }

  // 如果是编辑模式且名称未改变，跳过检查
  if (isEdit.value && projectForm.name === currentProject.value?.name) {
    nameCheckResult.value = 'available'
    return
  }

  nameCheckLoading.value = true
  try {
    const response = await checkProjectName(projectForm.name)
    nameCheckResult.value = response.data ? 'available' : 'unavailable'

    if (!response.data) {
      ElMessage.warning('项目名称已被占用')
    }
  } catch (error) {
    console.error('检查项目名称失败:', error)
    nameCheckResult.value = ''
  } finally {
    nameCheckLoading.value = false
  }
}, 500)

const handleSubmit = async () => {
  if (!projectFormRef.value) return

  try {
    await projectFormRef.value.validate()
  } catch (error) {
    console.log('表单验证失败:', error)
    return
  }

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateProject(currentProject.value.id, projectForm)
      ElMessage.success('项目更新成功')
    } else {
      const response = await createProject(projectForm)
      ElMessage.success('项目创建成功')

      // 创建成功后可以跳转到项目详情页
      if (response.data?.id) {
        router.push(`/projects/${response.data.id}/apis`)
        return
      }
    }

    dialogVisible.value = false
    loadProjects()
  } catch (error) {
    console.error('操作失败:', error)
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error(isEdit.value ? '更新项目失败' : '创建项目失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const viewProject = (project) => {
  router.push(`/projects/${project.id}/apis`)
}

const editProject = (project) => {
  isEdit.value = true
  currentProject.value = project

  Object.assign(projectForm, {
    name: project.name,
    description: project.description || '',
    type: project.type,
    visibility: project.visibility,
    baseUrl: project.baseUrl || '',
    environment: project.environment || 'development'
  })

  nameCheckResult.value = 'available'
  dialogVisible.value = true
}

const handleMenuCommand = async (command) => {
  const { action, row } = command

  if (action === 'delete') {
    await handleDeleteProject(row)
  }
}

const handleDeleteProject = async (project) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除项目 "${project.name}" 吗？删除后无法恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteProjectAPI(project.id)
    ElMessage.success('项目删除成功')
    loadProjects()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除项目失败:', error)
      ElMessage.error('删除项目失败')
    }
  }
}

const canEdit = (project) => {
  const currentUser = userStore.userInfo
  return currentUser && project.owner && project.owner.id === currentUser.id
}

const getTypeTagType = (type) => {
  const typeMap = {
    'WEB_API': '',
    'REST_API': 'success',
    'GRAPHQL_API': 'info',
    'MICROSERVICE': 'warning',
    'MOBILE_API': 'danger',
    'IOT_API': 'success',
    'CUSTOM': 'info'
  }
  return typeMap[type] || ''
}

const getTypeDisplayName = (type) => {
  const typeMap = {
    'WEB_API': 'Web API',
    'REST_API': 'REST API',
    'GRAPHQL_API': 'GraphQL',
    'MICROSERVICE': '微服务',
    'MOBILE_API': '移动端',
    'IOT_API': '物联网',
    'CUSTOM': '自定义'
  }
  return typeMap[type] || type
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleDateString('zh-CN', {
    month: '2-digit',
    day: '2-digit'
  })
}

// 监听器
watch(() => projectForm.name, () => {
  nameCheckResult.value = ''
})

// 生命周期
onMounted(() => {
  loadProjects()
})
</script>

<style scoped>
.project-list {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left h3 {
  margin: 0 0 4px 0;
  color: #303133;
  font-size: 20px;
  font-weight: 600;
}

.subtitle {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.search-bar {
  margin-bottom: 20px;
}

.project-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.project-icon {
  color: #409eff;
  font-size: 16px;
}

.name-link {
  color: #409eff;
  cursor: pointer;
  font-weight: 500;
  text-decoration: none;
}

.name-link:hover {
  color: #66b1ff;
  text-decoration: underline;
}

.owner-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.owner-avatar {
  background-color: #409eff;
  color: white;
  font-size: 12px;
  font-weight: 600;
}

.owner-name {
  font-size: 13px;
  color: #606266;
}

.stats-info {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

.stat-label {
  font-weight: 500;
  color: #606266;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.radio-option {
  margin-left: 8px;
}

.radio-title {
  font-weight: 500;
  color: #303133;
  margin-bottom: 2px;
}

.radio-desc {
  font-size: 12px;
  color: #909399;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
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

:deep(.el-table) {
  --el-table-header-bg-color: #fafafa;
  --el-table-header-text-color: #606266;
}

:deep(.el-table__header-wrapper) {
  .el-table__header {
    th {
      background-color: var(--el-table-header-bg-color);
      color: var(--el-table-header-text-color);
      font-weight: 600;
    }
  }
}

:deep(.el-dialog__header) {
  padding: 20px 20px 10px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 20px;
}

:deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

:deep(.el-radio) {
  margin-bottom: 12px;
  align-items: flex-start;
}

:deep(.el-radio__label) {
  padding-left: 8px;
}
</style>