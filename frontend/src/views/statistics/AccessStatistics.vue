<template>
  <div class="access-statistics-page">
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item>
          <router-link to="/projects">项目列表</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link :to="`/projects/${projectId}`">{{ projectName || '项目详情' }}</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>访问统计</el-breadcrumb-item>
      </el-breadcrumb>
      <h1>访问统计</h1>
      <p class="page-description">查看接口调用情况、成功率、响应时间等统计数据</p>
    </div>

    <div class="statistics-content">
      <!-- API选择器 -->
      <el-card class="api-selector-card">
        <div class="selector-content">
          <div class="selector-label">
            <el-icon><Menu /></el-icon>
            <span>选择API接口:</span>
          </div>
          <div class="selector-input">
            <el-select
              v-model="selectedApiId"
              placeholder="请选择API接口（不选择则显示项目整体统计）"
              clearable
              filterable
              @change="handleApiChange"
            >
              <el-option
                v-for="api in apiList"
                :key="api.id"
                :label="`${api.method} ${api.path}`"
                :value="api.id"
              >
                <div class="api-option">
                  <el-tag :type="getMethodTagType(api.method)" size="small">
                    {{ api.method }}
                  </el-tag>
                  <span class="api-path">{{ api.path }}</span>
                </div>
              </el-option>
            </el-select>
          </div>
          <div class="selector-actions">
            <el-button type="primary" @click="exportReport" :loading="exportLoading">
              <el-icon><Download /></el-icon>
              导出报告
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 统计仪表板 -->
      <AccessStatisticsDashboard
        :project-id="projectId"
        :api-id="selectedApiId"
        :key="dashboardKey"
      />

      <!-- 最近调用日志 -->
      <el-card v-if="selectedApiId" class="recent-logs-card">
        <template #header>
          <div class="card-header">
            <span>最近调用日志</span>
            <el-button type="text" @click="loadRecentLogs">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </template>
        <div class="recent-logs">
          <el-table
            :data="recentLogs"
            :loading="logsLoading"
            empty-text="暂无调用记录"
          >
            <el-table-column label="时间" width="180">
              <template #default="scope">
                {{ formatDateTime(scope.row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="请求方法" width="100">
              <template #default="scope">
                <el-tag :type="getMethodTagType(scope.row.requestMethod)" size="small">
                  {{ scope.row.requestMethod }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="请求路径" min-width="200">
              <template #default="scope">
                <code class="request-path">{{ scope.row.requestPath }}</code>
              </template>
            </el-table-column>
            <el-table-column label="状态码" width="100">
              <template #default="scope">
                <el-tag
                  :type="getStatusTagType(scope.row.responseStatus)"
                  size="small"
                >
                  {{ scope.row.responseStatus }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="响应时间" width="120">
              <template #default="scope">
                <span :class="getResponseTimeClass(scope.row.responseTime)">
                  {{ formatResponseTime(scope.row.responseTime) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="客户端IP" width="140">
              <template #default="scope">
                <span class="client-ip">{{ scope.row.clientIp || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="scope">
                <el-button type="text" @click="viewLogDetails(scope.row)">
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
    </div>

    <!-- 日志详情对话框 -->
    <el-dialog
      v-model="logDetailVisible"
      title="调用日志详情"
      width="800px"
      top="5vh"
    >
      <div v-if="selectedLog" class="log-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="调用时间">
            {{ formatDateTime(selectedLog.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="响应时间">
            <span :class="getResponseTimeClass(selectedLog.responseTime)">
              {{ formatResponseTime(selectedLog.responseTime) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="请求方法">
            <el-tag :type="getMethodTagType(selectedLog.requestMethod)" size="small">
              {{ selectedLog.requestMethod }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态码">
            <el-tag :type="getStatusTagType(selectedLog.responseStatus)" size="small">
              {{ selectedLog.responseStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="请求路径" span="2">
            <code>{{ selectedLog.requestPath }}</code>
          </el-descriptions-item>
          <el-descriptions-item label="客户端IP">
            {{ selectedLog.clientIp || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="User-Agent" span="2">
            {{ selectedLog.userAgent || '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="log-content-section">
          <el-tabs type="border-card">
            <el-tab-pane label="请求头">
              <pre class="log-content">{{ formatJson(selectedLog.requestHeaders) }}</pre>
            </el-tab-pane>
            <el-tab-pane label="请求参数">
              <pre class="log-content">{{ formatJson(selectedLog.requestParams) }}</pre>
            </el-tab-pane>
            <el-tab-pane label="请求体">
              <pre class="log-content">{{ formatJson(selectedLog.requestBody) }}</pre>
            </el-tab-pane>
            <el-tab-pane label="响应头">
              <pre class="log-content">{{ formatJson(selectedLog.responseHeaders) }}</pre>
            </el-tab-pane>
            <el-tab-pane label="响应体">
              <pre class="log-content">{{ formatJson(selectedLog.responseBody) }}</pre>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Menu,
  Download,
  Refresh
} from '@element-plus/icons-vue'
import AccessStatisticsDashboard from '@/components/statistics/AccessStatisticsDashboard.vue'
import { getApisByProject } from '@/api/api'
import { getRecentCallLogs } from '@/api/statistics'

const route = useRoute()
const projectId = ref(parseInt(route.params.id))
const projectName = ref('')
const selectedApiId = ref(null)
const dashboardKey = ref(0)

const apiList = ref([])
const recentLogs = ref([])
const logsLoading = ref(false)
const exportLoading = ref(false)

const logDetailVisible = ref(false)
const selectedLog = ref(null)

// 加载项目的API列表
const loadApiList = async () => {
  try {
    const response = await getApisByProject(projectId.value)
    apiList.value = response.data || []
  } catch (error) {
    ElMessage.error('加载API列表失败: ' + error.message)
  }
}

// 处理API选择变化
const handleApiChange = () => {
  dashboardKey.value++
  if (selectedApiId.value) {
    loadRecentLogs()
  } else {
    recentLogs.value = []
  }
}

// 加载最近调用日志
const loadRecentLogs = async () => {
  if (!selectedApiId.value) return

  logsLoading.value = true
  try {
    const response = await getRecentCallLogs(selectedApiId.value, 20)
    recentLogs.value = response.data || []
  } catch (error) {
    ElMessage.error('加载调用日志失败: ' + error.message)
  } finally {
    logsLoading.value = false
  }
}

// 导出报告
const exportReport = async () => {
  exportLoading.value = true
  try {
    // TODO: 实现导出功能
    ElMessage.success('导出功能正在开发中')
  } catch (error) {
    ElMessage.error('导出失败: ' + error.message)
  } finally {
    exportLoading.value = false
  }
}

// 查看日志详情
const viewLogDetails = (log) => {
  selectedLog.value = log
  logDetailVisible.value = true
}

// 工具方法
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

const formatResponseTime = (time) => {
  if (!time) return '-'
  return time >= 1000 ? `${(time / 1000).toFixed(2)}s` : `${time}ms`
}

const formatJson = (jsonStr) => {
  if (!jsonStr) return '无数据'
  try {
    return JSON.stringify(JSON.parse(jsonStr), null, 2)
  } catch {
    return jsonStr
  }
}

const getMethodTagType = (method) => {
  const typeMap = {
    GET: 'success',
    POST: 'primary',
    PUT: 'warning',
    DELETE: 'danger',
    PATCH: 'info'
  }
  return typeMap[method] || 'info'
}

const getStatusTagType = (status) => {
  if (status >= 200 && status < 300) return 'success'
  if (status >= 400 && status < 500) return 'warning'
  if (status >= 500) return 'danger'
  return 'info'
}

const getResponseTimeClass = (time) => {
  if (!time) return ''
  if (time < 100) return 'fast-response'
  if (time < 500) return 'normal-response'
  if (time < 1000) return 'slow-response'
  return 'very-slow-response'
}

onMounted(() => {
  loadApiList()
})
</script>

<style scoped>
.access-statistics-page {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 10px 0 5px 0;
  font-size: 28px;
  font-weight: 500;
  color: #303133;
}

.page-description {
  margin: 0 0 20px 0;
  color: #909399;
  font-size: 14px;
}

.api-selector-card {
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.selector-content {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.selector-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: #606266;
  white-space: nowrap;
}

.selector-input {
  flex: 1;
  min-width: 300px;
}

.api-option {
  display: flex;
  align-items: center;
  gap: 10px;
}

.api-path {
  font-family: 'Courier New', monospace;
  color: #606266;
}

.recent-logs-card {
  margin-top: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.request-path {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  background-color: #f6f8fa;
  padding: 2px 6px;
  border-radius: 4px;
}

.client-ip {
  font-family: 'Courier New', monospace;
  font-size: 12px;
}

.fast-response {
  color: #67C23A;
  font-weight: bold;
}

.normal-response {
  color: #409EFF;
}

.slow-response {
  color: #E6A23C;
  font-weight: bold;
}

.very-slow-response {
  color: #F56C6C;
  font-weight: bold;
}

.log-detail {
  max-height: 70vh;
  overflow-y: auto;
}

.log-content-section {
  margin-top: 20px;
}

.log-content {
  max-height: 300px;
  overflow-y: auto;
  background-color: #f6f8fa;
  padding: 12px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.4;
  white-space: pre-wrap;
  word-break: break-all;
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-descriptions__label) {
  font-weight: 500;
}
</style>