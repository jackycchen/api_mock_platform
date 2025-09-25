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

      <el-card class="detail-section">
        <template #header>
          <div class="section-header">
            <h4>版本历史</h4>
            <el-button
              type="primary"
              text
              size="small"
              :loading="versionsLoading"
              @click="loadVersions"
            >
              刷新
            </el-button>
          </div>
        </template>
        <el-table
          :data="versionList"
          v-loading="versionsLoading"
          size="small"
          border
          empty-text="暂无版本记录"
        >
          <el-table-column
            prop="versionNumber"
            label="版本号"
            width="100"
          />
          <el-table-column
            prop="changeSummary"
            label="变更说明"
            min-width="220"
            show-overflow-tooltip
          />
          <el-table-column
            prop="createdBy"
            label="操作人"
            width="120"
          />
          <el-table-column
            label="创建时间"
            width="180"
          >
            <template #default="scope">
              {{ formatDate(scope.row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column
            label="操作"
            width="180"
          >
            <template #default="scope">
              <el-button
                type="primary"
                text
                size="small"
                @click="handleViewVersion(scope.row)"
              >
                查看
              </el-button>
              <el-button
                type="warning"
                text
                size="small"
                @click="handleRestoreVersion(scope.row)"
              >
                回滚
              </el-button>
            </template>
          </el-table-column>
        </el-table>
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
  <el-dialog
    v-model="versionDetailVisible"
    title="版本详情"
    width="720px"
    destroy-on-close
  >
    <div v-loading="versionDetailLoading">
      <el-descriptions
        v-if="versionDetail"
        :column="2"
        border
      >
        <el-descriptions-item label="版本号">
          {{ versionDetail.versionNumber }}
        </el-descriptions-item>
        <el-descriptions-item label="变更说明">
          {{ versionDetail.changeSummary || '未填写' }}
        </el-descriptions-item>
        <el-descriptions-item label="请求方法">
          <el-tag :type="getMethodTagType(versionDetail.method)" size="small">
            {{ versionDetail.method }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="接口路径" :span="2">
          <code class="api-path">{{ versionDetail.path }}</code>
        </el-descriptions-item>
        <el-descriptions-item label="接口名称">
          {{ versionDetail.name }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="versionDetail.status === 1 ? 'success' : 'danger'" size="small">
            {{ versionDetail.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="所属分组">
          <span v-if="versionDetail.groupName">{{ versionDetail.groupName }}</span>
          <span v-else class="no-group">未分组</span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDate(versionDetail.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item label="操作人">
          {{ versionDetail.createdBy || '系统' }}
        </el-descriptions-item>
      </el-descriptions>

      <el-card
        v-if="versionDetail && parseArray(versionDetail.requestHeaders).length > 0"
        class="detail-section"
      >
        <template #header>
          <h4>请求头</h4>
        </template>
        <el-table
          :data="parseArray(versionDetail.requestHeaders)"
          size="small"
          border
        >
          <el-table-column prop="name" label="参数名" width="150" />
          <el-table-column prop="description" label="描述" />
          <el-table-column prop="required" label="必填" width="80">
            <template #default="scope">
              <el-tag :type="scope.row.required ? 'danger' : 'info'" size="small">
                {{ scope.row.required ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="example" label="示例值" width="150" />
        </el-table>
      </el-card>

      <el-card
        v-if="versionDetail && parseArray(versionDetail.pathParameters).length > 0"
        class="detail-section"
      >
        <template #header>
          <h4>路径参数</h4>
        </template>
        <el-table
          :data="parseArray(versionDetail.pathParameters)"
          size="small"
          border
        >
          <el-table-column prop="name" label="参数名" width="150" />
          <el-table-column prop="type" label="类型" width="120" />
          <el-table-column prop="description" label="描述" />
          <el-table-column prop="required" label="必填" width="80">
            <template #default="scope">
              <el-tag :type="scope.row.required ? 'danger' : 'info'" size="small">
                {{ scope.row.required ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="example" label="示例值" width="150" />
        </el-table>
      </el-card>

      <el-card
        v-if="versionDetail && parseArray(versionDetail.queryParameters).length > 0"
        class="detail-section"
      >
        <template #header>
          <h4>查询参数</h4>
        </template>
        <el-table
          :data="parseArray(versionDetail.queryParameters)"
          size="small"
          border
        >
          <el-table-column prop="name" label="参数名" width="150" />
          <el-table-column prop="type" label="类型" width="120" />
          <el-table-column prop="description" label="描述" />
          <el-table-column prop="required" label="必填" width="80">
            <template #default="scope">
              <el-tag :type="scope.row.required ? 'danger' : 'info'" size="small">
                {{ scope.row.required ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="example" label="示例值" width="150" />
        </el-table>
      </el-card>

      <el-card
        v-if="versionDetail?.requestBodySchema"
        class="detail-section"
      >
        <template #header>
          <h4>请求体 Schema</h4>
        </template>
        <div class="schema-viewer">
          <pre><code>{{ formatJsonSchema(versionDetail.requestBodySchema) }}</code></pre>
        </div>
      </el-card>

      <el-card
        v-if="versionDetail?.responseSchema"
        class="detail-section"
      >
        <template #header>
          <h4>响应体 Schema</h4>
        </template>
        <div class="schema-viewer">
          <pre><code>{{ formatJsonSchema(versionDetail.responseSchema) }}</code></pre>
        </div>
      </el-card>

      <el-card
        v-if="versionDetail && parseArray(versionDetail.responseExamples).length > 0"
        class="detail-section"
      >
        <template #header>
          <h4>响应示例</h4>
        </template>
        <el-tabs v-model="versionExampleTab" type="border-card">
          <el-tab-pane
            v-for="(example, index) in parseArray(versionDetail.responseExamples)"
            :key="index"
            :label="example.name || `示例${index + 1}`"
            :name="index.toString()"
          >
            <div class="example-content">
              <div class="example-meta">
                <el-tag size="small" type="success">
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
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDate } from '@/utils/format'
import { getApiVersions, getApiVersionDetail, restoreApiVersion } from '@/api/api'

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

const emit = defineEmits(['update:modelValue', 'edit', 'refresh'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const activeExample = ref('0')
const versionExampleTab = ref('0')

const versionsLoading = ref(false)
const versionList = ref([])
const versionDetailVisible = ref(false)
const versionDetailLoading = ref(false)
const versionDetail = ref(null)

const parseArray = (value) => {
  if (!value) return []
  try {
    return JSON.parse(value)
  } catch (error) {
    return []
  }
}

const requestHeaders = computed(() => parseArray(props.apiData?.requestHeaders))
const pathParameters = computed(() => parseArray(props.apiData?.pathParameters))
const queryParameters = computed(() => parseArray(props.apiData?.queryParameters))
const responseExamples = computed(() => parseArray(props.apiData?.responseExamples))

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
  } catch (error) {
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
  } catch (error) {
    return data
  }
}

const loadVersions = async () => {
  if (!props.apiData?.id) {
    versionList.value = []
    return
  }
  versionsLoading.value = true
  try {
    const response = await getApiVersions(props.apiData.id)
    versionList.value = response.data || []
  } catch (error) {
    ElMessage.error('获取API版本列表失败: ' + (error.message || '未知错误'))
  } finally {
    versionsLoading.value = false
  }
}

const handleViewVersion = async (versionRow) => {
  if (!props.apiData?.id) return
  versionDetailVisible.value = true
  versionDetailLoading.value = true
  versionExampleTab.value = '0'
  try {
    const response = await getApiVersionDetail(props.apiData.id, versionRow.versionNumber)
    versionDetail.value = response.data
  } catch (error) {
    versionDetailVisible.value = false
    ElMessage.error('获取版本详情失败: ' + (error.message || '未知错误'))
  } finally {
    versionDetailLoading.value = false
  }
}

const handleRestoreVersion = async (versionRow) => {
  if (!props.apiData?.id) return
  let summary = ''
  try {
    const { value } = await ElMessageBox.prompt(
      '请输入本次回滚说明（可选）',
      `回滚到版本 ${versionRow.versionNumber}`,
      {
        confirmButtonText: '回滚',
        cancelButtonText: '取消',
        inputPlaceholder: '记录这次回滚的原因',
        inputValue: ''
      }
    )
    summary = value ?? ''
  } catch (action) {
    return
  }

  try {
    await restoreApiVersion(props.apiData.id, versionRow.versionNumber, summary ? { changeSummary: summary } : {})
    ElMessage.success('回滚API版本成功')
    await loadVersions()
    emit('refresh', props.apiData.id)
  } catch (error) {
    ElMessage.error('回滚API版本失败: ' + (error.message || '未知错误'))
  }
}

const handleClose = () => {
  visible.value = false
}

const handleEdit = () => {
  emit('edit', props.apiData)
  handleClose()
}

watch(visible, (newValue) => {
  if (newValue) {
    activeExample.value = '0'
    versionExampleTab.value = '0'
    loadVersions()
  } else {
    versionList.value = []
    versionDetailVisible.value = false
  }
})

watch(
  () => props.apiData?.id,
  (newId, oldId) => {
    if (visible.value && newId && newId !== oldId) {
      loadVersions()
    }
  }
)
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

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
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
