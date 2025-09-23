<template>
  <div class="mock-call-logs">
    <el-card>
      <template #header>
        <div class="header-content">
          <h3>Mock调用日志</h3>
          <div class="header-actions">
            <el-button
              :loading="loading.callLogs"
              @click="refreshLogs"
            >
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button
              v-permission="['mock:admin']"
              type="warning"
              @click="handleCleanupLogs"
            >
              <el-icon><Delete /></el-icon>
              清理日志
            </el-button>
            <el-button
              v-permission="['mock:read']"
              @click="exportLogs"
            >
              <el-icon><Download /></el-icon>
              导出
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索过滤器 -->
      <div class="filter-section">
        <el-form
          :model="filterForm"
          inline
        >
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="filterForm.timeRange"
              type="datetimerange"
              :shortcuts="timeShortcuts"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 350px;"
            />
          </el-form-item>

          <el-form-item label="API ID">
            <el-input
              v-model="filterForm.apiId"
              placeholder="输入API ID"
              style="width: 120px;"
            />
          </el-form-item>

          <el-form-item label="状态码">
            <el-select
              v-model="filterForm.statusCode"
              placeholder="选择状态码"
              clearable
              style="width: 120px;"
            >
              <el-option
                label="200"
                value="200"
              />
              <el-option
                label="201"
                value="201"
              />
              <el-option
                label="400"
                value="400"
              />
              <el-option
                label="401"
                value="401"
              />
              <el-option
                label="403"
                value="403"
              />
              <el-option
                label="404"
                value="404"
              />
              <el-option
                label="500"
                value="500"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="客户端IP">
            <el-input
              v-model="filterForm.clientIp"
              placeholder="输入IP地址"
              style="width: 140px;"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              @click="handleSearch"
            >
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">
              <el-icon><RefreshRight /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 日志表格 -->
      <el-table
        :data="callLogs.content"
        :loading="loading.callLogs"
        row-key="id"
        empty-text="暂无调用日志"
        @row-click="handleRowClick"
      >
        <el-table-column
          prop="id"
          label="ID"
          width="80"
        />

        <el-table-column
          prop="callTime"
          label="调用时间"
          width="180"
        >
          <template #default="scope">
            {{ formatTime(scope.row.callTime) }}
          </template>
        </el-table-column>

        <el-table-column
          prop="method"
          label="方法"
          width="80"
        >
          <template #default="scope">
            <el-tag :type="getMethodType(scope.row.method)">
              {{ scope.row.method }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          prop="path"
          label="路径"
          show-overflow-tooltip
        />

        <el-table-column
          prop="statusCode"
          label="状态码"
          width="80"
        >
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.statusCode)">
              {{ scope.row.statusCode }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          prop="responseTime"
          label="响应时间"
          width="100"
        >
          <template #default="scope">
            <span :class="getResponseTimeClass(scope.row.responseTime)">
              {{ scope.row.responseTime }}ms
            </span>
          </template>
        </el-table-column>

        <el-table-column
          prop="mockType"
          label="Mock类型"
          width="100"
        >
          <template #default="scope">
            <el-tag :type="getMockTypeTag(scope.row.mockType)">
              {{ scope.row.mockType }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          prop="clientIp"
          label="客户端IP"
          width="120"
        />

        <el-table-column
          prop="userAgent"
          label="User Agent"
          show-overflow-tooltip
        >
          <template #default="scope">
            <span
              class="user-agent"
              :title="scope.row.userAgent"
            >
              {{ getUserAgentSummary(scope.row.userAgent) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column
          label="操作"
          width="100"
          fixed="right"
        >
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click.stop="handleViewDetail(scope.row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="callLogs.totalElements"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 日志详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="调用详情"
      width="80%"
      destroy-on-close
    >
      <div
        v-if="selectedLog"
        class="log-detail"
      >
        <el-descriptions
          border
          :column="2"
        >
          <el-descriptions-item label="调用ID">
            {{ selectedLog.id }}
          </el-descriptions-item>
          <el-descriptions-item label="调用时间">
            {{ formatTime(selectedLog.callTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="API ID">
            {{ selectedLog.apiId }}
          </el-descriptions-item>
          <el-descriptions-item label="项目ID">
            {{ selectedLog.projectId }}
          </el-descriptions-item>
          <el-descriptions-item label="请求方法">
            <el-tag :type="getMethodType(selectedLog.method)">
              {{ selectedLog.method }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="请求路径">
            {{ selectedLog.path }}
          </el-descriptions-item>
          <el-descriptions-item label="状态码">
            <el-tag :type="getStatusType(selectedLog.statusCode)">
              {{ selectedLog.statusCode }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="响应时间">
            <span :class="getResponseTimeClass(selectedLog.responseTime)">
              {{ selectedLog.responseTime }}ms
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="Mock类型">
            <el-tag :type="getMockTypeTag(selectedLog.mockType)">
              {{ selectedLog.mockType }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="Mock规则ID">
            {{ selectedLog.mockRuleId || '无' }}
          </el-descriptions-item>
          <el-descriptions-item label="客户端IP">
            {{ selectedLog.clientIp }}
          </el-descriptions-item>
          <el-descriptions-item
            label="User Agent"
            :span="2"
          >
            {{ selectedLog.userAgent }}
          </el-descriptions-item>
        </el-descriptions>

        <div
          v-if="selectedLog.requestHeaders"
          class="detail-section"
        >
          <h4>请求头</h4>
          <el-input
            type="textarea"
            :model-value="formatJson(selectedLog.requestHeaders)"
            :rows="6"
            readonly
          />
        </div>

        <div
          v-if="selectedLog.requestBody"
          class="detail-section"
        >
          <h4>请求体</h4>
          <el-input
            type="textarea"
            :model-value="formatJson(selectedLog.requestBody)"
            :rows="8"
            readonly
          />
        </div>

        <div
          v-if="selectedLog.responseBody"
          class="detail-section"
        >
          <h4>响应体</h4>
          <el-input
            type="textarea"
            :model-value="formatJson(selectedLog.responseBody)"
            :rows="10"
            readonly
          />
        </div>

        <div
          v-if="selectedLog.errorMessage"
          class="detail-section"
        >
          <h4>错误信息</h4>
          <el-alert
            :title="selectedLog.errorMessage"
            type="error"
            :closable="false"
          />
        </div>
      </div>
    </el-dialog>

    <!-- 清理日志对话框 -->
    <el-dialog
      v-model="cleanupDialogVisible"
      title="清理日志"
      width="500px"
    >
      <div class="cleanup-form">
        <el-alert
          title="清理过期日志"
          description="删除指定时间之前的所有调用日志，此操作不可恢复。"
          type="warning"
          :closable="false"
          show-icon
        />

        <el-form
          :model="cleanupForm"
          label-width="120px"
          style="margin-top: 20px;"
        >
          <el-form-item label="清理时间点">
            <el-date-picker
              v-model="cleanupForm.beforeTime"
              type="datetime"
              placeholder="选择时间点"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>

          <el-form-item label="快速选择">
            <el-radio-group
              v-model="cleanupForm.quickSelect"
              @change="handleQuickSelect"
            >
              <el-radio label="7days">
                7天前
              </el-radio>
              <el-radio label="30days">
                30天前
              </el-radio>
              <el-radio label="90days">
                90天前
              </el-radio>
              <el-radio label="custom">
                自定义
              </el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="cleanupDialogVisible = false">
          取消
        </el-button>
        <el-button
          type="danger"
          :disabled="!cleanupForm.beforeTime"
          @click="confirmCleanup"
        >
          确认清理
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Refresh,
  Delete,
  Download,
  Search,
  RefreshRight
} from '@element-plus/icons-vue'
import { useMockDataStore } from '@/stores/mockData'
import { storeToRefs } from 'pinia'
import { formatTime } from '@/utils/date'

const props = defineProps({
  projectId: {
    type: [String, Number],
    default: null
  },
  apiId: {
    type: [String, Number],
    default: null
  }
})

const mockDataStore = useMockDataStore()
const {
  callLogs,
  loading
} = storeToRefs(mockDataStore)

// 本地状态
const detailVisible = ref(false)
const cleanupDialogVisible = ref(false)
const selectedLog = ref(null)

// 过滤表单
const filterForm = ref({
  timeRange: [],
  apiId: '',
  statusCode: '',
  clientIp: ''
})

// 清理表单
const cleanupForm = ref({
  beforeTime: '',
  quickSelect: '30days'
})

// 分页
const pagination = ref({
  page: 1,
  size: 20
})

// 时间快捷选择
const timeShortcuts = [
  {
    text: '最近1小时',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000)
      return [start, end]
    }
  },
  {
    text: '最近24小时',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24)
      return [start, end]
    }
  },
  {
    text: '最近7天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    }
  }
]

// 格式化JSON
const formatJson = (data) => {
  try {
    if (typeof data === 'string') {
      return JSON.stringify(JSON.parse(data), null, 2)
    }
    return JSON.stringify(data, null, 2)
  } catch (error) {
    return data
  }
}

// 获取HTTP方法标签类型
const getMethodType = (method) => {
  const typeMap = {
    'GET': 'success',
    'POST': 'primary',
    'PUT': 'warning',
    'DELETE': 'danger',
    'PATCH': 'info'
  }
  return typeMap[method] || 'info'
}

// 获取状态码标签类型
const getStatusType = (statusCode) => {
  if (statusCode >= 200 && statusCode < 300) return 'success'
  if (statusCode >= 300 && statusCode < 400) return 'warning'
  if (statusCode >= 400) return 'danger'
  return 'info'
}

// 获取响应时间样式类
const getResponseTimeClass = (responseTime) => {
  if (responseTime < 100) return 'response-fast'
  if (responseTime < 500) return 'response-normal'
  if (responseTime < 1000) return 'response-slow'
  return 'response-very-slow'
}

// 获取Mock类型标签
const getMockTypeTag = (mockType) => {
  const typeMap = {
    'STATIC': 'success',
    'DYNAMIC': 'warning',
    'TEMPLATE': 'info',
    'CACHED': 'primary',
    'NOT_FOUND': 'danger',
    'NO_RULE': 'danger',
    'ERROR': 'danger'
  }
  return typeMap[mockType] || 'info'
}

// 获取User Agent摘要
const getUserAgentSummary = (userAgent) => {
  if (!userAgent) return '未知'

  // 简化显示User Agent
  if (userAgent.includes('Chrome')) return 'Chrome'
  if (userAgent.includes('Firefox')) return 'Firefox'
  if (userAgent.includes('Safari') && !userAgent.includes('Chrome')) return 'Safari'
  if (userAgent.includes('Edge')) return 'Edge'
  if (userAgent.includes('curl')) return 'curl'
  if (userAgent.includes('Postman')) return 'Postman'

  return userAgent.length > 20 ? userAgent.substring(0, 20) + '...' : userAgent
}

// 初始化时间范围
const initTimeRange = () => {
  const end = new Date()
  const start = new Date()
  start.setTime(start.getTime() - 3600 * 1000 * 24) // 最近24小时
  filterForm.value.timeRange = [start, end]
}

// 加载日志数据
const loadLogs = async () => {
  const params = {
    page: pagination.value.page - 1,
    size: pagination.value.size
  }

  // 添加过滤条件
  if (props.projectId) {
    params.projectId = props.projectId
  }

  if (props.apiId) {
    params.apiId = props.apiId
  }

  if (filterForm.value.apiId) {
    params.apiId = filterForm.value.apiId
  }

  if (filterForm.value.statusCode) {
    params.statusCode = parseInt(filterForm.value.statusCode)
  }

  if (filterForm.value.clientIp) {
    params.clientIp = filterForm.value.clientIp
  }

  if (filterForm.value.timeRange && filterForm.value.timeRange.length === 2) {
    params.startTime = new Date(filterForm.value.timeRange[0])
    params.endTime = new Date(filterForm.value.timeRange[1])
  }

  try {
    await mockDataStore.fetchCallLogs(params)
  } catch (error) {
    ElMessage.error('加载日志失败: ' + error.message)
  }
}

// 刷新日志
const refreshLogs = () => {
  loadLogs()
}

// 搜索
const handleSearch = () => {
  pagination.value.page = 1
  loadLogs()
}

// 重置搜索
const handleReset = () => {
  filterForm.value = {
    timeRange: [],
    apiId: '',
    statusCode: '',
    clientIp: ''
  }
  initTimeRange()
  pagination.value.page = 1
  loadLogs()
}

// 查看详情
const handleViewDetail = (log) => {
  selectedLog.value = log
  detailVisible.value = true
}

// 行点击
const handleRowClick = (row) => {
  handleViewDetail(row)
}

// 处理分页
const handleSizeChange = (newSize) => {
  pagination.value.size = newSize
  pagination.value.page = 1
  loadLogs()
}

const handleCurrentChange = (newPage) => {
  pagination.value.page = newPage
  loadLogs()
}

// 清理日志
const handleCleanupLogs = () => {
  cleanupDialogVisible.value = true
  handleQuickSelect('30days') // 默认选择30天前
}

// 快速选择清理时间
const handleQuickSelect = (value) => {
  const now = new Date()
  let beforeTime

  switch (value) {
    case '7days':
      beforeTime = new Date(now.getTime() - 7 * 24 * 3600 * 1000)
      break
    case '30days':
      beforeTime = new Date(now.getTime() - 30 * 24 * 3600 * 1000)
      break
    case '90days':
      beforeTime = new Date(now.getTime() - 90 * 24 * 3600 * 1000)
      break
    case 'custom':
      return
  }

  if (beforeTime) {
    cleanupForm.value.beforeTime = beforeTime.toISOString().slice(0, 19).replace('T', ' ')
  }
}

// 确认清理
const confirmCleanup = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除 ${cleanupForm.value.beforeTime} 之前的所有日志吗？此操作不可恢复。`,
      '确认清理',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const deletedCount = await mockDataStore.cleanupExpiredLogs(new Date(cleanupForm.value.beforeTime))
    ElMessage.success(`成功清理了 ${deletedCount} 条日志记录`)

    cleanupDialogVisible.value = false
    loadLogs() // 重新加载日志
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清理日志失败: ' + error.message)
    }
  }
}

// 导出日志
const exportLogs = () => {
  ElMessage.info('导出功能开发中...')
}

onMounted(() => {
  initTimeRange()
  loadLogs()
})
</script>

<style scoped>
.mock-call-logs {
  padding: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.filter-section {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.user-agent {
  font-family: monospace;
  font-size: 12px;
}

.response-fast {
  color: #67c23a;
  font-weight: bold;
}

.response-normal {
  color: #409eff;
}

.response-slow {
  color: #e6a23c;
}

.response-very-slow {
  color: #f56c6c;
  font-weight: bold;
}

.log-detail {
  padding: 20px 0;
}

.detail-section {
  margin-top: 24px;
}

.detail-section h4 {
  margin: 0 0 12px 0;
  color: #303133;
}

.cleanup-form {
  padding: 20px 0;
}
</style>