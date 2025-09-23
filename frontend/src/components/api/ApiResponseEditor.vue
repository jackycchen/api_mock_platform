<template>
  <div class="api-response-editor">
    <!-- 响应 Schema 编辑 -->
    <div class="response-section">
      <h5 class="section-title">
        响应数据结构 (JSON Schema)
      </h5>
      <json-schema-editor
        v-model="responseSchema"
        placeholder="定义响应数据的 JSON Schema..."
        @update:model-value="handleSchemaChange"
      />
    </div>

    <!-- 响应示例编辑 -->
    <div class="response-section">
      <div class="section-header">
        <h5 class="section-title">
          响应示例
        </h5>
        <el-button
          type="primary"
          size="small"
          @click="addExample"
        >
          <el-icon><Plus /></el-icon>
          添加示例
        </el-button>
      </div>

      <div
        v-if="responseExamples.length > 0"
        class="examples-container"
      >
        <el-card
          v-for="(example, index) in responseExamples"
          :key="index"
          class="example-card"
          shadow="hover"
        >
          <template #header>
            <div class="example-header">
              <div class="example-meta">
                <el-input
                  v-model="example.name"
                  placeholder="示例名称"
                  size="small"
                  style="width: 200px"
                  @input="handleExamplesChange"
                />
                <el-input-number
                  v-model="example.statusCode"
                  placeholder="状态码"
                  :min="100"
                  :max="599"
                  size="small"
                  style="width: 120px"
                  @change="handleExamplesChange"
                />
              </div>
              <el-button
                type="danger"
                size="small"
                text
                @click="removeExample(index)"
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </template>

          <div class="example-content">
            <el-form-item label="描述">
              <el-input
                v-model="example.description"
                placeholder="请输入示例描述"
                size="small"
                @input="handleExamplesChange"
              />
            </el-form-item>

            <el-form-item label="响应头">
              <el-input
                v-model="example.headers"
                type="textarea"
                :rows="3"
                placeholder="例如：{&quot;Content-Type&quot;: &quot;application/json&quot;}"
                @input="handleExamplesChange"
              />
            </el-form-item>

            <el-form-item label="响应体">
              <div class="response-body-editor">
                <div class="editor-toolbar">
                  <el-button
                    size="small"
                    @click="formatExampleBody(index)"
                  >
                    <el-icon><Document /></el-icon>
                    格式化
                  </el-button>
                  <el-button
                    size="small"
                    @click="validateExampleBody(index)"
                  >
                    <el-icon><Check /></el-icon>
                    验证
                  </el-button>
                </div>
                <el-input
                  v-model="example.body"
                  type="textarea"
                  :rows="8"
                  placeholder="请输入响应体 JSON 数据..."
                  class="json-input"
                  @input="handleExamplesChange"
                />
              </div>
            </el-form-item>
          </div>
        </el-card>
      </div>

      <div
        v-else
        class="empty-examples"
      >
        <el-empty
          description="点击上方按钮添加响应示例"
          :image-size="60"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Delete, Document, Check } from '@element-plus/icons-vue'
import JsonSchemaEditor from './JsonSchemaEditor.vue'

const props = defineProps({
  schema: {
    type: String,
    default: ''
  },
  examples: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:schema', 'update:examples'])

const responseSchema = ref('')
const responseExamples = ref([])

// 初始化数据
const initData = () => {
  responseSchema.value = props.schema || ''
  responseExamples.value = props.examples.length > 0
    ? [...props.examples]
    : []
}

// 添加示例
const addExample = () => {
  responseExamples.value.push({
    name: `示例 ${responseExamples.value.length + 1}`,
    description: '',
    statusCode: 200,
    headers: '{}',
    body: `{
  "code": 200,
  "message": "success",
  "data": {}
}`
  })
  handleExamplesChange()
}

// 删除示例
const removeExample = (index) => {
  responseExamples.value.splice(index, 1)
  handleExamplesChange()
}

// 格式化示例响应体
const formatExampleBody = (index) => {
  const example = responseExamples.value[index]
  if (!example.body.trim()) {
    ElMessage.warning('请先输入响应体内容')
    return
  }

  try {
    const parsed = JSON.parse(example.body)
    example.body = JSON.stringify(parsed, null, 2)
    handleExamplesChange()
    ElMessage.success('格式化成功')
  } catch (error) {
    ElMessage.error('格式化失败：JSON 格式不正确')
  }
}

// 验证示例响应体
const validateExampleBody = (index) => {
  const example = responseExamples.value[index]
  if (!example.body.trim()) {
    ElMessage.warning('请先输入响应体内容')
    return
  }

  try {
    JSON.parse(example.body)
    ElMessage.success('JSON 格式正确')
  } catch (error) {
    ElMessage.error('JSON 格式错误：' + error.message)
  }
}

// 处理 Schema 变更
const handleSchemaChange = (newSchema) => {
  emit('update:schema', newSchema)
}

// 处理示例变更
const handleExamplesChange = () => {
  // 清理和验证示例数据
  const cleanedExamples = responseExamples.value.map(example => ({
    ...example,
    statusCode: example.statusCode || 200,
    headers: example.headers || '{}',
    body: example.body || '{}'
  }))

  emit('update:examples', cleanedExamples)
}

// 监听外部数据变化
watch(() => [props.schema, props.examples], () => {
  initData()
}, { immediate: true, deep: true })
</script>

<style scoped>
.api-response-editor {
  width: 100%;
}

.response-section {
  margin-bottom: 24px;
}

.response-section:last-child {
  margin-bottom: 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 14px;
  font-weight: 600;
}

.examples-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.example-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}

.example-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.example-meta {
  display: flex;
  gap: 12px;
  align-items: center;
}

.example-content {
  padding: 0;
}

.response-body-editor {
  width: 100%;
}

.editor-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.json-input {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

.json-input :deep(.el-textarea__inner) {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  line-height: 1.5;
}

.empty-examples {
  padding: 40px 0;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .section-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .example-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .example-meta {
    width: 100%;
    flex-direction: column;
    gap: 8px;
  }

  .example-meta .el-input,
  .example-meta .el-input-number {
    width: 100% !important;
  }
}

/* 表单样式 */
:deep(.el-form-item) {
  margin-bottom: 16px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
  font-size: 13px;
}

/* 卡片样式 */
:deep(.el-card__header) {
  padding: 16px 20px;
  background: #fafafa;
  border-bottom: 1px solid #e4e7ed;
}

:deep(.el-card__body) {
  padding: 20px;
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