<template>
  <div class="json-schema-editor">
    <div class="editor-header">
      <span class="editor-title">JSON Schema 定义</span>
      <div class="editor-actions">
        <el-button
          size="small"
          @click="formatSchema"
        >
          <el-icon><Document /></el-icon>
          格式化
        </el-button>
        <el-button
          size="small"
          @click="validateSchema"
        >
          <el-icon><Check /></el-icon>
          验证
        </el-button>
        <el-button
          size="small"
          @click="showExamples"
        >
          <el-icon><QuestionFilled /></el-icon>
          示例
        </el-button>
      </div>
    </div>

    <div class="editor-content">
      <el-input
        v-model="schemaText"
        type="textarea"
        :rows="15"
        placeholder="请输入 JSON Schema 定义..."
        class="schema-input"
        @input="handleInput"
        @blur="handleBlur"
      />

      <div
        v-if="validationError"
        class="validation-error"
      >
        <el-alert
          :title="validationError"
          type="error"
          :closable="false"
          show-icon
        />
      </div>

      <div
        v-if="isValid && schemaText"
        class="validation-success"
      >
        <el-alert
          title="JSON Schema 格式正确"
          type="success"
          :closable="false"
          show-icon
        />
      </div>
    </div>

    <!-- 示例对话框 -->
    <el-dialog
      v-model="examplesVisible"
      title="JSON Schema 示例"
      width="700px"
      @close="examplesVisible = false"
    >
      <el-tabs
        v-model="activeExample"
        type="border-card"
      >
        <el-tab-pane
          label="用户对象"
          name="user"
        >
          <pre class="example-code">{{ userExample }}</pre>
        </el-tab-pane>
        <el-tab-pane
          label="分页响应"
          name="pagination"
        >
          <pre class="example-code">{{ paginationExample }}</pre>
        </el-tab-pane>
        <el-tab-pane
          label="错误响应"
          name="error"
        >
          <pre class="example-code">{{ errorExample }}</pre>
        </el-tab-pane>
        <el-tab-pane
          label="基础类型"
          name="basic"
        >
          <pre class="example-code">{{ basicExample }}</pre>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="examplesVisible = false">
          关闭
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, Check, QuestionFilled } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '请输入 JSON Schema 定义...'
  }
})

const emit = defineEmits(['update:modelValue'])

const schemaText = ref('')
const validationError = ref('')
const isValid = ref(false)
const examplesVisible = ref(false)
const activeExample = ref('user')

// 示例数据
const userExample = ref(`{
  "type": "object",
  "properties": {
    "id": {
      "type": "integer",
      "description": "用户ID"
    },
    "name": {
      "type": "string",
      "description": "用户名",
      "minLength": 2,
      "maxLength": 50
    },
    "email": {
      "type": "string",
      "format": "email",
      "description": "邮箱地址"
    },
    "age": {
      "type": "integer",
      "minimum": 0,
      "maximum": 150,
      "description": "年龄"
    },
    "isActive": {
      "type": "boolean",
      "description": "是否激活"
    },
    "tags": {
      "type": "array",
      "items": {
        "type": "string"
      },
      "description": "标签列表"
    }
  },
  "required": ["id", "name", "email"],
  "additionalProperties": false
}`)

const paginationExample = ref(`{
  "type": "object",
  "properties": {
    "data": {
      "type": "array",
      "items": {
        "type": "object"
      },
      "description": "数据列表"
    },
    "total": {
      "type": "integer",
      "description": "总数量"
    },
    "page": {
      "type": "integer",
      "description": "当前页码"
    },
    "size": {
      "type": "integer",
      "description": "每页大小"
    },
    "totalPages": {
      "type": "integer",
      "description": "总页数"
    }
  },
  "required": ["data", "total", "page", "size"],
  "additionalProperties": false
}`)

const errorExample = ref(`{
  "type": "object",
  "properties": {
    "code": {
      "type": "integer",
      "description": "错误代码"
    },
    "message": {
      "type": "string",
      "description": "错误信息"
    },
    "details": {
      "type": "object",
      "description": "错误详情",
      "additionalProperties": true
    },
    "timestamp": {
      "type": "string",
      "format": "date-time",
      "description": "时间戳"
    }
  },
  "required": ["code", "message"],
  "additionalProperties": false
}`)

const basicExample = ref(`{
  "oneOf": [
    {
      "type": "string",
      "description": "字符串类型"
    },
    {
      "type": "number",
      "description": "数字类型"
    },
    {
      "type": "boolean",
      "description": "布尔类型"
    },
    {
      "type": "array",
      "items": {
        "type": "string"
      },
      "description": "字符串数组"
    },
    {
      "type": "null",
      "description": "空值"
    }
  ]
}`)

// 初始化
const initSchema = () => {
  schemaText.value = props.modelValue || ''
  validateSchemaContent()
}

// 验证 JSON Schema
const validateSchemaContent = () => {
  if (!schemaText.value.trim()) {
    validationError.value = ''
    isValid.value = false
    return
  }

  try {
    const parsed = JSON.parse(schemaText.value)

    // 基本 JSON Schema 结构验证
    if (typeof parsed !== 'object' || parsed === null) {
      throw new Error('Schema 必须是一个对象')
    }

    // 检查是否包含 type 字段
    if (!parsed.type && !parsed.oneOf && !parsed.anyOf && !parsed.allOf) {
      throw new Error('Schema 必须包含 type 字段或组合关键字')
    }

    validationError.value = ''
    isValid.value = true
  } catch (error) {
    validationError.value = `JSON Schema 格式错误: ${error.message}`
    isValid.value = false
  }
}

// 格式化 Schema
const formatSchema = () => {
  if (!schemaText.value.trim()) {
    ElMessage.warning('请先输入 JSON Schema 内容')
    return
  }

  try {
    const parsed = JSON.parse(schemaText.value)
    schemaText.value = JSON.stringify(parsed, null, 2)
    handleInput()
    ElMessage.success('格式化成功')
  } catch (error) {
    ElMessage.error('格式化失败：JSON 格式不正确')
  }
}

// 验证 Schema
const validateSchema = () => {
  validateSchemaContent()
  if (isValid.value) {
    ElMessage.success('JSON Schema 验证通过')
  } else if (validationError.value) {
    ElMessage.error('JSON Schema 验证失败')
  } else {
    ElMessage.warning('请先输入 JSON Schema 内容')
  }
}

// 显示示例
const showExamples = () => {
  examplesVisible.value = true
}

// 处理输入
const handleInput = () => {
  emit('update:modelValue', schemaText.value)
}

// 处理失焦
const handleBlur = () => {
  validateSchemaContent()
}

// 监听外部数据变化
watch(() => props.modelValue, () => {
  if (props.modelValue !== schemaText.value) {
    initSchema()
  }
}, { immediate: true })
</script>

<style scoped>
.json-schema-editor {
  width: 100%;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.editor-title {
  font-weight: 500;
  color: #303133;
}

.editor-actions {
  display: flex;
  gap: 8px;
}

.editor-content {
  position: relative;
}

.schema-input {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

.schema-input :deep(.el-textarea__inner) {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  line-height: 1.5;
  resize: vertical;
}

.validation-error,
.validation-success {
  margin-top: 12px;
}

.example-code {
  background: #f8f9fa;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 16px;
  margin: 0;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  line-height: 1.5;
  color: #333;
  max-height: 400px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .editor-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .editor-actions {
    width: 100%;
    justify-content: flex-end;
  }
}

/* 标签页样式 */
:deep(.el-tabs--border-card) {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
}

:deep(.el-tabs__nav-wrap) {
  border-radius: 6px 6px 0 0;
}

/* 警告样式 */
:deep(.el-alert) {
  border-radius: 6px;
}

:deep(.el-alert--error) {
  background-color: #fef0f0;
  border-color: #fbc4c4;
}

:deep(.el-alert--success) {
  background-color: #f0f9ff;
  border-color: #c6f7ff;
}
</style>