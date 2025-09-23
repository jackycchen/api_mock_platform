<template>
  <div class="documentation-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <h3>API接口文档</h3>
            <p class="subtitle">
              标准OpenAPI 3.0规范文档
            </p>
          </div>
          <div class="header-right">
            <el-button
              :loading="refreshing"
              @click="refreshDoc"
            >
              <el-icon><Refresh /></el-icon>
              刷新文档
            </el-button>
            <el-dropdown
              trigger="click"
              @command="handleExport"
            >
              <el-button type="primary">
                <el-icon><Download /></el-icon>
                导出文档<el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="html">
                    <el-icon><Document /></el-icon>
                    导出HTML
                  </el-dropdown-item>
                  <el-dropdown-item command="pdf">
                    <el-icon><DocumentCopy /></el-icon>
                    导出PDF
                  </el-dropdown-item>
                  <el-dropdown-item
                    command="json"
                    divided
                  >
                    <el-icon><Files /></el-icon>
                    下载OpenAPI JSON
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </template>

      <!-- 文档工具栏 -->
      <div class="doc-toolbar">
        <el-tabs
          v-model="activeTab"
          @tab-change="handleTabChange"
        >
          <el-tab-pane
            label="Swagger UI"
            name="swagger"
          >
            <template #label>
              <el-icon><Monitor /></el-icon>
              Swagger UI
            </template>
          </el-tab-pane>
          <el-tab-pane
            label="API测试"
            name="test"
          >
            <template #label>
              <el-icon><Connection /></el-icon>
              API测试
            </template>
          </el-tab-pane>
          <el-tab-pane
            label="原始JSON"
            name="json"
          >
            <template #label>
              <el-icon><Files /></el-icon>
              原始JSON
            </template>
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- Swagger UI 内容 -->
      <div
        v-if="activeTab === 'swagger'"
        class="swagger-container"
      >
        <div
          v-loading="loading"
          class="swagger-iframe-container"
        >
          <iframe
            ref="swaggerFrame"
            :src="swaggerUrl"
            class="swagger-iframe"
            @load="handleIframeLoad"
          />
        </div>
      </div>

      <!-- API测试内容 -->
      <div
        v-else-if="activeTab === 'test'"
        class="api-test-container"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card
              shadow="never"
              class="test-form-card"
            >
              <template #header>
                <h4>API测试配置</h4>
              </template>

              <el-form
                :model="testForm"
                label-width="100px"
                @submit.prevent="executeTest"
              >
                <el-form-item label="服务器地址">
                  <el-input
                    v-model="testForm.baseUrl"
                    placeholder="留空使用Mock服务器"
                    clearable
                  >
                    <template #prepend>
                      <el-select
                        v-model="testForm.serverType"
                        style="width: 120px"
                        @change="handleServerTypeChange"
                      >
                        <el-option
                          label="Mock服务器"
                          value="mock"
                        />
                        <el-option
                          label="开发服务器"
                          value="dev"
                        />
                        <el-option
                          label="自定义"
                          value="custom"
                        />
                      </el-select>
                    </template>
                  </el-input>
                </el-form-item>

                <el-form-item label="请求方法">
                  <el-select
                    v-model="testForm.method"
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
                  </el-select>
                </el-form-item>

                <el-form-item label="接口路径">
                  <el-input
                    v-model="testForm.path"
                    placeholder="/api/example"
                    clearable
                  />
                </el-form-item>

                <el-form-item label="查询参数">
                  <el-input
                    v-model="testForm.queryParams"
                    placeholder="id=123&name=test"
                    type="textarea"
                    :rows="2"
                    clearable
                  />
                </el-form-item>

                <el-form-item label="请求头">
                  <el-input
                    v-model="testForm.headers"
                    placeholder="{&quot;Content-Type&quot;: &quot;application/json&quot;}"
                    type="textarea"
                    :rows="3"
                    clearable
                  />
                </el-form-item>

                <el-form-item
                  v-if="['POST', 'PUT', 'PATCH'].includes(testForm.method)"
                  label="请求体"
                >
                  <el-input
                    v-model="testForm.requestBody"
                    placeholder="{&quot;key&quot;: &quot;value&quot;}"
                    type="textarea"
                    :rows="6"
                    clearable
                  />
                </el-form-item>

                <el-form-item>
                  <el-button
                    type="primary"
                    :loading="testing"
                    @click="executeTest"
                  >
                    <el-icon><Connection /></el-icon>
                    发送请求
                  </el-button>
                  <el-button @click="clearTestForm">
                    <el-icon><RefreshLeft /></el-icon>
                    清空
                  </el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-col>

          <el-col :span="12">
            <el-card
              shadow="never"
              class="test-result-card"
            >
              <template #header>
                <h4>响应结果</h4>
              </template>

              <div
                v-if="testResult"
                class="test-result"
              >
                <!-- 响应状态 -->
                <div class="result-status">
                  <el-tag
                    :type="getStatusTagType(testResult.statusCode)"
                    size="large"
                  >
                    {{ testResult.statusCode }} {{ getStatusText(testResult.statusCode) }}
                  </el-tag>
                  <span class="response-time">{{ testResult.responseTime }}ms</span>
                </div>

                <!-- 错误信息 -->
                <div
                  v-if="testResult.error"
                  class="error-message"
                >
                  <el-alert
                    :title="testResult.error"
                    type="error"
                    show-icon
                    :closable="false"
                  />
                </div>

                <!-- 响应头 -->
                <div class="result-section">
                  <h5>响应头</h5>
                  <el-input
                    :value="testResult.responseHeaders"
                    type="textarea"
                    readonly
                    :rows="4"
                    class="result-textarea"
                  />
                </div>

                <!-- 响应体 -->
                <div class="result-section">
                  <h5>响应体</h5>
                  <el-input
                    :value="formatResponseBody(testResult.responseBody)"
                    type="textarea"
                    readonly
                    :rows="12"
                    class="result-textarea"
                  />
                </div>
              </div>

              <div
                v-else
                class="no-result"
              >
                <el-empty description="暂无测试结果" />
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- JSON 原始内容 -->
      <div
        v-else-if="activeTab === 'json'"
        class="json-container"
      >
        <div class="json-toolbar">
          <el-button
            size="small"
            @click="copyJsonToClipboard"
          >
            <el-icon><DocumentCopy /></el-icon>
            复制JSON
          </el-button>
          <el-button
            size="small"
            @click="formatJson"
          >
            <el-icon><Operation /></el-icon>
            格式化
          </el-button>
        </div>
        <el-input
          v-model="openApiJson"
          type="textarea"
          readonly
          :rows="25"
          class="json-textarea"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getOpenApiDocument,
  exportHtmlDocument,
  exportPdfDocument,
  refreshDocumentation,
  testApiEndpoint
} from '@/api/documentation'

// Icons
import {
  Refresh,
  Download,
  ArrowDown,
  Document,
  DocumentCopy,
  Files,
  Monitor,
  Connection,
  Operation,
  RefreshLeft
} from '@element-plus/icons-vue'

// 路由参数
const route = useRoute()
const projectId = computed(() => parseInt(route.params.projectId))

// 状态管理
const loading = ref(false)
const refreshing = ref(false)
const testing = ref(false)
const activeTab = ref('swagger')
const openApiJson = ref('')

// Swagger UI
const swaggerFrame = ref(null)
const swaggerUrl = computed(() => `/api/v1/documentation/${projectId.value}/swagger-ui`)

// API测试表单
const testForm = ref({
  serverType: 'mock',
  baseUrl: '',
  method: 'GET',
  path: '',
  queryParams: '',
  headers: '{"Content-Type": "application/json"}',
  requestBody: ''
})

const testResult = ref(null)

// 方法定义
const loadOpenApiDocument = async () => {
  loading.value = true
  try {
    const response = await getOpenApiDocument(projectId.value)
    if (typeof response.data === 'string') {
      openApiJson.value = response.data
    } else {
      openApiJson.value = JSON.stringify(response.data, null, 2)
    }
  } catch (error) {
    ElMessage.error('加载API文档失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const handleTabChange = (tabName) => {
  if (tabName === 'json' && !openApiJson.value) {
    loadOpenApiDocument()
  }
}

const refreshDoc = async () => {
  refreshing.value = true
  try {
    await refreshDocumentation(projectId.value)
    ElMessage.success('文档刷新成功')

    // 重新加载内容
    if (activeTab.value === 'json') {
      await loadOpenApiDocument()
    } else if (activeTab.value === 'swagger' && swaggerFrame.value) {
      swaggerFrame.value.src = swaggerFrame.value.src // 强制刷新iframe
    }
  } catch (error) {
    ElMessage.error('刷新文档失败：' + error.message)
  } finally {
    refreshing.value = false
  }
}

const handleExport = async (command) => {
  try {
    let response
    let filename
    let mimeType

    switch (command) {
      case 'html':
        response = await exportHtmlDocument(projectId.value)
        filename = `api_doc_project_${projectId.value}.html`
        mimeType = 'text/html'
        break
      case 'pdf':
        response = await exportPdfDocument(projectId.value)
        filename = `api_doc_project_${projectId.value}.pdf`
        mimeType = 'application/pdf'
        break
      case 'json':
        if (!openApiJson.value) {
          await loadOpenApiDocument()
        }
        const blob = new Blob([openApiJson.value], { type: 'application/json' })
        downloadBlob(blob, `openapi_project_${projectId.value}.json`)
        ElMessage.success('OpenAPI JSON下载成功')
        return
    }

    if (response.data) {
      downloadBlob(response.data, filename)
      ElMessage.success('文档导出成功')
    }
  } catch (error) {
    ElMessage.error('导出失败：' + error.message)
  }
}

const downloadBlob = (blob, filename) => {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

const handleServerTypeChange = (type) => {
  switch (type) {
    case 'mock':
      testForm.value.baseUrl = ''
      break
    case 'dev':
      testForm.value.baseUrl = 'http://localhost:8080/api/v1'
      break
    case 'custom':
      testForm.value.baseUrl = 'https://your-api-server.com'
      break
  }
}

const executeTest = async () => {
  testing.value = true
  testResult.value = null

  try {
    const response = await testApiEndpoint(projectId.value, testForm.value)
    testResult.value = response.data

    if (testResult.value.success) {
      ElMessage.success('API测试执行成功')
    } else {
      ElMessage.warning('API测试返回错误响应')
    }
  } catch (error) {
    ElMessage.error('API测试失败：' + error.message)
  } finally {
    testing.value = false
  }
}

const clearTestForm = () => {
  testForm.value = {
    serverType: 'mock',
    baseUrl: '',
    method: 'GET',
    path: '',
    queryParams: '',
    headers: '{"Content-Type": "application/json"}',
    requestBody: ''
  }
  testResult.value = null
}

const getStatusTagType = (statusCode) => {
  if (statusCode >= 200 && statusCode < 300) return 'success'
  if (statusCode >= 300 && statusCode < 400) return 'warning'
  if (statusCode >= 400 && statusCode < 500) return 'danger'
  if (statusCode >= 500) return 'danger'
  return 'info'
}

const getStatusText = (statusCode) => {
  const statusTexts = {
    200: 'OK',
    201: 'Created',
    400: 'Bad Request',
    401: 'Unauthorized',
    403: 'Forbidden',
    404: 'Not Found',
    500: 'Internal Server Error'
  }
  return statusTexts[statusCode] || ''
}

const formatResponseBody = (body) => {
  if (!body) return ''

  try {
    const parsed = JSON.parse(body)
    return JSON.stringify(parsed, null, 2)
  } catch {
    return body
  }
}

const copyJsonToClipboard = async () => {
  try {
    await navigator.clipboard.writeText(openApiJson.value)
    ElMessage.success('JSON已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

const formatJson = () => {
  try {
    const parsed = JSON.parse(openApiJson.value)
    openApiJson.value = JSON.stringify(parsed, null, 2)
    ElMessage.success('JSON格式化成功')
  } catch (error) {
    ElMessage.error('JSON格式化失败：内容不是有效的JSON')
  }
}

const handleIframeLoad = () => {
  loading.value = false
}

// 生命周期
onMounted(() => {
  // 默认不加载JSON，只有在切换到对应tab时才加载
})
</script>

<style scoped>
.documentation-page {
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

.doc-toolbar {
  margin-bottom: 20px;
}

.swagger-container {
  height: 800px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
}

.swagger-iframe-container {
  height: 100%;
  position: relative;
}

.swagger-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.api-test-container {
  margin-top: 20px;
}

.test-form-card,
.test-result-card {
  height: 100%;
}

.test-result {
  height: 100%;
}

.result-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
}

.response-time {
  color: #666;
  font-size: 14px;
  font-weight: 500;
}

.error-message {
  margin-bottom: 16px;
}

.result-section {
  margin-bottom: 16px;
}

.result-section h5 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 14px;
  font-weight: 600;
}

.result-textarea {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
}

.no-result {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.json-container {
  margin-top: 20px;
}

.json-toolbar {
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
}

.json-textarea {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .documentation-page {
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

  .swagger-container {
    height: 600px;
  }

  .api-test-container :deep(.el-row) {
    --el-row-gutter: 0;
  }

  .api-test-container :deep(.el-col) {
    margin-bottom: 20px;
  }
}

:deep(.el-tabs__item) {
  display: flex;
  align-items: center;
  gap: 6px;
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-textarea__inner) {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}
</style>