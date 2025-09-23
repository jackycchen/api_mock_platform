<template>
  <el-dialog
    v-model="visible"
    title="API接口详情"
    width="900px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div
      v-if="apiData"
      class="api-detail"
    >
      <!-- 基本信息 -->
      <el-card class="detail-section">
        <template #header>
          <h4>基本信息</h4>
        </template>
        <el-descriptions
          :column="2"
          border
        >
          <el-descriptions-item label="接口名称">
            {{ apiData.name }}
          </el-descriptions-item>
          <el-descriptions-item label="请求方法">
            <el-tag
              :type="getMethodTagType(apiData.method)"
              size="small"
            >
              {{ apiData.method }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item
            label="接口路径"
            :span="2"
          >
            <code class="api-path">{{ apiData.path }}</code>
          </el-descriptions-item>
          <el-descriptions-item
            label="接口描述"
            :span="2"
          >
            {{ apiData.description || '暂无描述' }}
          </el-descriptions-item>
          <el-descriptions-item label="所属分组">
            <el-tag
              v-if="apiData.group"
              size="small"
              type="info"
            >
              {{ apiData.group.name }}
            </el-tag>
            <span
              v-else
              class="no-group"
            >未分组</span>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag
              :type="apiData.status === 1 ? 'success' : 'danger'"
              size="small"
            >
              {{ apiData.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDate(apiData.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDate(apiData.updatedAt) }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 请求头配置 -->
      <el-card
        v-if="requestHeaders.length > 0"
        class="detail-section"
      >
        <template #header>
          <h4>请求头配置</h4>
        </template>
        <el-table
          :data="requestHeaders"
          border
          size="small"
        >
          <el-table-column
            prop="name"
            label="参数名"
            width="150"
          />
          <el-table-column
            prop="description"
            label="描述"
          />
          <el-table-column
            prop="required"
            label="必填"
            width="80"
          >
            <template #default="scope">
              <el-tag
                :type="scope.row.required ? 'danger' : 'info'"
                size="small"
              >
                {{ scope.row.required ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column
            prop="example"
            label="示例值"
            width="120"
          />
        </el-table>
      </el-card>

      <!-- 路径参数配置 -->
      <el-card
        v-if="pathParameters.length > 0"
        class="detail-section"
      >
        <template #header>
          <h4>路径参数配置</h4>
        </template>
        <el-table
          :data="pathParameters"
          border
          size="small"
        >
          <el-table-column
            prop="name"
            label="参数名"
            width="150"
          />
          <el-table-column
            prop="type"
            label="类型"
            width="100"
          />
          <el-table-column
            prop="description"
            label="描述"
          />
          <el-table-column
            prop="required"
            label="必填"
            width="80"
          >
            <template #default="scope">
              <el-tag
                :type="scope.row.required ? 'danger' : 'info'"
                size="small"
              >
                {{ scope.row.required ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column
            prop="example"
            label="示例值"
            width="120"
          />
        </el-table>
      </el-card>

      <!-- 查询参数配置 -->
      <el-card
        v-if="queryParameters.length > 0"
        class="detail-section"
      >
        <template #header>
          <h4>查询参数配置</h4>
        </template>
        <el-table
          :data="queryParameters"
          border
          size="small"
        >
          <el-table-column
            prop="name"
            label="参数名"
            width="150"
          />
          <el-table-column
            prop="type"
            label="类型"
            width="100"
          />
          <el-table-column
            prop="description"
            label="描述"
          />
          <el-table-column
            prop="required"
            label="必填"
            width="80"
          >
            <template #default="scope">
              <el-tag
                :type="scope.row.required ? 'danger' : 'info'"
                size="small"
              >
                {{ scope.row.required ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column
            prop="example"
            label="示例值"
            width="120"
          />
        </el-table>
      </el-card>

      <!-- 请求体配置 -->
      <el-card
        v-if="apiData.requestBodySchema"
        class="detail-section"
      >
        <template #header>
          <h4>请求体配置</h4>
        </template>
        <div class="schema-viewer">
          <pre><code>{{ formatJsonSchema(apiData.requestBodySchema) }}</code></pre>
        </div>
      </el-card>

      <!-- 响应配置 -->
      <el-card
        v-if="apiData.responseSchema"
        class="detail-section"
      >
        <template #header>
          <h4>响应配置</h4>
        </template>
        <div class="schema-viewer">
          <pre><code>{{ formatJsonSchema(apiData.responseSchema) }}</code></pre>
        </div>
      </el-card>

      <!-- 响应示例 -->
      <el-card
        v-if="responseExamples.length > 0"
        class="detail-section"
      >
        <template #header>
          <h4>响应示例</h4>
        </template>
        <el-tabs
          v-model="activeExample"
          type="border-card"
        >
          <el-tab-pane
            v-for="(example, index) in responseExamples"
            :key="index"
            :label="example.name || `示例${index + 1}`"
            :name="index.toString()"
          >
            <div class="example-content">
              <div class="example-meta">
                <el-tag
                  size="small"
                  type="success"
                >
                  {{ example.statusCode || 200 }}
                </el-tag>
                <span class="example-description">{{ example.description }}</span>
              </div>
              <div class="example-body">
                <pre><code>{{ formatJson(example.body) }}</code></pre>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">
          关闭
        </el-button>
        <el-button
          type="primary"
          @click="handleEdit"
        >
          编辑接口
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { formatDate } from '@/utils/format'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  apiData: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'edit'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const activeExample = ref('0')

// 解析JSON数据
const requestHeaders = computed(() => {
  if (!props.apiData?.requestHeaders) return []
  try {
    return JSON.parse(props.apiData.requestHeaders)
  } catch (e) {
    return []
  }
})

const pathParameters = computed(() => {
  if (!props.apiData?.pathParameters) return []
  try {
    return JSON.parse(props.apiData.pathParameters)
  } catch (e) {
    return []
  }
})

const queryParameters = computed(() => {
  if (!props.apiData?.queryParameters) return []
  try {
    return JSON.parse(props.apiData.queryParameters)
  } catch (e) {
    return []
  }
})

const responseExamples = computed(() => {
  if (!props.apiData?.responseExamples) return []
  try {
    return JSON.parse(props.apiData.responseExamples)
  } catch (e) {
    return []
  }
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

const formatJsonSchema = (schema) => {
  if (!schema) return ''
  try {
    return JSON.stringify(JSON.parse(schema), null, 2)
  } catch (e) {
    return schema
  }
}

const formatJson = (data) => {
  if (!data) return ''
  try {
    if (typeof data === 'string') {
      return JSON.stringify(JSON.parse(data), null, 2)
    }
    return JSON.stringify(data, null, 2)
  } catch (e) {
    return data
  }
}

const handleClose = () => {
  visible.value = false
}

const handleEdit = () => {
  emit('edit', props.apiData)
  handleClose()
}

// 监听对话框显示状态，重置示例选择
watch(visible, (newValue) => {
  if (newValue) {
    activeExample.value = '0'
  }
})
</script>

<style scoped>
.api-detail {
  max-height: 70vh;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.detail-section h4 {
  margin: 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.api-path {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  background: #f1f1f1;
  padding: 4px 8px;
  border-radius: 4px;
  color: #666;
}

.no-group {
  color: #c0c4cc;
  font-style: italic;
}

.schema-viewer {
  max-height: 300px;
  overflow: auto;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.schema-viewer pre {
  margin: 0;
  padding: 16px;
  background: #f8f9fa;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  line-height: 1.5;
  color: #333;
}

.schema-viewer code {
  background: none;
  padding: 0;
  color: inherit;
}

.example-content {
  padding: 0;
}

.example-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding: 0 16px;
}

.example-description {
  color: #606266;
  font-size: 14px;
}

.example-body {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}

.example-body pre {
  margin: 0;
  padding: 16px;
  background: #f8f9fa;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  line-height: 1.5;
  color: #333;
  max-height: 300px;
  overflow: auto;
}

.example-body code {
  background: none;
  padding: 0;
  color: inherit;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .example-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}

/* 表格样式优化 */
:deep(.el-table) {
  border-radius: 6px;
}

:deep(.el-table .el-table__header-wrapper) {
  border-radius: 6px 6px 0 0;
}

:deep(.el-descriptions__body) {
  background: #fafafa;
}

:deep(.el-descriptions__label) {
  font-weight: 500;
  color: #606266;
}

/* 标签页样式 */
:deep(.el-tabs--border-card) {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
}

:deep(.el-tabs__nav-wrap) {
  border-radius: 6px 6px 0 0;
}
</style>