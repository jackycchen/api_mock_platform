<template>
  <el-dialog
    v-model="visible"
    title="API接口统计"
    width="800px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div
      v-loading="loading"
      class="statistics-content"
    >
      <el-row :gutter="20">
        <!-- 总体统计 -->
        <el-col :span="24">
          <el-card class="stat-card">
            <template #header>
              <h4>总体统计</h4>
            </template>
            <el-row :gutter="20">
              <el-col :span="6">
                <div class="stat-item">
                  <div class="stat-value">
                    {{ statistics.total || 0 }}
                  </div>
                  <div class="stat-label">
                    接口总数
                  </div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-item">
                  <div class="stat-value">
                    {{ statistics.active || 0 }}
                  </div>
                  <div class="stat-label">
                    启用接口
                  </div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-item">
                  <div class="stat-value">
                    {{ statistics.inactive || 0 }}
                  </div>
                  <div class="stat-label">
                    禁用接口
                  </div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-item">
                  <div class="stat-value">
                    {{ statistics.groups || 0 }}
                  </div>
                  <div class="stat-label">
                    分组数量
                  </div>
                </div>
              </el-col>
            </el-row>
          </el-card>
        </el-col>
      </el-row>

      <el-row
        :gutter="20"
        style="margin-top: 20px;"
      >
        <!-- 请求方法分布 -->
        <el-col :span="12">
          <el-card class="stat-card">
            <template #header>
              <h4>请求方法分布</h4>
            </template>
            <div class="method-stats">
              <div
                v-for="method in methodStats"
                :key="method.name"
                class="method-item"
              >
                <div class="method-info">
                  <el-tag
                    :type="getMethodTagType(method.name)"
                    size="small"
                  >
                    {{ method.name }}
                  </el-tag>
                  <span class="method-count">{{ method.count }} 个</span>
                </div>
                <div class="method-progress">
                  <el-progress
                    :percentage="method.percentage"
                    :color="getMethodColor(method.name)"
                    :show-text="false"
                    :stroke-width="8"
                  />
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 分组统计 -->
        <el-col :span="12">
          <el-card class="stat-card">
            <template #header>
              <h4>分组统计</h4>
            </template>
            <div
              v-if="groupStats.length > 0"
              class="group-stats"
            >
              <div
                v-for="group in groupStats"
                :key="group.id"
                class="group-item"
              >
                <div class="group-info">
                  <span class="group-name">{{ group.name }}</span>
                  <span class="group-count">{{ group.count }} 个</span>
                </div>
                <div class="group-progress">
                  <el-progress
                    :percentage="group.percentage"
                    color="#409eff"
                    :show-text="false"
                    :stroke-width="6"
                  />
                </div>
              </div>
            </div>
            <div
              v-else
              class="no-data"
            >
              <el-empty
                description="暂无分组数据"
                :image-size="80"
              />
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row
        :gutter="20"
        style="margin-top: 20px;"
      >
        <!-- 最近创建的接口 -->
        <el-col :span="24">
          <el-card class="stat-card">
            <template #header>
              <h4>最近创建的接口</h4>
            </template>
            <div v-if="recentApis.length > 0">
              <el-table
                :data="recentApis"
                size="small"
              >
                <el-table-column
                  prop="name"
                  label="接口名称"
                  min-width="150"
                />
                <el-table-column
                  prop="method"
                  label="方法"
                  width="80"
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
                  label="路径"
                  min-width="200"
                >
                  <template #default="scope">
                    <code class="api-path">{{ scope.row.path }}</code>
                  </template>
                </el-table-column>
                <el-table-column
                  prop="status"
                  label="状态"
                  width="80"
                >
                  <template #default="scope">
                    <el-tag
                      :type="scope.row.status === 1 ? 'success' : 'danger'"
                      size="small"
                    >
                      {{ scope.row.status === 1 ? '启用' : '禁用' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column
                  prop="createdAt"
                  label="创建时间"
                  width="120"
                >
                  <template #default="scope">
                    {{ formatDate(scope.row.createdAt, 'MM-DD HH:mm') }}
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <div
              v-else
              class="no-data"
            >
              <el-empty
                description="暂无接口数据"
                :image-size="80"
              />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">
          关闭
        </el-button>
        <el-button
          type="primary"
          @click="refreshStatistics"
        >
          刷新数据
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getApiStatistics } from '@/api/api'
import { formatDate } from '@/utils/format'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  projectId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const loading = ref(false)
const statistics = ref({})

// 计算属性
const methodStats = computed(() => {
  const methods = statistics.value.methodStats || []
  const total = statistics.value.total || 1

  return methods.map(method => ({
    ...method,
    percentage: Math.round((method.count / total) * 100)
  }))
})

const groupStats = computed(() => {
  const groups = statistics.value.groupStats || []
  const total = statistics.value.total || 1

  return groups.map(group => ({
    ...group,
    percentage: Math.round((group.count / total) * 100)
  }))
})

const recentApis = computed(() => {
  return statistics.value.recentApis || []
})

// 方法定义
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

const getMethodColor = (method) => {
  const colors = {
    GET: '#67c23a',
    POST: '#409eff',
    PUT: '#e6a23c',
    DELETE: '#f56c6c',
    PATCH: '#909399',
    HEAD: '#909399',
    OPTIONS: '#909399'
  }
  return colors[method] || '#909399'
}

const loadStatistics = async () => {
  if (!props.projectId) return

  loading.value = true
  try {
    const response = await getApiStatistics({ projectId: props.projectId })
    statistics.value = response.data || {}
  } catch (error) {
    ElMessage.error('获取统计数据失败：' + error.message)
    statistics.value = {}
  } finally {
    loading.value = false
  }
}

const refreshStatistics = () => {
  loadStatistics()
}

const handleClose = () => {
  visible.value = false
}

// 监听对话框显示状态
watch(visible, (newValue) => {
  if (newValue) {
    loadStatistics()
  }
})
</script>

<style scoped>
.statistics-content {
  min-height: 400px;
}

.stat-card {
  height: 100%;
}

.stat-card h4 {
  margin: 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.stat-item {
  text-align: center;
  padding: 20px 0;
}

.stat-value {
  font-size: 32px;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 8px;
}

.stat-label {
  color: #606266;
  font-size: 14px;
}

.method-stats {
  padding: 0;
}

.method-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding: 8px 0;
}

.method-item:last-child {
  margin-bottom: 0;
}

.method-info {
  flex: 0 0 120px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.method-count {
  color: #606266;
  font-size: 13px;
}

.method-progress {
  flex: 1;
}

.group-stats {
  padding: 0;
}

.group-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  padding: 6px 0;
}

.group-item:last-child {
  margin-bottom: 0;
}

.group-info {
  flex: 0 0 140px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.group-name {
  color: #303133;
  font-size: 14px;
  font-weight: 500;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.group-count {
  color: #606266;
  font-size: 13px;
}

.group-progress {
  flex: 1;
}

.api-path {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  background: #f1f1f1;
  padding: 2px 6px;
  border-radius: 3px;
  color: #666;
}

.no-data {
  padding: 40px 0;
  text-align: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stat-item {
    padding: 15px 0;
  }

  .stat-value {
    font-size: 24px;
  }

  .method-info {
    flex: 0 0 100px;
  }

  .group-info {
    flex: 0 0 120px;
  }

  .group-name {
    max-width: 70px;
  }
}

/* 表格样式 */
:deep(.el-table) {
  border-radius: 6px;
}

:deep(.el-table .el-table__header-wrapper) {
  border-radius: 6px 6px 0 0;
}

/* 进度条样式 */
:deep(.el-progress-bar__outer) {
  border-radius: 10px;
}

:deep(.el-progress-bar__inner) {
  border-radius: 10px;
}

/* 空状态样式 */
:deep(.el-empty) {
  padding: 20px 0;
}

:deep(.el-empty__description) {
  color: #909399;
  font-size: 13px;
}
</style>