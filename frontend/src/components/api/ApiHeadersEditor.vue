<template>
  <div class="api-headers-editor">
    <div class="editor-header">
      <span class="editor-title">请求头配置</span>
      <el-button
        type="primary"
        size="small"
        @click="addHeader"
      >
        <el-icon><Plus /></el-icon>
        添加请求头
      </el-button>
    </div>

    <el-table
      :data="headers"
      border
      size="small"
      empty-text="暂无请求头配置"
    >
      <el-table-column
        label="参数名"
        width="200"
      >
        <template #default="scope">
          <el-input
            v-model="scope.row.name"
            placeholder="请输入参数名"
            size="small"
            @input="handleChange"
          />
        </template>
      </el-table-column>

      <el-table-column
        label="描述"
        min-width="200"
      >
        <template #default="scope">
          <el-input
            v-model="scope.row.description"
            placeholder="请输入描述"
            size="small"
            @input="handleChange"
          />
        </template>
      </el-table-column>

      <el-table-column
        label="必填"
        width="80"
      >
        <template #default="scope">
          <el-switch
            v-model="scope.row.required"
            size="small"
            @change="handleChange"
          />
        </template>
      </el-table-column>

      <el-table-column
        label="示例值"
        width="150"
      >
        <template #default="scope">
          <el-input
            v-model="scope.row.example"
            placeholder="示例值"
            size="small"
            @input="handleChange"
          />
        </template>
      </el-table-column>

      <el-table-column
        label="操作"
        width="60"
      >
        <template #default="scope">
          <el-button
            type="danger"
            size="small"
            text
            @click="removeHeader(scope.$index)"
          >
            <el-icon><Delete /></el-icon>
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div
      v-if="headers.length === 0"
      class="empty-state"
    >
      <el-empty
        description="点击上方按钮添加请求头配置"
        :image-size="60"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Plus, Delete } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue'])

const headers = ref([])

// 初始化数据
const initHeaders = () => {
  headers.value = props.modelValue.length > 0
    ? [...props.modelValue]
    : []
}

// 添加请求头
const addHeader = () => {
  headers.value.push({
    name: '',
    description: '',
    required: false,
    example: ''
  })
  handleChange()
}

// 删除请求头
const removeHeader = (index) => {
  headers.value.splice(index, 1)
  handleChange()
}

// 处理变更
const handleChange = () => {
  emit('update:modelValue', headers.value)
}

// 监听外部数据变化
watch(() => props.modelValue, () => {
  initHeaders()
}, { immediate: true, deep: true })
</script>

<style scoped>
.api-headers-editor {
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

.empty-state {
  padding: 20px 0;
  text-align: center;
}

:deep(.el-table) {
  border-radius: 6px;
}

:deep(.el-table .el-table__header-wrapper) {
  border-radius: 6px 6px 0 0;
}

:deep(.el-empty) {
  padding: 20px 0;
}

:deep(.el-empty__description) {
  color: #909399;
  font-size: 13px;
}
</style>