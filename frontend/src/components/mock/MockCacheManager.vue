<template>
  <div class="mock-cache-manager">
    <el-card>
      <template #header>
        <div class="header-content">
          <h3>Mock缓存管理</h3>
          <div class="header-actions">
            <el-button
              :loading="loading.cacheStats"
              @click="refreshCacheStats"
            >
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button
              v-permission="['api:write']"
              type="warning"
              :disabled="selectedApis.length === 0"
              @click="handleBatchClearCache"
            >
              <el-icon><Delete /></el-icon>
              批量清除缓存 ({{ selectedApis.length }})
            </el-button>
          </div>
        </div>
      </template>

      <!-- 缓存统计概览 -->
      <div class="cache-overview">
        <div class="stat-grid">
          <div class="stat-item">
            <div class="stat-icon data-icon">
              <el-icon><DataBoard /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-title">
                数据缓存
              </div>
              <div class="stat-value">
                {{ cacheStats.dataKeyCount }}
              </div>
            </div>
          </div>

          <div class="stat-item">
            <div class="stat-icon rule-icon">
              <el-icon><Setting /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-title">
                规则缓存
              </div>
              <div class="stat-value">
                {{ cacheStats.ruleKeyCount }}
              </div>
            </div>
          </div>

          <div class="stat-item">
            <div class="stat-icon count-icon">
              <el-icon><DataAnalysis /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-title">
                计数缓存
              </div>
              <div class="stat-value">
                {{ cacheStats.callCountKeyCount }}
              </div>
            </div>
          </div>

          <div class="stat-item">
            <div class="stat-icon total-icon">
              <el-icon><PieChart /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-title">
                总缓存数
              </div>
              <div class="stat-value">
                {{ totalCacheKeys }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- API缓存详情表格 -->
      <div class="cache-table">
        <el-table
          :data="apiCacheList"
          :loading="loadingApiCache"
          row-key="apiId"
          @selection-change="handleSelectionChange"
        >
          <el-table-column
            type="selection"
            width="55"
          />

          <el-table-column
            prop="apiId"
            label="API ID"
            width="80"
          />

          <el-table-column
            prop="apiName"
            label="API名称"
            show-overflow-tooltip
          />

          <el-table-column
            prop="path"
            label="路径"
            show-overflow-tooltip
          />

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
            prop="callCount"
            label="调用次数"
            width="100"
          >
            <template #default="scope">
              <span v-if="scope.row.callCount !== undefined">
                {{ scope.row.callCount.toLocaleString() }}
              </span>
              <el-button
                v-else
                type="text"
                size="small"
                :loading="scope.row.loadingCount"
                @click="loadApiCallCount(scope.row)"
              >
                加载
              </el-button>
            </template>
          </el-table-column>

          <el-table-column
            prop="lastCallTime"
            label="最后调用"
            width="180"
          >
            <template #default="scope">
              <span v-if="scope.row.lastCallTime !== undefined">
                <span v-if="scope.row.lastCallTime">
                  {{ formatTime(scope.row.lastCallTime) }}
                </span>
                <span
                  v-else
                  class="no-call"
                >未调用</span>
              </span>
              <el-button
                v-else
                type="text"
                size="small"
                :loading="scope.row.loadingTime"
                @click="loadApiLastCallTime(scope.row)"
              >
                加载
              </el-button>
            </template>
          </el-table-column>

          <el-table-column
            prop="cacheStatus"
            label="缓存状态"
            width="120"
          >
            <template #default="scope">
              <el-tag
                v-if="scope.row.hasCachedData"
                type="success"
              >
                已缓存
              </el-tag>
              <el-tag
                v-else
                type="info"
              >
                未缓存
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column
            label="操作"
            width="200"
            fixed="right"
          >
            <template #default="scope">
              <el-button
                type="primary"
                size="small"
                :disabled="!scope.row.hasCachedData"
                @click="handleViewCache(scope.row)"
              >
                查看缓存
              </el-button>

              <el-button
                v-permission="['api:write']"
                type="warning"
                size="small"
                :disabled="!scope.row.hasCachedData"
                @click="handleClearSingleCache(scope.row)"
              >
                清除缓存
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
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>

    <!-- 缓存内容查看对话框 -->
    <el-dialog
      v-model="cacheViewVisible"
      title="缓存内容"
      width="70%"
      destroy-on-close
    >
      <div
        v-if="selectedApi"
        class="cache-content"
      >
        <el-descriptions
          border
          :column="2"
          class="cache-info"
        >
          <el-descriptions-item label="API ID">
            {{ selectedApi.apiId }}
          </el-descriptions-item>
          <el-descriptions-item label="API名称">
            {{ selectedApi.apiName }}
          </el-descriptions-item>
          <el-descriptions-item label="请求路径">
            {{ selectedApi.path }}
          </el-descriptions-item>
          <el-descriptions-item label="请求方法">
            <el-tag :type="getMethodType(selectedApi.method)">
              {{ selectedApi.method }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <div
          v-if="cachedData"
          class="cached-data"
        >
          <h4>缓存数据</h4>
          <el-input
            type="textarea"
            :model-value="formatJson(cachedData)"
            :rows="15"
            readonly
          />
        </div>

        <el-alert
          v-else-if="loadingCacheData"
          title="正在加载缓存数据..."
          type="info"
          :closable="false"
        />

        <el-alert
          v-else
          title="未找到缓存数据"
          type="warning"
          :closable="false"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Refresh,
  Delete,
  DataBoard,
  Setting,
  DataAnalysis,
  PieChart
} from '@element-plus/icons-vue'
import { useMockDataStore } from '@/stores/mockData'
import { storeToRefs } from 'pinia'
import { formatTime } from '@/utils/date'
import * as mockDataApi from '@/api/mockData'

const props = defineProps({
  projectId: {
    type: [String, Number],
    required: true
  },
  apiList: {
    type: Array,
    default: () => []
  }
})

const mockDataStore = useMockDataStore()
const {
  cacheStats,
  loading,
  totalCacheKeys
} = storeToRefs(mockDataStore)

// 本地状态
const loadingApiCache = ref(false)
const loadingCacheData = ref(false)
const cacheViewVisible = ref(false)
const selectedApi = ref(null)
const cachedData = ref(null)
const selectedApis = ref([])

// API缓存列表
const apiCacheList = ref([])

// 分页
const pagination = ref({
  page: 1,
  size: 20,
  total: 0
})

// 计算属性
const paginatedApiList = computed(() => {
  const start = (pagination.value.page - 1) * pagination.value.size
  const end = start + pagination.value.size
  return apiCacheList.value.slice(start, end)
})

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

// 初始化API缓存列表
const initApiCacheList = () => {
  apiCacheList.value = props.apiList.map(api => ({
    ...api,
    callCount: undefined,
    lastCallTime: undefined,
    hasCachedData: false,
    loadingCount: false,
    loadingTime: false
  }))

  pagination.value.total = apiCacheList.value.length

  // 检查每个API的缓存状态
  checkCacheStatus()
}

// 检查缓存状态
const checkCacheStatus = async () => {
  for (const api of apiCacheList.value) {
    try {
      const response = await mockDataApi.getCachedMockData(api.id)
      api.hasCachedData = !!response.data
    } catch (error) {
      api.hasCachedData = false
    }
  }
}

// 加载API调用次数
const loadApiCallCount = async (api) => {
  api.loadingCount = true
  try {
    api.callCount = await mockDataStore.fetchApiCallCount(api.id)
  } catch (error) {
    ElMessage.error('加载调用次数失败')
  } finally {
    api.loadingCount = false
  }
}

// 加载API最后调用时间
const loadApiLastCallTime = async (api) => {
  api.loadingTime = true
  try {
    api.lastCallTime = await mockDataStore.fetchApiLastCallTime(api.id)
  } catch (error) {
    ElMessage.error('加载最后调用时间失败')
  } finally {
    api.loadingTime = false
  }
}

// 刷新缓存统计
const refreshCacheStats = async () => {
  try {
    await mockDataStore.fetchCacheStats()
    await checkCacheStatus()
  } catch (error) {
    ElMessage.error('刷新缓存统计失败: ' + error.message)
  }
}

// 查看缓存内容
const handleViewCache = async (api) => {
  selectedApi.value = api
  cacheViewVisible.value = true
  loadingCacheData.value = true
  cachedData.value = null

  try {
    const response = await mockDataApi.getCachedMockData(api.id)
    cachedData.value = response.data
  } catch (error) {
    ElMessage.error('获取缓存数据失败: ' + error.message)
  } finally {
    loadingCacheData.value = false
  }
}

// 清除单个缓存
const handleClearSingleCache = async (api) => {
  try {
    await ElMessageBox.confirm(
      `确定要清除 ${api.name} 的缓存吗？`,
      '确认清除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await mockDataStore.clearMockCache(api.id)
    api.hasCachedData = false
    api.callCount = undefined
    api.lastCallTime = undefined

    ElMessage.success('缓存清除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清除缓存失败: ' + error.message)
    }
  }
}

// 批量清除缓存
const handleBatchClearCache = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要清除选中的 ${selectedApis.value.length} 个API的缓存吗？`,
      '确认批量清除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const apiIds = selectedApis.value.map(api => api.id)
    await mockDataStore.batchClearMockCache(apiIds)

    // 更新列表状态
    selectedApis.value.forEach(api => {
      const found = apiCacheList.value.find(item => item.id === api.id)
      if (found) {
        found.hasCachedData = false
        found.callCount = undefined
        found.lastCallTime = undefined
      }
    })

    selectedApis.value = []
    ElMessage.success('批量清除缓存成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量清除缓存失败: ' + error.message)
    }
  }
}

// 处理选择变化
const handleSelectionChange = (selection) => {
  selectedApis.value = selection
}

// 处理分页大小变化
const handleSizeChange = (newSize) => {
  pagination.value.size = newSize
  pagination.value.page = 1
}

// 处理当前页变化
const handleCurrentChange = (newPage) => {
  pagination.value.page = newPage
}

onMounted(() => {
  initApiCacheList()
  refreshCacheStats()
})
</script>

<style scoped>
.mock-cache-manager {
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

.cache-overview {
  margin-bottom: 24px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  padding: 20px;
  background: white;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 20px;
  color: white;
}

.data-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.rule-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.count-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.total-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-content {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.cache-table {
  margin-top: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.no-call {
  color: #909399;
  font-style: italic;
}

.cache-content {
  padding: 20px 0;
}

.cache-info {
  margin-bottom: 20px;
}

.cached-data h4 {
  margin: 0 0 16px 0;
  color: #303133;
}
</style>