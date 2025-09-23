<template>
  <div class="mock-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <h3>Mock规则管理</h3>
            <p class="subtitle">
              管理接口的Mock数据和规则
            </p>
          </div>
          <div class="header-right">
            <el-button @click="showStatistics">
              <el-icon><DataAnalysis /></el-icon>
              统计
            </el-button>
            <el-button
              type="primary"
              @click="showCreateDialog"
            >
              <el-icon><Plus /></el-icon>
              新建Mock规则
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
              placeholder="搜索Mock规则名称或描述"
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
              placeholder="Mock类型"
              clearable
              @change="handleFilter"
            >
              <el-option
                label="全部"
                value=""
              />
              <el-option
                label="静态数据"
                value="STATIC"
              />
              <el-option
                label="动态数据"
                value="DYNAMIC"
              />
              <el-option
                label="模板数据"
                value="TEMPLATE"
              />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select
              v-model="filterStatus"
              placeholder="状态"
              clearable
              @change="handleFilter"
            >
              <el-option
                label="全部"
                value=""
              />
              <el-option
                label="已启用"
                value="enabled"
              />
              <el-option
                label="已禁用"
                value="disabled"
              />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select
              v-model="sortBy"
              @change="handleSort"
            >
              <el-option
                label="优先级"
                value="priority"
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
                label="规则名称"
                value="name"
              />
            </el-select>
          </el-col>
        </el-row>
      </div>

      <!-- 批量操作 -->
      <div
        v-if="selectedMockRules.length > 0"
        class="batch-actions"
      >
        <span class="selected-count">已选择 {{ selectedMockRules.length }} 个Mock规则</span>
        <el-button
          size="small"
          @click="batchEnable"
        >
          批量启用
        </el-button>
        <el-button
          size="small"
          @click="batchDisable"
        >
          批量禁用
        </el-button>
        <el-button
          size="small"
          type="danger"
          @click="batchDelete"
        >
          批量删除
        </el-button>
      </div>

      <!-- Mock规则列表 -->
      <el-table
        v-loading="loading"
        :data="mockRuleList"
        style="width: 100%"
        empty-text="暂无Mock规则数据"
        @selection-change="handleSelectionChange"
      >
        <el-table-column
          type="selection"
          width="55"
        />

        <el-table-column
          prop="name"
          label="规则名称"
          min-width="150"
        >
          <template #default="scope">
            <div class="rule-name">
              <el-icon class="rule-icon">
                <Tools />
              </el-icon>
              <span
                class="name-link"
                @click="viewMockRuleDetail(scope.row)"
              >{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column
          prop="apiName"
          label="关联API"
          min-width="180"
        >
          <template #default="scope">
            <div class="api-info">
              <el-tag
                :type="getMethodTagType(scope.row.apiMethod)"
                size="small"
              >
                {{ scope.row.apiMethod }}
              </el-tag>
              <span class="api-name">{{ scope.row.apiName }}</span>
              <code class="api-path">{{ scope.row.apiPath }}</code>
            </div>
          </template>
        </el-table-column>

        <el-table-column
          prop="mockType"
          label="Mock类型"
          width="100"
        >
          <template #default="scope">
            <el-tag
              :type="getMockTypeTagType(scope.row.mockType)"
              size="small"
            >
              {{ getMockTypeLabel(scope.row.mockType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          prop="statusCode"
          label="状态码"
          width="80"
        />

        <el-table-column
          prop="priority"
          label="优先级"
          width="80"
        >
          <template #default="scope">
            <el-tag
              :type="getPriorityTagType(scope.row.priority)"
              size="small"
            >
              {{ scope.row.priority }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          prop="isEnabled"
          label="状态"
          width="80"
        >
          <template #default="scope">
            <el-switch
              v-model="scope.row.isEnabled"
              @change="toggleMockRuleStatus(scope.row)"
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
              @click="previewMockData(scope.row)"
            >
              <el-icon><View /></el-icon>
              预览
            </el-button>
            <el-button
              type="text"
              @click="editMockRule(scope.row)"
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
                    复制规则
                  </el-dropdown-item>
                  <el-dropdown-item :command="{action: 'test', row: scope.row}">
                    <el-icon><Connection /></el-icon>
                    测试规则
                  </el-dropdown-item>
                  <el-dropdown-item
                    :command="{action: 'delete', row: scope.row}"
                    divided
                  >
                    <el-icon><Delete /></el-icon>
                    删除规则
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

    <!-- 高级Mock规则配置对话框 -->
    <el-dialog
      v-model="mockFormDialogVisible"
      :title="currentMockRule ? '编辑Mock规则' : '创建Mock规则'"
      width="90%"
      :close-on-click-modal="false"
      append-to-body
    >
      <AdvancedMockForm
        :project-id="projectId"
        :mock-rule="currentMockRule"
        :api-list="apiList"
        @submit="handleMockFormSubmit"
        @cancel="handleMockFormCancel"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getProjectMockRules,
  createMockRule,
  updateMockRule,
  deleteMockRule,
  toggleMockRuleStatus as toggleMockRuleStatusApi,
  batchUpdateMockRuleStatus,
  copyMockRule,
  previewMockData as previewMockDataApi
} from '@/api/mock'
import { getProjectApis } from '@/api/api'
import { formatDate } from '@/utils/format'

// Components
import AdvancedMockForm from '@/components/mock/AdvancedMockForm.vue'

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
  DataAnalysis
} from '@element-plus/icons-vue'

// 路由参数
const route = useRoute()
const projectId = computed(() => parseInt(route.params.projectId))

// 状态管理
const loading = ref(false)
const mockRuleList = ref([])
const selectedMockRules = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

// 搜索和筛选状态
const searchKeyword = ref('')
const filterType = ref('')
const filterStatus = ref('')
const sortBy = ref('priority')

// 对话框状态
const statisticsDialogVisible = ref(false)
const mockFormDialogVisible = ref(false)
const currentMockRule = ref(null)

// API列表数据
const apiList = ref([])

// 方法定义
const loadMockRules = async () => {
  loading.value = true
  try {
    const params = {
      projectId: projectId.value,
      page: currentPage.value - 1,
      size: pageSize.value,
      sortBy: sortBy.value,
      sortDir: 'desc',
      keyword: searchKeyword.value || undefined
    }

    const response = await getProjectMockRules(params)
    mockRuleList.value = response.data.content
    total.value = response.data.totalElements
  } catch (error) {
    ElMessage.error('获取Mock规则列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadMockRules()
}

const handleFilter = () => {
  currentPage.value = 1
  loadMockRules()
}

const handleSort = () => {
  currentPage.value = 1
  loadMockRules()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadMockRules()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadMockRules()
}

const handleSelectionChange = (selection) => {
  selectedMockRules.value = selection
}

const showCreateDialog = () => {
  currentMockRule.value = null
  mockFormDialogVisible.value = true
}

const editMockRule = (mockRule) => {
  currentMockRule.value = { ...mockRule }
  mockFormDialogVisible.value = true
}

const viewMockRuleDetail = (mockRule) => {
  ElMessage.info('查看Mock规则详情功能正在开发中...')
}

const previewMockData = async (mockRule) => {
  try {
    const response = await previewMockDataApi(mockRule.id)
    ElMessage.success('获取Mock数据预览成功')
    // 这里可以显示预览对话框
    console.log('Mock数据预览:', response.data)
  } catch (error) {
    ElMessage.error('获取Mock数据预览失败：' + error.message)
  }
}

const toggleMockRuleStatus = async (mockRule) => {
  try {
    await toggleMockRuleStatusApi(mockRule.id, mockRule.isEnabled)
    ElMessage.success(`Mock规则已${mockRule.isEnabled ? '启用' : '禁用'}`)
  } catch (error) {
    ElMessage.error('更新Mock规则状态失败：' + error.message)
    // 恢复原状态
    mockRule.isEnabled = !mockRule.isEnabled
  }
}

const handleMenuCommand = async ({ action, row }) => {
  switch (action) {
    case 'copy':
      await handleCopyMockRule(row)
      break
    case 'test':
      handleTestMockRule(row)
      break
    case 'delete':
      await handleDeleteMockRule(row)
      break
  }
}

const handleCopyMockRule = async (mockRule) => {
  try {
    const newName = mockRule.name + ' - 副本'
    await copyMockRule(mockRule.id, newName)
    ElMessage.success('复制Mock规则成功')
    loadMockRules()
  } catch (error) {
    ElMessage.error('复制Mock规则失败：' + error.message)
  }
}

const handleTestMockRule = (mockRule) => {
  ElMessage.info('测试Mock规则功能正在开发中...')
}

const handleDeleteMockRule = async (mockRule) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除Mock规则"${mockRule.name}"吗？此操作不可撤销。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteMockRule(mockRule.id)
    ElMessage.success('删除Mock规则成功')
    loadMockRules()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除Mock规则失败：' + error.message)
    }
  }
}

const batchEnable = async () => {
  if (selectedMockRules.value.length === 0) return

  try {
    const ids = selectedMockRules.value.map(rule => rule.id)
    await batchUpdateMockRuleStatus({ ids, isEnabled: true })
    ElMessage.success(`成功启用 ${selectedMockRules.value.length} 个Mock规则`)
    selectedMockRules.value = []
    loadMockRules()
  } catch (error) {
    ElMessage.error('批量启用失败：' + error.message)
  }
}

const batchDisable = async () => {
  if (selectedMockRules.value.length === 0) return

  try {
    const ids = selectedMockRules.value.map(rule => rule.id)
    await batchUpdateMockRuleStatus({ ids, isEnabled: false })
    ElMessage.success(`成功禁用 ${selectedMockRules.value.length} 个Mock规则`)
    selectedMockRules.value = []
    loadMockRules()
  } catch (error) {
    ElMessage.error('批量禁用失败：' + error.message)
  }
}

const batchDelete = async () => {
  if (selectedMockRules.value.length === 0) return

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedMockRules.value.length} 个Mock规则吗？此操作不可撤销。`,
      '批量删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    for (const rule of selectedMockRules.value) {
      await deleteMockRule(rule.id)
    }

    ElMessage.success(`成功删除 ${selectedMockRules.value.length} 个Mock规则`)
    selectedMockRules.value = []
    loadMockRules()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败：' + error.message)
    }
  }
}

const showStatistics = () => {
  statisticsDialogVisible.value = true
}

// 加载API列表
const loadApiList = async () => {
  try {
    const response = await getProjectApis(projectId.value)
    apiList.value = response.data.content || response.data
  } catch (error) {
    console.error('获取API列表失败：', error)
  }
}

// 表单提交处理
const handleMockFormSubmit = async (mockRuleData) => {
  try {
    if (currentMockRule.value) {
      // 更新现有规则
      await updateMockRule(currentMockRule.value.id, mockRuleData)
      ElMessage.success('更新Mock规则成功')
    } else {
      // 创建新规则
      await createMockRule(mockRuleData)
      ElMessage.success('创建Mock规则成功')
    }

    mockFormDialogVisible.value = false
    loadMockRules()
  } catch (error) {
    ElMessage.error(`${currentMockRule.value ? '更新' : '创建'}Mock规则失败：` + error.message)
  }
}

// 表单取消处理
const handleMockFormCancel = () => {
  mockFormDialogVisible.value = false
  currentMockRule.value = null
}

// 工具方法
const getMethodTagType = (method) => {
  const types = {
    GET: 'success',
    POST: 'primary',
    PUT: 'warning',
    DELETE: 'danger',
    PATCH: 'info'
  }
  return types[method] || ''
}

const getMockTypeTagType = (type) => {
  const types = {
    STATIC: 'success',
    DYNAMIC: 'primary',
    TEMPLATE: 'warning'
  }
  return types[type] || ''
}

const getMockTypeLabel = (type) => {
  const labels = {
    STATIC: '静态',
    DYNAMIC: '动态',
    TEMPLATE: '模板'
  }
  return labels[type] || type
}

const getPriorityTagType = (priority) => {
  if (priority >= 8) return 'danger'
  if (priority >= 5) return 'warning'
  if (priority >= 2) return 'primary'
  return 'info'
}

// 监听器
watch([searchKeyword], () => {
  if (searchKeyword.value === '') {
    handleSearch()
  }
})

watch([filterType, filterStatus], () => {
  handleFilter()
})

// 生命周期
onMounted(() => {
  loadMockRules()
  loadApiList()
})
</script>

<style scoped>
.mock-list {
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

.rule-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.rule-icon {
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

.api-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.api-name {
  color: #606266;
  font-size: 13px;
}

.api-path {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  background: #f1f1f1;
  padding: 2px 6px;
  border-radius: 3px;
  color: #666;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .mock-list {
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

  .api-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
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
</style>