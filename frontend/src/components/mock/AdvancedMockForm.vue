<template>
  <div class="advanced-mock-form">
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      size="default"
    >
      <!-- 基础信息 -->
      <el-form-item
        label="规则名称"
        prop="name"
      >
        <el-input
          v-model="form.name"
          placeholder="请输入规则名称"
          clearable
        />
      </el-form-item>

      <el-form-item label="规则描述">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="2"
          placeholder="请输入规则描述"
        />
      </el-form-item>

      <el-form-item
        label="关联API"
        prop="apiId"
      >
        <el-select
          v-model="form.apiId"
          placeholder="选择关联的API接口"
          filterable
          clearable
          style="width: 100%"
        >
          <el-option
            v-for="api in apiList"
            :key="api.id"
            :label="`${api.method} ${api.path} - ${api.name}`"
            :value="api.id"
          />
        </el-select>
      </el-form-item>

      <!-- Mock类型选择 -->
      <el-form-item
        label="Mock类型"
        prop="mockType"
      >
        <el-radio-group
          v-model="form.mockType"
          @change="handleMockTypeChange"
        >
          <el-radio-button label="STATIC">
            静态数据
          </el-radio-button>
          <el-radio-button label="DYNAMIC">
            动态数据
          </el-radio-button>
          <el-radio-button label="TEMPLATE">
            模板数据
          </el-radio-button>
        </el-radio-group>
      </el-form-item>

      <!-- 静态数据配置 -->
      <el-form-item
        v-if="form.mockType === 'STATIC'"
        label="静态JSON数据"
        prop="staticData"
      >
        <div class="json-editor-container">
          <el-input
            v-model="form.staticData"
            type="textarea"
            :rows="12"
            placeholder="请输入JSON格式的静态数据"
            class="json-textarea"
          />
          <div class="editor-toolbar">
            <el-button
              size="small"
              @click="formatJson"
            >
              格式化JSON
            </el-button>
            <el-button
              size="small"
              @click="validateJson"
            >
              验证JSON
            </el-button>
            <el-button
              size="small"
              @click="previewMockData"
            >
              预览数据
            </el-button>
          </div>
        </div>
      </el-form-item>

      <!-- 动态数据配置 -->
      <el-form-item
        v-if="form.mockType === 'DYNAMIC'"
        label="动态配置"
        prop="dynamicConfig"
      >
        <el-tabs
          v-model="activeTab"
          type="border-card"
        >
          <!-- 基础动态配置 -->
          <el-tab-pane
            label="基础配置"
            name="basic"
          >
            <div class="json-editor-container">
              <el-input
                v-model="form.dynamicConfig"
                type="textarea"
                :rows="10"
                placeholder="请输入动态配置JSON，支持占位符如 {{name.full}}、{{date.now}} 等"
                class="json-textarea"
              />
              <div class="editor-toolbar">
                <el-button
                  size="small"
                  @click="formatJson"
                >
                  格式化JSON
                </el-button>
                <el-button
                  size="small"
                  @click="insertPlaceholder"
                >
                  插入占位符
                </el-button>
                <el-button
                  size="small"
                  @click="previewMockData"
                >
                  预览数据
                </el-button>
              </div>
            </div>
          </el-tab-pane>

          <!-- 高级规则配置 -->
          <el-tab-pane
            label="高级规则"
            name="advanced"
          >
            <div class="advanced-rules">
              <el-alert
                title="高级规则说明"
                type="info"
                show-icon
                :closable="false"
                class="rules-alert"
              >
                <p>支持以下高级功能：</p>
                <ul>
                  <li>条件判断：<code v-pre>{{if condition}}...{{ endif }}</code></li>
                  <li>条件分支：<code v-pre>{{if condition}}...{{else}}...{{ endif }}</code></li>
                  <li>JavaScript表达式：<code v-pre>{{js: Math.random() * 100}}</code></li>
                  <li>数据关联：<code v-pre>{{relation:user:id}}</code></li>
                  <li>高级生成器：<code>@uuid</code>、<code>@timestamp</code>、<code>@random.phone</code></li>
                </ul>
              </el-alert>

              <div class="rule-templates">
                <h4>常用模板</h4>
                <el-row :gutter="12">
                  <el-col :span="8">
                    <el-card
                      class="template-card"
                      @click="useTemplate('conditional')"
                    >
                      <h5>条件判断</h5>
                      <p>根据请求参数返回不同数据</p>
                    </el-card>
                  </el-col>
                  <el-col :span="8">
                    <el-card
                      class="template-card"
                      @click="useTemplate('pagination')"
                    >
                      <h5>分页数据</h5>
                      <p>生成带分页的列表数据</p>
                    </el-card>
                  </el-col>
                  <el-col :span="8">
                    <el-card
                      class="template-card"
                      @click="useTemplate('user-profile')"
                    >
                      <h5>用户资料</h5>
                      <p>生成完整的用户信息</p>
                    </el-card>
                  </el-col>
                </el-row>
              </div>

              <div class="custom-rule-editor">
                <h4>自定义规则</h4>
                <el-input
                  v-model="form.dynamicConfig"
                  type="textarea"
                  :rows="15"
                  placeholder="编写高级Mock规则..."
                  class="json-textarea"
                />
              </div>
            </div>
          </el-tab-pane>

          <!-- JavaScript脚本 -->
          <el-tab-pane
            label="JavaScript脚本"
            name="javascript"
          >
            <div class="javascript-editor">
              <el-alert
                title="JavaScript脚本支持"
                type="info"
                show-icon
                :closable="false"
              >
                <p>您可以使用JavaScript编写复杂的Mock逻辑，支持以下对象：</p>
                <ul>
                  <li><code>request</code> - 请求信息（headers, params, body等）</li>
                  <li><code>api</code> - API接口信息</li>
                  <li><code>random</code> - 随机数生成工具</li>
                  <li><code>date</code> - 日期处理工具</li>
                  <li><code>Math</code> - 数学计算</li>
                </ul>
              </el-alert>

              <div class="script-examples">
                <h4>示例脚本</h4>
                <el-collapse v-model="activeExample">
                  <el-collapse-item
                    title="根据请求参数返回不同数据"
                    name="example1"
                  >
                    <pre class="code-example">{{ `// 根据用户ID返回不同的用户信息
if (request.queryParams.userId === '1') {
  return {
    id: 1,
    name: "管理员用户",
    role: "admin"
  };
} else {
  return {
    id: parseInt(request.queryParams.userId) || random.int(1, 1000),
    name: random.string(8),
    role: "user"
  };
}` }}</pre>
                  </el-collapse-item>
                  <el-collapse-item
                    title="动态生成列表数据"
                    name="example2"
                  >
                    <pre class="code-example">{{ `// 生成指定数量的用户列表
const count = parseInt(request.queryParams.count) || 10;
const users = [];

for (let i = 0; i < count; i++) {
  users.push({
    id: i + 1,
    name: random.string(6),
    email: \`user\${i}@example.com\`,
    createdAt: date.addDays(-random.int(1, 365))
  });
}

return {
  data: users,
  total: count,
  page: parseInt(request.queryParams.page) || 1
};` }}</pre>
                  </el-collapse-item>
                </el-collapse>
              </div>

              <el-input
                v-model="jsScript"
                type="textarea"
                :rows="12"
                placeholder="编写JavaScript脚本..."
                class="js-textarea"
              />

              <div class="script-toolbar">
                <el-button
                  size="small"
                  @click="testScript"
                >
                  测试脚本
                </el-button>
                <el-button
                  size="small"
                  @click="insertJsTemplate"
                >
                  插入模板
                </el-button>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-form-item>

      <!-- 模板数据配置 -->
      <el-form-item
        v-if="form.mockType === 'TEMPLATE'"
        label="模板选择"
        prop="templateName"
      >
        <el-select
          v-model="form.templateName"
          placeholder="选择Mock模板"
          style="width: 100%"
        >
          <el-option
            label="用户信息模板"
            value="user"
          />
          <el-option
            label="商品列表模板"
            value="product"
          />
          <el-option
            label="订单详情模板"
            value="order"
          />
        </el-select>
      </el-form-item>

      <!-- 响应配置 -->
      <el-divider content-position="left">
        响应配置
      </el-divider>

      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item
            label="状态码"
            prop="statusCode"
          >
            <el-select
              v-model="form.statusCode"
              placeholder="HTTP状态码"
            >
              <el-option
                label="200 OK"
                :value="200"
              />
              <el-option
                label="201 Created"
                :value="201"
              />
              <el-option
                label="400 Bad Request"
                :value="400"
              />
              <el-option
                label="401 Unauthorized"
                :value="401"
              />
              <el-option
                label="403 Forbidden"
                :value="403"
              />
              <el-option
                label="404 Not Found"
                :value="404"
              />
              <el-option
                label="500 Internal Server Error"
                :value="500"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="响应延迟(ms)">
            <el-input-number
              v-model="form.delayMs"
              :min="0"
              :max="10000"
              :step="100"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="优先级">
            <el-input-number
              v-model="form.priority"
              :min="1"
              :max="100"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 响应头配置 -->
      <el-form-item label="响应头">
        <div class="headers-config">
          <el-input
            v-model="form.headers"
            type="textarea"
            :rows="3"
            placeholder="{&quot;Content-Type&quot;: &quot;application/json&quot;, &quot;X-Custom-Header&quot;: &quot;value&quot;}"
          />
          <div class="headers-toolbar">
            <el-button
              size="small"
              @click="addCommonHeaders"
            >
              添加常用响应头
            </el-button>
            <el-button
              size="small"
              @click="formatHeaders"
            >
              格式化
            </el-button>
          </div>
        </div>
      </el-form-item>

      <!-- 匹配条件 -->
      <el-form-item label="匹配条件">
        <el-input
          v-model="form.matchCondition"
          type="textarea"
          :rows="2"
          placeholder="可选：设置规则生效的条件，如请求头、参数等条件判断"
        />
      </el-form-item>

      <!-- 启用状态 -->
      <el-form-item label="启用状态">
        <el-switch
          v-model="form.enabled"
          active-text="启用"
          inactive-text="禁用"
        />
      </el-form-item>

      <!-- 操作按钮 -->
      <el-form-item>
        <el-button
          type="primary"
          :loading="submitting"
          @click="handleSubmit"
        >
          {{ isEdit ? '更新规则' : '创建规则' }}
        </el-button>
        <el-button @click="handleCancel">
          取消
        </el-button>
        <el-button @click="previewMockData">
          预览数据
        </el-button>
        <el-button @click="testMockRule">
          测试规则
        </el-button>
      </el-form-item>
    </el-form>

    <!-- 预览对话框 -->
    <el-dialog
      v-model="previewDialogVisible"
      title="Mock数据预览"
      width="60%"
      append-to-body
    >
      <div class="preview-content">
        <el-tabs v-model="previewTab">
          <el-tab-pane
            label="格式化预览"
            name="formatted"
          >
            <pre class="json-preview">{{ formattedPreviewData }}</pre>
          </el-tab-pane>
          <el-tab-pane
            label="原始数据"
            name="raw"
          >
            <el-input
              :model-value="previewData"
              type="textarea"
              :rows="15"
              readonly
            />
          </el-tab-pane>
        </el-tabs>
      </div>
      <template #footer>
        <el-button @click="previewDialogVisible = false">
          关闭
        </el-button>
        <el-button
          type="primary"
          @click="copyPreviewData"
        >
          复制数据
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// Props
const props = defineProps({
  projectId: {
    type: Number,
    required: true
  },
  mockRule: {
    type: Object,
    default: null
  },
  apiList: {
    type: Array,
    default: () => []
  }
})

// Emits
const emit = defineEmits(['submit', 'cancel'])

// 响应式数据
const formRef = ref()
const submitting = ref(false)
const isEdit = computed(() => !!props.mockRule)

// 表单数据
const form = reactive({
  name: '',
  description: '',
  apiId: null,
  mockType: 'STATIC',
  staticData: '',
  dynamicConfig: '',
  templateName: '',
  statusCode: 200,
  delayMs: 0,
  priority: 1,
  headers: '',
  matchCondition: '',
  enabled: true
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入规则名称', trigger: 'blur' },
    { min: 2, max: 50, message: '名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  apiId: [
    { required: true, message: '请选择关联的API接口', trigger: 'change' }
  ],
  mockType: [
    { required: true, message: '请选择Mock类型', trigger: 'change' }
  ],
  staticData: [
    { validator: validateJsonData, trigger: 'blur' }
  ],
  statusCode: [
    { required: true, message: '请选择状态码', trigger: 'change' }
  ]
}

// Tab状态
const activeTab = ref('basic')
const activeExample = ref([])
const previewTab = ref('formatted')

// JavaScript脚本
const jsScript = ref('')

// 预览相关
const previewDialogVisible = ref(false)
const previewData = ref('')
const formattedPreviewData = computed(() => {
  try {
    return JSON.stringify(JSON.parse(previewData.value), null, 2)
  } catch {
    return previewData.value
  }
})

// 表单验证函数
function validateJsonData(rule, value, callback) {
  if (!value || value.trim() === '') {
    callback(new Error('请输入JSON数据'))
    return
  }

  try {
    JSON.parse(value)
    callback()
  } catch (error) {
    callback(new Error('JSON格式不正确：' + error.message))
  }
}

// 方法定义
const handleMockTypeChange = (type) => {
  // 清空其他类型的数据
  if (type !== 'STATIC') form.staticData = ''
  if (type !== 'DYNAMIC') form.dynamicConfig = ''
  if (type !== 'TEMPLATE') form.templateName = ''
}

const formatJson = () => {
  try {
    const data = form.mockType === 'STATIC' ? form.staticData : form.dynamicConfig
    if (data) {
      const formatted = JSON.stringify(JSON.parse(data), null, 2)
      if (form.mockType === 'STATIC') {
        form.staticData = formatted
      } else {
        form.dynamicConfig = formatted
      }
      ElMessage.success('JSON格式化完成')
    }
  } catch (error) {
    ElMessage.error('JSON格式不正确：' + error.message)
  }
}

const validateJson = () => {
  try {
    const data = form.mockType === 'STATIC' ? form.staticData : form.dynamicConfig
    if (data) {
      JSON.parse(data)
      ElMessage.success('JSON格式验证通过')
    }
  } catch (error) {
    ElMessage.error('JSON格式不正确：' + error.message)
  }
}

const insertPlaceholder = () => {
  ElMessageBox.prompt('选择占位符类型', '插入占位符', {
    inputType: 'textarea',
    inputPlaceholder: '常用占位符：\n{{name.full}} - 姓名\n{{email}} - 邮箱\n{{phone}} - 电话\n{{date.now}} - 当前时间\n{{number.int}} - 随机整数'
  }).then(({ value }) => {
    if (value) {
      form.dynamicConfig += `\n${value}`
    }
  })
}

const useTemplate = (templateType) => {
  const templates = {
    conditional: `{
  "{{if request.queryParams.type === 'admin'}}": {
    "role": "admin",
    "permissions": ["read", "write", "delete"]
  },
  "{{else}}": {
    "role": "user",
    "permissions": ["read"]
  },
  "{{endif}}": "",
  "id": "{{js: random.int(1, 1000)}}",
  "name": "{{name.full}}",
  "timestamp": "@timestamp"
}`,
    pagination: `{
  "data": [
    {{repeat 10}}{
      "id": "{{js: $index + 1}}",
      "name": "{{name.full}}",
      "email": "{{email}}",
      "createdAt": "{{date.now}}"
    }{{/repeat}}
  ],
  "total": "{{js: random.int(50, 200)}}",
  "page": "{{js: parseInt(request.queryParams.page) || 1}}",
  "pageSize": 10
}`,
    'user-profile': `{
  "id": "@uuid",
  "name": "{{name.full}}",
  "email": "{{email}}",
  "phone": "@random.phone",
  "address": "@random.address",
  "company": "@random.company",
  "avatar": "{{image.avatar}}",
  "profile": {
    "bio": "{{lorem.paragraph}}",
    "website": "{{internet.url}}",
    "social": {
      "twitter": "{{internet.userName}}",
      "github": "{{internet.userName}}"
    }
  },
  "settings": {
    "theme": "{{js: random.boolean() ? 'dark' : 'light'}}",
    "notifications": "{{js: random.boolean()}}",
    "language": "{{js: array.random('en', 'zh', 'es', 'fr')}}"
  },
  "stats": {
    "loginCount": "{{js: random.int(1, 1000)}}",
    "lastLogin": "{{date.addHours(-random.int(1, 24))}}"
  }
}`
  }

  if (templates[templateType]) {
    form.dynamicConfig = templates[templateType]
    ElMessage.success('模板已插入')
  }
}

const testScript = () => {
  // 这里可以调用后端API测试JavaScript脚本
  ElMessage.info('脚本测试功能开发中...')
}

const insertJsTemplate = () => {
  const template = `// 示例JavaScript模板
const userId = parseInt(request.queryParams.userId) || random.int(1, 1000);

return {
  id: userId,
  name: random.string(8),
  email: \`user\${userId}@example.com\`,
  createdAt: date.addDays(-random.int(1, 365)),
  isActive: random.boolean()
};`

  jsScript.value = template
}

const addCommonHeaders = () => {
  const commonHeaders = {
    'Content-Type': 'application/json',
    'Cache-Control': 'no-cache',
    'Access-Control-Allow-Origin': '*'
  }
  form.headers = JSON.stringify(commonHeaders, null, 2)
}

const formatHeaders = () => {
  try {
    if (form.headers) {
      form.headers = JSON.stringify(JSON.parse(form.headers), null, 2)
      ElMessage.success('响应头格式化完成')
    }
  } catch (error) {
    ElMessage.error('响应头格式不正确：' + error.message)
  }
}

const previewMockData = () => {
  // 这里可以调用后端API预览Mock数据
  const mockData = getMockDataByType()
  previewData.value = JSON.stringify(mockData, null, 2)
  previewDialogVisible.value = true
}

const getMockDataByType = () => {
  switch (form.mockType) {
    case 'STATIC':
      try {
        return JSON.parse(form.staticData || '{}')
      } catch {
        return { error: 'Invalid JSON format' }
      }
    case 'DYNAMIC':
      return {
        message: '动态数据预览',
        config: form.dynamicConfig,
        note: '实际数据将根据配置动态生成'
      }
    case 'TEMPLATE':
      return {
        message: '模板数据预览',
        template: form.templateName,
        note: '实际数据将根据模板生成'
      }
    default:
      return {}
  }
}

const copyPreviewData = () => {
  navigator.clipboard.writeText(previewData.value).then(() => {
    ElMessage.success('数据已复制到剪贴板')
  })
}

const testMockRule = () => {
  // 这里可以调用后端API测试Mock规则
  ElMessage.info('Mock规则测试功能开发中...')
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()

    submitting.value = true

    const mockRuleData = {
      ...form,
      projectId: props.projectId
    }

    emit('submit', mockRuleData)
  } catch (error) {
    ElMessage.error('表单验证失败，请检查输入')
  } finally {
    submitting.value = false
  }
}

const handleCancel = () => {
  emit('cancel')
}

// 初始化表单数据
const initForm = () => {
  if (props.mockRule) {
    Object.assign(form, props.mockRule)
  }
}

// 监听props变化
watch(() => props.mockRule, initForm, { immediate: true })

onMounted(() => {
  initForm()
})
</script>

<style scoped>
.advanced-mock-form {
  max-width: 1000px;
  margin: 0 auto;
}

.json-editor-container {
  position: relative;
}

.json-textarea {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
}

.editor-toolbar {
  margin-top: 8px;
  text-align: right;
}

.editor-toolbar .el-button {
  margin-left: 8px;
}

.advanced-rules {
  padding: 20px 0;
}

.rules-alert {
  margin-bottom: 20px;
}

.rules-alert ul {
  margin: 10px 0 0 20px;
  padding: 0;
}

.rules-alert li {
  margin: 5px 0;
}

.rules-alert code {
  background: #f5f5f5;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: monospace;
}

.rule-templates {
  margin: 20px 0;
}

.template-card {
  cursor: pointer;
  transition: all 0.3s;
  text-align: center;
  height: 100px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.template-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.template-card h5 {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #409eff;
}

.template-card p {
  margin: 0;
  font-size: 12px;
  color: #666;
}

.custom-rule-editor {
  margin-top: 20px;
}

.javascript-editor {
  padding: 20px 0;
}

.script-examples {
  margin: 20px 0;
}

.code-example {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 4px;
  padding: 12px;
  margin: 0;
  font-size: 12px;
  line-height: 1.4;
  overflow-x: auto;
}

.js-textarea {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  margin-top: 12px;
}

.script-toolbar {
  margin-top: 8px;
  text-align: right;
}

.headers-config {
  width: 100%;
}

.headers-toolbar {
  margin-top: 8px;
  text-align: right;
}

.preview-content {
  max-height: 60vh;
  overflow-y: auto;
}

.json-preview {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 4px;
  padding: 16px;
  margin: 0;
  font-size: 13px;
  line-height: 1.4;
  overflow-x: auto;
  white-space: pre-wrap;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

.el-divider {
  margin: 30px 0 20px 0;
}

.el-form-item {
  margin-bottom: 22px;
}
</style>
