<template>
  <div class="api-parameters-editor">
    <div class="editor-header">
      <span class="editor-title">{{ title }}配置</span>
      <el-button
        type="primary"
        size="small"
        @click="addParameter"
      >
        <el-icon><Plus /></el-icon>
        添加参数
      </el-button>
    </div>

    <el-table
      :data="parameters"
      border
      size="small"
      :empty-text="`暂无${title}配置`"
    >
      <el-table-column
        label="参数名"
        width="160"
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
        label="类型"
        width="120"
      >
        <template #default="scope">
          <el-select
            v-model="scope.row.type"
            placeholder="选择类型"
            size="small"
            @change="handleChange"
          >
            <el-option
              label="string"
              value="string"
            />
            <el-option
              label="number"
              value="number"
            />
            <el-option
              label="integer"
              value="integer"
            />
            <el-option
              label="boolean"
              value="boolean"
            />
            <el-option
              label="array"
              value="array"
            />
            <el-option
              label="object"
              value="object"
            />
          </el-select>
        </template>
      </el-table-column>

      <el-table-column
        label="描述"
        min-width="150"
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
        width="60"
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
        width="120"
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
        v-if="showConstraints"
        label="约束"
        width="100"
      >
        <template #default="scope">
          <el-popover
            placement="left"
            width="300"
            trigger="click"
            :title="`${scope.row.name} 约束设置`"
          >
            <template #reference>
              <el-button
                size="small"
                text
                type="primary"
              >
                <el-icon><Setting /></el-icon>
              </el-button>
            </template>
            <div class="constraints-form">
              <el-form
                size="small"
                label-width="80px"
              >
                <el-form-item
                  v-if="scope.row.type === 'string'"
                  label="最小长度"
                >
                  <el-input-number
                    v-model="scope.row.minLength"
                    :min="0"
                    size="small"
                    style="width: 100%"
                    @change="handleChange"
                  />
                </el-form-item>
                <el-form-item
                  v-if="scope.row.type === 'string'"
                  label="最大长度"
                >
                  <el-input-number
                    v-model="scope.row.maxLength"
                    :min="0"
                    size="small"
                    style="width: 100%"
                    @change="handleChange"
                  />
                </el-form-item>
                <el-form-item
                  v-if="['number', 'integer'].includes(scope.row.type)"
                  label="最小值"
                >
                  <el-input-number
                    v-model="scope.row.minimum"
                    size="small"
                    style="width: 100%"
                    @change="handleChange"
                  />
                </el-form-item>
                <el-form-item
                  v-if="['number', 'integer'].includes(scope.row.type)"
                  label="最大值"
                >
                  <el-input-number
                    v-model="scope.row.maximum"
                    size="small"
                    style="width: 100%"
                    @change="handleChange"
                  />
                </el-form-item>
                <el-form-item
                  v-if="scope.row.type === 'string'"
                  label="格式"
                >
                  <el-select
                    v-model="scope.row.format"
                    placeholder="选择格式"
                    size="small"
                    clearable
                    style="width: 100%"
                    @change="handleChange"
                  >
                    <el-option
                      label="email"
                      value="email"
                    />
                    <el-option
                      label="date"
                      value="date"
                    />
                    <el-option
                      label="date-time"
                      value="date-time"
                    />
                    <el-option
                      label="uri"
                      value="uri"
                    />
                    <el-option
                      label="uuid"
                      value="uuid"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item
                  v-if="scope.row.type === 'string'"
                  label="正则"
                >
                  <el-input
                    v-model="scope.row.pattern"
                    placeholder="正则表达式"
                    size="small"
                    @input="handleChange"
                  />
                </el-form-item>
              </el-form>
            </div>
          </el-popover>
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
            @click="removeParameter(scope.$index)"
          >
            <el-icon><Delete /></el-icon>
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div
      v-if="parameters.length === 0"
      class="empty-state"
    >
      <el-empty
        :description="`点击上方按钮添加${title}配置`"
        :image-size="60"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Plus, Delete, Setting } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  },
  type: {
    type: String,
    default: 'query',
    validator: (value) => ['path', 'query', 'header'].includes(value)
  }
})

const emit = defineEmits(['update:modelValue'])

const parameters = ref([])

// 计算属性
const title = computed(() => {
  const titles = {
    path: '路径参数',
    query: '查询参数',
    header: '请求头参数'
  }
  return titles[props.type] || '参数'
})

const showConstraints = computed(() => {
  return props.type !== 'header'
})

// 初始化数据
const initParameters = () => {
  parameters.value = props.modelValue.length > 0
    ? [...props.modelValue]
    : []
}

// 添加参数
const addParameter = () => {
  const newParameter = {
    name: '',
    type: 'string',
    description: '',
    required: props.type === 'path', // 路径参数默认必填
    example: ''
  }

  // 根据类型添加额外字段
  if (showConstraints.value) {
    Object.assign(newParameter, {
      minLength: undefined,
      maxLength: undefined,
      minimum: undefined,
      maximum: undefined,
      format: undefined,
      pattern: undefined
    })
  }

  parameters.value.push(newParameter)
  handleChange()
}

// 删除参数
const removeParameter = (index) => {
  parameters.value.splice(index, 1)
  handleChange()
}

// 处理变更
const handleChange = () => {
  // 清理空值
  const cleanData = parameters.value.map(param => {
    const cleaned = { ...param }
    Object.keys(cleaned).forEach(key => {
      if (cleaned[key] === undefined || cleaned[key] === null || cleaned[key] === '') {
        if (['required'].includes(key)) return // 保留布尔值字段
        delete cleaned[key]
      }
    })
    return cleaned
  })

  emit('update:modelValue', cleanData)
}

// 监听外部数据变化
watch(() => props.modelValue, () => {
  initParameters()
}, { immediate: true, deep: true })
</script>

<style scoped>
.api-parameters-editor {
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

.constraints-form {
  padding: 0;
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

:deep(.el-form-item) {
  margin-bottom: 12px;
}

:deep(.el-form-item__label) {
  font-size: 13px;
}

:deep(.el-popover) {
  padding: 16px;
}
</style>