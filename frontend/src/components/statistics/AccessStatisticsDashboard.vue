<template>
  <div class="statistics-dashboard">
    <!-- 时间选择器和控制面板 -->
    <div class="dashboard-controls">
      <el-card class="controls-card">
        <div class="controls-row">
          <div class="time-range-selector">
            <el-date-picker
              v-model="timeRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="handleTimeRangeChange"
            />
          </div>
          <div class="granularity-selector">
            <el-radio-group v-model="timeGranularity" @change="loadStatistics">
              <el-radio-button label="hour">小时</el-radio-button>
              <el-radio-button label="day">天</el-radio-button>
            </el-radio-group>
          </div>
          <div class="refresh-button">
            <el-button type="primary" @click="loadStatistics" :loading="loading">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 统计概览卡片 -->
    <div class="overview-cards">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-card class="overview-card">
            <div class="card-content">
              <div class="card-icon total">
                <el-icon><DataLine /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-title">总调用次数</div>
                <div class="card-value">{{ formatNumber(statistics.totalCalls || 0) }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="overview-card">
            <div class="card-content">
              <div class="card-icon success">
                <el-icon><SuccessFilled /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-title">成功次数</div>
                <div class="card-value">{{ formatNumber(statistics.successCalls || 0) }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="overview-card">
            <div class="card-content">
              <div class="card-icon rate">
                <el-icon><TrendCharts /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-title">成功率</div>
                <div class="card-value">{{ formatPercent(statistics.successRate || 0) }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="overview-card">
            <div class="card-content">
              <div class="card-icon time">
                <el-icon><Timer /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-title">平均响应时间</div>
                <div class="card-value">{{ formatResponseTime(statistics.averageResponseTime || 0) }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 图表区域 -->
    <div class="charts-container">
      <el-row :gutter="16">
        <!-- 调用趋势图 -->
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>调用趋势</span>
              </div>
            </template>
            <div ref="callTrendChart" class="chart-container"></div>
          </el-card>
        </el-col>

        <!-- 响应时间分布 -->
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>响应时间分布</span>
              </div>
            </template>
            <div ref="responseTimeChart" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="16" style="margin-top: 16px;">
        <!-- 状态码分布 -->
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>状态码分布</span>
              </div>
            </template>
            <div ref="statusCodeChart" class="chart-container"></div>
          </el-card>
        </el-col>

        <!-- 客户端IP Top 10 -->
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>客户端IP Top 10</span>
              </div>
            </template>
            <div ref="clientIpChart" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Refresh,
  DataLine,
  SuccessFilled,
  TrendCharts,
  Timer
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getApiStatistics, getProjectStatistics } from '@/api/statistics'

const props = defineProps({
  projectId: {
    type: Number,
    required: true
  },
  apiId: {
    type: Number,
    default: null
  }
})

const loading = ref(false)
const timeRange = ref([])
const timeGranularity = ref('day')
const statistics = reactive({
  totalCalls: 0,
  successCalls: 0,
  errorCalls: 0,
  successRate: 0,
  averageResponseTime: 0,
  maxResponseTime: 0,
  minResponseTime: 0,
  timeSeriesData: [],
  statusCodeDistribution: {},
  responseTimeDistribution: {},
  clientIpDistribution: []
})

// 图表实例
let callTrendChart = null
let responseTimeChart = null
let statusCodeChart = null
let clientIpChart = null

// 初始化时间范围（最近7天）
const initTimeRange = () => {
  const endDate = new Date()
  const startDate = new Date()
  startDate.setDate(startDate.getDate() - 7)

  timeRange.value = [
    startDate.toISOString().split('T')[0],
    endDate.toISOString().split('T')[0]
  ]
}

// 格式化数字
const formatNumber = (num) => {
  if (num >= 1000000) {
    return (num / 1000000).toFixed(1) + 'M'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'K'
  }
  return num.toString()
}

// 格式化百分比
const formatPercent = (percent) => {
  return percent.toFixed(1) + '%'
}

// 格式化响应时间
const formatResponseTime = (time) => {
  if (time >= 1000) {
    return (time / 1000).toFixed(2) + 's'
  }
  return Math.round(time) + 'ms'
}

// 时间范围变化处理
const handleTimeRangeChange = (value) => {
  if (value && value.length === 2) {
    loadStatistics()
  }
}

// 加载统计数据
const loadStatistics = async () => {
  if (!timeRange.value || timeRange.value.length !== 2) {
    ElMessage.warning('请选择时间范围')
    return
  }

  loading.value = true
  try {
    const params = {
      startTime: timeRange.value[0],
      endTime: timeRange.value[1],
      timeGranularity: timeGranularity.value
    }

    let response
    if (props.apiId) {
      response = await getApiStatistics({ apiId: props.apiId, ...params })
    } else {
      response = await getProjectStatistics({ projectId: props.projectId, ...params })
    }

    if (response.data) {
      Object.assign(statistics, response.data)
      await nextTick()
      updateCharts()
    }
  } catch (error) {
    ElMessage.error('加载统计数据失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 初始化图表
const initCharts = () => {
  // 调用趋势图
  callTrendChart = echarts.init(document.querySelector('[ref="callTrendChart"]'))

  // 响应时间分布图
  responseTimeChart = echarts.init(document.querySelector('[ref="responseTimeChart"]'))

  // 状态码分布图
  statusCodeChart = echarts.init(document.querySelector('[ref="statusCodeChart"]'))

  // 客户端IP图
  clientIpChart = echarts.init(document.querySelector('[ref="clientIpChart"]'))

  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    callTrendChart?.resize()
    responseTimeChart?.resize()
    statusCodeChart?.resize()
    clientIpChart?.resize()
  })
}

// 更新图表
const updateCharts = () => {
  updateCallTrendChart()
  updateResponseTimeChart()
  updateStatusCodeChart()
  updateClientIpChart()
}

// 更新调用趋势图
const updateCallTrendChart = () => {
  if (!callTrendChart) return

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      data: ['调用次数']
    },
    xAxis: {
      type: 'category',
      data: statistics.timeSeriesData.map(item => item.time)
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      name: '调用次数',
      type: 'line',
      data: statistics.timeSeriesData.map(item => item.count),
      smooth: true,
      itemStyle: { color: '#409EFF' }
    }]
  }

  callTrendChart.setOption(option)
}

// 更新响应时间分布图
const updateResponseTimeChart = () => {
  if (!responseTimeChart) return

  const data = Object.entries(statistics.responseTimeDistribution || {})

  const option = {
    tooltip: {
      trigger: 'item'
    },
    series: [{
      name: '响应时间分布',
      type: 'pie',
      radius: ['40%', '70%'],
      data: data.map(([range, count]) => ({
        value: count,
        name: range
      })),
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      }
    }]
  }

  responseTimeChart.setOption(option)
}

// 更新状态码分布图
const updateStatusCodeChart = () => {
  if (!statusCodeChart) return

  const data = Object.entries(statistics.statusCodeDistribution || {})

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    xAxis: {
      type: 'category',
      data: data.map(([code]) => code)
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      name: '次数',
      type: 'bar',
      data: data.map(([, count]) => count),
      itemStyle: { color: '#67C23A' }
    }]
  }

  statusCodeChart.setOption(option)
}

// 更新客户端IP图
const updateClientIpChart = () => {
  if (!clientIpChart) return

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    xAxis: {
      type: 'value'
    },
    yAxis: {
      type: 'category',
      data: statistics.clientIpDistribution.map(item => item.clientIp)
    },
    series: [{
      name: '调用次数',
      type: 'bar',
      data: statistics.clientIpDistribution.map(item => item.count),
      itemStyle: { color: '#E6A23C' }
    }]
  }

  clientIpChart.setOption(option)
}

onMounted(async () => {
  initTimeRange()
  await nextTick()
  initCharts()
  loadStatistics()
})

onUnmounted(() => {
  callTrendChart?.dispose()
  responseTimeChart?.dispose()
  statusCodeChart?.dispose()
  clientIpChart?.dispose()
  window.removeEventListener('resize', () => {})
})
</script>

<style scoped>
.statistics-dashboard {
  padding: 20px;
}

.dashboard-controls {
  margin-bottom: 20px;
}

.controls-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.controls-row {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.time-range-selector {
  flex: 1;
  min-width: 300px;
}

.overview-cards {
  margin-bottom: 20px;
}

.overview-card {
  height: 120px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
  color: white;
}

.card-icon.total {
  background: linear-gradient(135deg, #409EFF, #66b1ff);
}

.card-icon.success {
  background: linear-gradient(135deg, #67C23A, #85ce61);
}

.card-icon.rate {
  background: linear-gradient(135deg, #E6A23C, #ebb563);
}

.card-icon.time {
  background: linear-gradient(135deg, #F56C6C, #f78989);
}

.card-info {
  flex: 1;
}

.card-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.charts-container {
  margin-top: 20px;
}

.chart-container {
  height: 300px;
  width: 100%;
}

.card-header {
  font-weight: 500;
  color: #303133;
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-date-editor.el-input) {
  width: 100%;
}
</style>