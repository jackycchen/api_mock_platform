<template>
  <div class="api-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <h3>API接口管理</h3>
            <p class="subtitle">
              管理项目的API接口定义
            </p>
          </div>
          <div class="header-right">
            <el-button @click="viewDocumentation">
              <el-icon><Document /></el-icon>
              API文档
            </el-button>
            <el-button @click="showStatistics">
              <el-icon><DataAnalysis /></el-icon>
              统计
            </el-button>
            <el-button
              type="primary"
              @click="showCreateDialog"
            >
              <el-icon><Plus /></el-icon>
              新建接口
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索和筛选 -->
      <div class="search-bar">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索接口名称、路径或描述"
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
              v-model="filterMethod"
              placeholder="请求方法"
              clearable
              @change="handleFilter"
            >
              <el-option
                label="全部"
                value=""
              />
              <el-option
                label="GET"
                value="GET"
              />
              <el-option
                label="POST"
                value="POST"
              />
              <el-option
                label="PUT"
                value="PUT"
              />
              <el-option
                label="DELETE"
                value="DELETE"
              />
              <el-option
                label="PATCH"
                value="PATCH"
              />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select
              v-model="sortBy"
              @change="handleSort"
            >
              <el-option
                label="排序顺序"
                value="sortOrder"
              />
              <el-option
                label="创建时间"
                value="createdAt"
              />
              <el-option
                label="更新时间"
                value="updatedAt"
              />
              <el-option
                label="接口名称"
                value="name"
              />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-checkbox v-model="showOnlyActive">
              仅显示启用的接口
            </el-checkbox>
          </el-col>
        </el-row>
      </div>

      <!-- 批量操作 -->
      <div
        v-if="selectedApis.length > 0"
        class="batch-actions"
      >
        <span class="selected-count">已选择 {{ selectedApis.length }} 个接口</span>
        <el-button
          size="small"
          @click="batchMoveToGroup"
        >
          移动到分组
        </el-button>
        <el-button
          size="small"
          type="danger"
          @click="batchDelete"
        >
          批量删除
        </el-button>
      </div>

      <!-- API列表 -->
      <el-table
        v-loading="loading"
        :data="apiList"
        style="width: 100%"
        empty-text="暂无API接口数据"
        @selection-change="handleSelectionChange"
      >
        <el-table-column
          type="selection"
          width="55"
        />

        <el-table-column
          prop="name"
          label="接口名称"
          min-width="150"
        >
          <template #default="scope">
            <div class="api-name">
              <el-icon class="api-icon">
                <Link />
              </el-icon>
              <span
                class="name-link"
                @click="viewApiDetail(scope.row)"
              >{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column
          prop="method"
          label="请求方法"
          width="100"
        >
          <template #default="scope">
            <el-tag
              :type="getMethodTagType(scope.row.method)"
              size="small"
            >
              {{ scope.row.method }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          prop="path"
          label="接口路径"
          min-width="200"
        >
          <template #default="scope">
            <code class="api-path">{{ scope.row.path }}</code>
          </template>
        </el-table-column>

        <el-table-column
          prop="description"
          label="描述"
          min-width="150"
          show-overflow-tooltip
        />

        <el-table-column
          prop="group"
          label="分组"
          width="120"
        >
          <template #default="scope">
            <el-tag
              v-if="scope.row.group"
              size="small"
              type="info"
            >
              {{ scope.row.group.name }}
            </el-tag>
            <span
              v-else
              class="no-group"
            >未分组</span>
          </template>
        </el-table-column>

        <el-table-column
          prop="status"
          label="状态"
          width="80"
        >
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="toggleApiStatus(scope.row)"
            />
          </template>
        </el-table-column>

        <el-table-column
          prop="updatedAt"
          label="更新时间"
          width="120"
        >
          <template #default="scope">
            {{ formatDate(scope.row.updatedAt) }}
          </template>
        </el-table-column>

        <el-table-column
          label="操作"
          width="200"
          fixed="right"
        >
          <template #default="scope">
            <el-button
              type="text"
              @click="viewApiDetail(scope.row)"
            >
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button
              type="text"
              @click="editApi(scope.row)"
            >
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-dropdown
              trigger="click"
              @command="handleMenuCommand"
            >
              <el-button type="text">
                更多<el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="{action: 'copy', row: scope.row}">
                    <el-icon><DocumentCopy /></el-icon>
                    复制接口
                  </el-dropdown-item>
                  <el-dropdown-item :command="{action: 'test', row: scope.row}">
                    <el-icon><Connection /></el-icon>
                    测试接口
                  </el-dropdown-item>
                  <el-dropdown-item :command="{action: 'mock', row: scope.row}">
                    <el-icon><Tools /></el-icon>
                    配置Mock
                  </el-dropdown-item>
                  <el-dropdown-item
                    :command="{action: 'delete', row: scope.row}"
                    divided
                  >
                    <el-icon><Delete /></el-icon>
                    删除接口
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

    <!-- 创建/编辑API对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑API接口' : '创建API接口'"
      width="800px"
      @close="resetForm"
    >
      <el-form
        ref="apiFormRef"
        :model="apiForm"
        :rules="apiRules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item
              label="接口名称"
              prop="name"
            >
              <el-input
                v-model="apiForm.name"
                placeholder="请输入接口名称"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              label="请求方法"
              prop="method"
            >
              <el-select
                v-model="apiForm.method"
                placeholder="选择请求方法"
                style="width: 100%"
              >
                <el-option
                  label="GET"
                  value="GET"
                />
                <el-option
                  label="POST"
                  value="POST"
                />
                <el-option
                  label="PUT"
                  value="PUT"
                />
                <el-option
                  label="DELETE"
                  value="DELETE"
                />
                <el-option
                  label="PATCH"
                  value="PATCH"
                />
                <el-option
                  label="HEAD"
                  value="HEAD"
                />
                <el-option
                  label="OPTIONS"
                  value="OPTIONS"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item
          label="接口路径"
          prop="path"
        >
          <el-input
            v-model="apiForm.path"
            placeholder="例如: /api/users/{id}"
            clearable
            @blur="checkApiUniqueness"
          >
            <template #prefix>
              <span class="path-prefix">{{ projectBaseUrl }}</span>
            </template>
            <template #suffix>
              <el-icon
                v-if="pathCheckLoading"
                class="is-loading"
              >
                <Loading />
              </el-icon>
              <el-icon
                v-else-if="pathCheckResult === 'available'"
                style="color: #67c23a"
              >
                <SuccessFilled />
              </el-icon>
              <el-icon
                v-else-if="pathCheckResult === 'unavailable'"
                style="color: #f56c6c"
              >
                <CircleCloseFilled />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item
          label="接口描述"
          prop="description"
        >
          <el-input
            v-model="apiForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入接口描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="所属分组">
          <el-select
            v-model="apiForm.groupId"
            placeholder="选择分组（可选）"
            clearable
            style="width: 100%"
          >
            <el-option
              label="未分组"
              :value="null"
            />
            <!-- TODO: 动态加载分组选项 -->
          </el-select>
        </el-form-item>

        <!-- 请求参数配置 -->
        <el-collapse v-model="activeCollapse">
          <el-collapse-item
            title="请求头配置"
            name="headers"
          >
            <api-headers-editor v-model="apiForm.requestHeaders" />
          </el-collapse-item>

          <el-collapse-item
            title="路径参数配置"
            name="pathParams"
          >
            <api-parameters-editor
              v-model="apiForm.pathParameters"
              type="path"
            />
          </el-collapse-item>

          <el-collapse-item
            title="查询参数配置"
            name="queryParams"
          >
            <api-parameters-editor
              v-model="apiForm.queryParameters"
              type="query"
            />
          </el-collapse-item>

          <el-collapse-item
            v-if="needsRequestBody"
            title="请求体配置"
            name="requestBody"
          >
            <json-schema-editor v-model="apiForm.requestBodySchema" />
          </el-collapse-item>

          <el-collapse-item
            title="响应配置"
            name="response"
          >
            <api-response-editor
              v-model:schema="apiForm.responseSchema"
              v-model:examples="apiForm.responseExamples"
            />
          </el-collapse-item>
        </el-collapse>
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

    <!-- API详情对话框 -->
    <api-detail-dialog
      v-model="detailDialogVisible"
      :api-data="currentApiDetail"
      @refresh="handleDetailRefresh"
    />

    <!-- 统计信息对话框 -->
    <api-statistics-dialog
      v-model="statisticsDialogVisible"
      :project-id="projectId"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getProjectApis,
  getApiById,
  createApi,
  updateApi,
  deleteApi,
  checkApiUniqueness as checkApiUniquenessApi,
  batchDeleteApis,
  batchMoveApisToGroup,
  copyApi,
  getApiStatistics
} from '@/api/api'
import { formatDate } from '@/utils/format'
import ApiDetailDialog from '@/components/api/ApiDetailDialog.vue'
import ApiStatisticsDialog from '@/components/api/ApiStatisticsDialog.vue'
import ApiHeadersEditor from '@/components/api/ApiHeadersEditor.vue'
import ApiParametersEditor from '@/components/api/ApiParametersEditor.vue'
import JsonSchemaEditor from '@/components/api/JsonSchemaEditor.vue'
import ApiResponseEditor from '@/components/api/ApiResponseEditor.vue'

// Icons
import {
  Search,
  Plus,
  View,
  Edit,
  Delete,
  ArrowDown,
  DocumentCopy,
  Connection,
  Tools,
  Link,
  DataAnalysis,
  Loading,
  SuccessFilled,
  CircleCloseFilled,
  Document
} from '@element-plus/icons-vue'

// 路由参数
const route = useRoute()
const router = useRouter()
const projectId = computed(() => parseInt(route.params.projectId))

// 状态管理
const loading = ref(false)
const submitLoading = ref(false)
const pathCheckLoading = ref(false)
const pathCheckResult = ref('')

// 数据状态
const apiList = ref([])
const selectedApis = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

// 搜索和筛选状态
const searchKeyword = ref('')
const filterMethod = ref('')
const sortBy = ref('sortOrder')
const showOnlyActive = ref(false)

// 对话框状态
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const statisticsDialogVisible = ref(false)
const isEdit = ref(false)
const currentApiDetail = ref(null)

// 表单状态
const apiFormRef = ref(null)
const activeCollapse = ref(['headers', 'response'])

const apiForm = reactive({
  name: '',
  method: 'GET',
  path: '',
  description: '',
  groupId: null,
  requestHeaders: [],
  pathParameters: [],
  queryParameters: [],
  requestBodySchema: '',
  responseSchema: '',
  responseExamples: []
})

// 项目基础URL（可以从项目信息获取）
const projectBaseUrl = ref('http://localhost:8080')

// 表单验证规则
const apiRules = {
  name: [
    { required: true, message: '请输入接口名称', trigger: 'blur' },
    { min: 2, max: 100, message: '接口名称长度在2到100个字符', trigger: 'blur' }
  ],
  method: [
    { required: true, message: '请选择请求方法', trigger: 'change' }
  ],
  path: [
    { required: true, message: '请输入接口路径', trigger: 'blur' },
    { pattern: /^\//, message: '路径必须以 / 开头', trigger: 'blur' }
  ]
}

// 计算属性
const isFormValid = computed(() => {
  return apiForm.name && apiForm.method && apiForm.path
})

const needsRequestBody = computed(() => {
  return ['POST', 'PUT', 'PATCH'].includes(apiForm.method)
})

// 方法定义
const loadApis = async () => {
  loading.value = true
  try {
    const params = {
      projectId: projectId.value,
      page: currentPage.value - 1,
      size: pageSize.value,
      sortBy: sortBy.value,
      sortDir: 'asc',
      keyword: searchKeyword.value || undefined
    }

    if (filterMethod.value) {
      params.method = filterMethod.value
    }

    const response = await getProjectApis(params)
    apiList.value = response.data.content
    total.value = response.data.totalElements
  } catch (error) {
    ElMessage.error('获取API列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadApis()
}

const handleFilter = () => {
  currentPage.value = 1
  loadApis()
}

const handleSort = () => {
  currentPage.value = 1
  loadApis()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadApis()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadApis()
}

const handleSelectionChange = (selection) => {
  selectedApis.value = selection
}

const showCreateDialog = () => {
  isEdit.value = false
  dialogVisible.value = true
  resetForm()
}

const editApi = (api) => {
  isEdit.value = true
  dialogVisible.value = true

  // 填充表单
  Object.assign(apiForm, {
    ...api,
    requestHeaders: JSON.parse(api.requestHeaders || '[]'),
    pathParameters: JSON.parse(api.pathParameters || '[]'),
    queryParameters: JSON.parse(api.queryParameters || '[]'),
    responseExamples: JSON.parse(api.responseExamples || '[]')
  })

  currentApiDetail.value = api
}

const resetForm = () => {
  if (apiFormRef.value) {
    apiFormRef.value.resetFields()
  }

  Object.assign(apiForm, {
    name: '',
    method: 'GET',
    path: '',
    description: '',
    groupId: null,
    requestHeaders: [],
    pathParameters: [],
    queryParameters: [],
    requestBodySchema: '',
    responseSchema: '',
    responseExamples: []
  })

  pathCheckResult.value = ''
  currentApiDetail.value = null
}

const checkApiUniqueness = async () => {
  if (!apiForm.path || !apiForm.method) return

  pathCheckLoading.value = true
  pathCheckResult.value = ''

  try {
    const params = {
      projectId: projectId.value,
      path: apiForm.path,
      method: apiForm.method
    }

    if (isEdit.value && currentApiDetail.value) {
      params.excludeApiId = currentApiDetail.value.id
    }

    const response = await checkApiUniquenessApi(params)
    pathCheckResult.value = response.data ? 'available' : 'unavailable'
  } catch (error) {
    pathCheckResult.value = 'error'
    console.error('检查API唯一性失败:', error)
  } finally {
    pathCheckLoading.value = false
  }
}

const handleSubmit = async () => {
  if (!apiFormRef.value) return

  try {
    await apiFormRef.value.validate()
  } catch (error) {
    return
  }

  if (pathCheckResult.value === 'unavailable') {
    ElMessage.error('接口路径和方法已存在，请修改后重试')
    return
  }

  submitLoading.value = true

  try {
    const formData = {
      ...apiForm,
      projectId: projectId.value,
      requestHeaders: JSON.stringify(apiForm.requestHeaders),
      pathParameters: JSON.stringify(apiForm.pathParameters),
      queryParameters: JSON.stringify(apiForm.queryParameters),
      responseExamples: JSON.stringify(apiForm.responseExamples)
    }

    if (isEdit.value) {
      await updateApi(currentApiDetail.value.id, formData)
      ElMessage.success('API接口更新成功')
    } else {
      await createApi(formData)
      ElMessage.success('API接口创建成功')
    }

    dialogVisible.value = false
    loadApis()
  } catch (error) {
    ElMessage.error((isEdit.value ? '更新' : '创建') + 'API接口失败：' + error.message)
  } finally {
    submitLoading.value = false
  }
}

const viewApiDetail = async (api) => {
  try {
    const response = await getApiById(api.id)
    currentApiDetail.value = response.data
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取API详情失败:', error)
    ElMessage.error('获取API详情失败: ' + error.message)
  }
}

const handleDetailRefresh = async (apiId) => {
  if (!apiId) return
  try {
    const response = await getApiById(apiId)
    currentApiDetail.value = response.data
    await loadApis()
  } catch (error) {
    console.error('刷新API详情失败:', error)
  }
}

const getMethodTagType = (method) => {
  const types = {
    GET: 'success',
    POST: 'primary',
    PUT: 'warning',
    DELETE: 'danger',
    PATCH: 'info',
    HEAD: '',
    OPTIONS: ''
  }
  return types[method] || ''
}

const toggleApiStatus = async (api) => {
  try {
    await updateApi(api.id, { status: api.status })
    ElMessage.success(`API接口已${api.status === 1 ? '启用' : '禁用'}`)
    loadApis()
  } catch (error) {
    ElMessage.error('更新API状态失败：' + error.message)
    // 恢复原状态
    api.status = api.status === 1 ? 0 : 1
  }
}

const handleMenuCommand = async ({ action, row }) => {
  switch (action) {
    case 'copy':
      await handleCopyApi(row)
      break
    case 'test':
      handleTestApi(row)
      break
    case 'mock':
      handleMockApi(row)
      break
    case 'delete':
      await handleDeleteApi(row)
      break
  }
}

const handleCopyApi = async (api) => {
  try {
    const newName = api.name + ' - 副本'
    await copyApi(api.id, { newName })
    ElMessage.success('复制API接口成功')
    loadApis()
  } catch (error) {
    ElMessage.error('复制API接口失败：' + error.message)
  }
}

const handleTestApi = (api) => {
  ElMessage.info('测试功能正在开发中...')
}

const handleMockApi = (api) => {
  ElMessage.info('Mock配置功能正在开发中...')
}

const handleDeleteApi = async (api) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除接口"${api.name}"吗？此操作不可撤销。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteApi(api.id)
    ElMessage.success('删除API接口成功')
    loadApis()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除API接口失败：' + error.message)
    }
  }
}

const batchDelete = async () => {
  if (selectedApis.value.length === 0) {
    ElMessage.warning('请选择要删除的接口')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedApis.value.length} 个接口吗？此操作不可撤销。`,
      '批量删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const apiIds = selectedApis.value.map(api => api.id)
    await batchDeleteApis(apiIds)
    ElMessage.success(`成功删除 ${selectedApis.value.length} 个接口`)
    selectedApis.value = []
    loadApis()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败：' + error.message)
    }
  }
}

const batchMoveToGroup = async () => {
  if (selectedApis.value.length === 0) {
    ElMessage.warning('请选择要移动的接口')
    return
  }

  ElMessage.info('分组管理功能正在开发中...')
}

const viewDocumentation = () => {
  router.push(`/projects/${projectId.value}/documentation`)
}

const showStatistics = () => {
  router.push(`/projects/${projectId.value}/statistics`)
}

// 监听器
watch([searchKeyword], () => {
  if (searchKeyword.value === '') {
    handleSearch()
  }
})

watch([filterMethod, sortBy, showOnlyActive], () => {
  handleFilter()
})

// 生命周期
onMounted(() => {
  loadApis()
})
</script>

<style scoped>
.api-list {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.header-left h3 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 20px;
  font-weight: 600;
}

.header-left .subtitle {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.header-right {
  display: flex;
  gap: 12px;
}

.search-bar {
  margin-bottom: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.batch-actions {
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #e1f3ff;
  border: 1px solid #b3d8ff;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.selected-count {
  color: #409eff;
  font-weight: 500;
  margin-right: auto;
}

.api-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.api-icon {
  color: #909399;
  font-size: 16px;
}

.name-link {
  color: #409eff;
  cursor: pointer;
  text-decoration: none;
}

.name-link:hover {
  text-decoration: underline;
}

.api-path {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  background: #f1f1f1;
  padding: 2px 6px;
  border-radius: 3px;
  color: #666;
}

.no-group {
  color: #c0c4cc;
  font-style: italic;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.path-prefix {
  color: #909399;
  font-size: 12px;
  padding: 0 8px;
  border-right: 1px solid #e4e7ed;
  margin-right: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .api-list {
    padding: 12px;
  }

  .card-header {
    flex-direction: column;
    gap: 16px;
  }

  .header-right {
    width: 100%;
    justify-content: flex-end;
  }

  .search-bar :deep(.el-row) {
    --el-row-gutter: 12px;
  }

  .search-bar :deep(.el-col) {
    margin-bottom: 12px;
  }
}

/* 表格自定义样式 */
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table .el-table__header-wrapper) {
  border-radius: 8px 8px 0 0;
}

:deep(.el-table .el-table__body-wrapper) {
  border-radius: 0 0 8px 8px;
}

/* 表单样式优化 */
:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

:deep(.el-collapse-item__header) {
  padding-left: 20px;
  font-weight: 500;
}

:deep(.el-collapse-item__content) {
  padding: 20px;
  background: #fafafa;
  border-radius: 4px;
  margin: 0 20px 15px 20px;
}

/* 按钮组样式 */
.header-right .el-button {
  min-width: 100px;
}

/* 状态开关样式 */
:deep(.el-switch) {
  --el-switch-on-color: #13ce66;
  --el-switch-off-color: #ff4949;
}

/* 标签样式 */
:deep(.el-tag) {
  font-weight: 500;
}

/* 下拉菜单样式 */
:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.el-dropdown-menu__item .el-icon) {
  font-size: 14px;
}

/* 加载状态样式 */
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
