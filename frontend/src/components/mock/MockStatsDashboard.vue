<template>
  <div class="mock-statistics-dashboard">
    <el-card class="stats-header">
      <template #header>
        <div class="header-content">
          <h3>Mock数据统计分析</h3>
          <div class="time-range-picker">
            <el-date-picker
              v-model="timeRange"
              type="datetimerange"
              :shortcuts="timeShortcuts"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              @change="handleTimeRangeChange"
            />
            <el-button
              type="primary"
              :loading="loading"
              @click="refreshStats"
            >
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <!-- 概览统计卡片 -->
      <div class="stats-overview">
        <div
          v-if="props.type === 'api' && apiStats"
          class="stat-card"
        >
          <div class="stat-title">
            总调用次数
          </div>
          <div class="stat-value">
            {{ apiStats.totalCalls.toLocaleString() }}
          </div>
          <div class="stat-trend success">
            成功率: {{ apiSuccessRate }}%
          </div>
        </div>

        <div
          v-if="props.type === 'api' && apiStats"
          class="stat-card"
        >
          <div class="stat-title">
            平均响应时间
          </div>
          <div class="stat-value">
            {{ apiStats.avgResponseTime.toFixed(2) }}ms
          </div>
          <div class="stat-trend">
            最大: {{ apiStats.maxResponseTime }}ms
          </div>
        </div>

        <div
          v-if="props.type === 'project' && projectStats"
          class="stat-card"
        >
          <div class="stat-title">
            项目总调用
          </div>
          <div class="stat-value">
            {{ projectStats.totalCalls.toLocaleString() }}
          </div>
          <div class="stat-trend success">
            成功率: {{ projectSuccessRate }}%
          </div>
        </div>

        <div
          v-if="props.type === 'project' && projectStats"
          class="stat-card"
        >
          <div class="stat-title">
            活跃API数
          </div>
          <div class="stat-value">
            {{ projectStats.activeApis }}
          </div>
          <div class="stat-trend">
            总API数: {{ totalApis }}
          </div>
        </div>

        <div
          v-if="props.type === 'global' && globalStats"
          class="stat-card"
        >
          <div class="stat-title">
            全局总调用
          </div>
          <div class="stat-value">
            {{ globalStats.totalCalls.toLocaleString() }}
          </div>
          <div class="stat-trend">
            平均响应: {{ globalStats.avgResponseTime.toFixed(2) }}ms
          </div>
        </div>

        <div
          v-if="props.type === 'global' && globalStats"
          class="stat-card"
        >
          <div class="stat-title">
            活跃项目数
          </div>
          <div class="stat-value">
            {{ globalStats.activeProjects }}
          </div>
          <div class="stat-trend">
            活跃API: {{ globalStats.activeApis }}
          </div>
        </div>
      </div>
    </el-card>

    <!-- 图表区域 -->
    <div class="charts-container">
      <!-- 调用趋势图 -->
      <el-card class="chart-card">
        <template #header>
          <h4>调用趋势</h4>
        </template>
        <div
          ref="callTrendChart"
          class="chart"
          style="height: 300px;"
        />
      </el-card>

      <!-- 状态码分布 -->
      <el-card
        v-if="props.type === 'api'"
        class="chart-card"
      >
        <template #header>
          <h4>状态码分布</h4>
        </template>
        <div
          ref="statusChart"
          class="chart"
          style="height: 300px;"
        />
      </el-card>

      <!-- 响应时间分布 -->
      <el-card
        v-if="props.type === 'api'"
        class="chart-card"
      >
        <template #header>
          <h4>响应时间分布</h4>
        </template>
        <div
          ref="responseTimeChart"
          class="chart"
          style="height: 300px;"
        />
      </el-card>

      <!-- 热门API -->
      <el-card
        v-if="props.type === 'project'"
        class="chart-card"
      >
        <template #header>
          <h4>热门API Top 10</h4>
        </template>
        <div
          ref="topApisChart"
          class="chart"
          style="height: 300px;"
        />
      </el-card>
    </div>

    <!-- 最近调用记录 -->
    <el-card
      v-if="props.type === 'api' && apiStats?.recentCalls?.length"
      class="recent-calls"
    >
      <template #header>
        <h4>最近调用记录</h4>
      </template>
      <el-table
        :data="apiStats.recentCalls"
        size="small"
      >
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
        />
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
            {{ scope.row.responseTime }}ms
          </template>
        </el-table-column>
        <el-table-column
          prop="clientIp"
          label="客户端IP"
          width="120"
        />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { useMockDataStore } from '@/stores/mockData'
import { storeToRefs } from 'pinia'
import { formatTime } from '@/utils/date'

const props = defineProps({
  type: {
    type: String,
    required: true,
    validator: (value) => ['api', 'project', 'global'].includes(value)
  },
  id: {
    type: [String, Number],
    default: null
  },
  totalApis: {
    type: Number,
    default: 0
  }
})

const mockDataStore = useMockDataStore()
const {
  apiStats,
  projectStats,
  globalStats,
  responseTimeDistribution,
  loading,
  apiSuccessRate,
  projectSuccessRate
} = storeToRefs(mockDataStore)

// 时间范围
const timeRange = ref([])
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
  },
  {
    text: '最近30天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    }
  }
]

// 图表引用
const callTrendChart = ref(null)
const statusChart = ref(null)
const responseTimeChart = ref(null)
const topApisChart = ref(null)

// 图表实例
let callTrendChartInstance = null
let statusChartInstance = null
let responseTimeChartInstance = null
let topApisChartInstance = null

// 初始化默认时间范围（最近24小时）
const initTimeRange = () => {
  const end = new Date()
  const start = new Date()
  start.setTime(start.getTime() - 3600 * 1000 * 24)
  timeRange.value = [start, end]
}

// 处理时间范围变化
const handleTimeRangeChange = () => {
  if (timeRange.value && timeRange.value.length === 2) {
    loadStats()
  }
}

// 刷新统计
const refreshStats = () => {
  loadStats()
}

// 加载统计数据
const loadStats = async () => {
  if (!timeRange.value || timeRange.value.length !== 2) return

  const [startTime, endTime] = timeRange.value

  try {
    switch (props.type) {
      case 'api':
        if (props.id) {
          await mockDataStore.fetchApiStats(props.id, new Date(startTime), new Date(endTime))
          await mockDataStore.fetchResponseTimeDistribution(props.id, new Date(startTime), new Date(endTime))
        }
        break
      case 'project':
        if (props.id) {
          await mockDataStore.fetchProjectStats(props.id, new Date(startTime), new Date(endTime))
        }
        break
      case 'global':
        await mockDataStore.fetchGlobalStats(new Date(startTime), new Date(endTime))
        break
    }

    // 数据加载完成后更新图表
    await nextTick()
    updateCharts()
  } catch (error) {
    ElMessage.error('加载统计数据失败: ' + error.message)
  }
}

// 更新图表
const updateCharts = () => {
  updateCallTrendChart()
  if (props.type === 'api') {
    updateStatusChart()
    updateResponseTimeChart()
  }
  if (props.type === 'project') {
    updateTopApisChart()
  }
}

// 更新调用趋势图
const updateCallTrendChart = () => {
  if (!callTrendChartInstance) return

  let hourlyData = []
  let dailyData = []

  if (props.type === 'api' && apiStats.value) {
    hourlyData = apiStats.value.hourlyStats || []
  } else if (props.type === 'project' && projectStats.value) {
    dailyData = projectStats.value.dailyStats || []
  } else if (props.type === 'global' && globalStats.value) {
    hourlyData = globalStats.value.hourlyStats || []
  }

  const data = hourlyData.length > 0 ? hourlyData : dailyData
  const option = {
    title: {
      text: props.type === 'project' ? '每日调用趋势' : '每小时调用趋势'
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: data.map(item => item[0])
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      data: data.map(item => item[1]),
      type: 'line',
      smooth: true,
      areaStyle: {}
    }]
  }

  callTrendChartInstance.setOption(option)
}

// 更新状态码分布图
const updateStatusChart = () => {
  if (!statusChartInstance || !apiStats.value) return

  const statusData = apiStats.value.statusStats || []
  const option = {
    title: {
      text: '状态码分布'
    },
    tooltip: {
      trigger: 'item'
    },
    series: [{
      type: 'pie',
      radius: '50%',
      data: statusData.map(item => ({
        name: item[0],
        value: item[1]
      }))
    }]
  }

  statusChartInstance.setOption(option)
}

// 更新响应时间分布图
const updateResponseTimeChart = () => {
  if (!responseTimeChartInstance) return

  const distribution = responseTimeDistribution.value || {}
  const option = {
    title: {
      text: '响应时间分布'
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: Object.keys(distribution)
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      data: Object.values(distribution),
      type: 'bar'
    }]
  }

  responseTimeChartInstance.setOption(option)
}

// 更新热门API图
const updateTopApisChart = () => {
  if (!topApisChartInstance || !projectStats.value) return

  const topApis = projectStats.value.topApis || []
  const option = {
    title: {
      text: '热门API Top 10'
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'value'
    },
    yAxis: {
      type: 'category',
      data: topApis.map(item => `API ${item[0]}`)
    },
    series: [{
      data: topApis.map(item => item[1]),
      type: 'bar'
    }]
  }

  topApisChartInstance.setOption(option)
}

// 初始化图表
const initCharts = () => {
  callTrendChartInstance = echarts.init(callTrendChart.value)

  if (props.type === 'api') {
    statusChartInstance = echarts.init(statusChart.value)
    responseTimeChartInstance = echarts.init(responseTimeChart.value)
  }

  if (props.type === 'project') {
    topApisChartInstance = echarts.init(topApisChart.value)
  }

  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
}

// 处理窗口大小变化
const handleResize = () => {
  callTrendChartInstance?.resize()
  statusChartInstance?.resize()
  responseTimeChartInstance?.resize()
  topApisChartInstance?.resize()
}

// 获取状态码类型
const getStatusType = (statusCode) => {
  if (statusCode >= 200 && statusCode < 300) return 'success'
  if (statusCode >= 300 && statusCode < 400) return 'warning'
  if (statusCode >= 400) return 'danger'
  return 'info'
}

onMounted(async () => {
  initTimeRange()
  await nextTick()
  initCharts()
  await loadStats()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  callTrendChartInstance?.dispose()
  statusChartInstance?.dispose()
  responseTimeChartInstance?.dispose()
  topApisChartInstance?.dispose()
})

// 监听统计数据变化
watch([apiStats, projectStats, globalStats, responseTimeDistribution], () => {
  nextTick(() => {
    updateCharts()
  })
}, { deep: true })
</script>

<style scoped>
.mock-statistics-dashboard {
  padding: 20px;
}

.stats-header .header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.time-range-picker {
  display: flex;
  gap: 12px;
  align-items: center;
}

.stats-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.stat-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
}

.stat-title {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 8px;
}

.stat-trend {
  font-size: 12px;
  opacity: 0.8;
}

.stat-trend.success {
  color: #a8f5a8;
}

.charts-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.chart-card {
  min-height: 350px;
}

.recent-calls {
  margin-top: 20px;
}

.chart {
  width: 100%;
}
</style>