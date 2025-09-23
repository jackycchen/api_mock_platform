<template>
  <div class="mock-version-manager">
    <el-card>
      <template #header>
        <div class="header-content">
          <h3>Mock规则版本管理</h3>
          <div class="header-actions">
            <el-button
              :loading="loading.versions"
              @click="refreshVersions"
            >
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button
              v-permission="['api:admin']"
              type="danger"
              @click="handleCleanupVersions"
            >
              <el-icon><Delete /></el-icon>
              清理所有版本
            </el-button>
          </div>
        </div>
      </template>

      <!-- 版本列表 -->
      <el-table
        :data="versionList"
        :loading="loading.versions"
        row-key="versionId"
        empty-text="暂无版本记录"
      >
        <el-table-column
          prop="versionId"
          label="版本ID"
          width="200"
          show-overflow-tooltip
        />

        <el-table-column
          prop="createTime"
          label="创建时间"
          width="180"
        >
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column
          prop="changeDescription"
          label="变更描述"
          show-overflow-tooltip
        />

        <el-table-column
          prop="operator"
          label="操作者"
          width="120"
        />

        <el-table-column
          prop="ruleName"
          label="规则名称"
          width="150"
          show-overflow-tooltip
        />

        <el-table-column
          prop="mockType"
          label="Mock类型"
          width="100"
        >
          <template #default="scope">
            <el-tag :type="getMockTypeTag(scope.row.mockType)">
              {{ scope.row.mockType }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          label="操作"
          width="300"
          fixed="right"
        >
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="handleViewVersion(scope.row)"
            >
              查看
            </el-button>

            <el-button
              v-permission="['api:write']"
              type="warning"
              size="small"
              @click="handleRollback(scope.row)"
            >
              回滚
            </el-button>

            <el-dropdown @command="(command) => handleVersionAction(command, scope.row)">
              <el-button
                type="text"
                size="small"
              >
                更多<el-icon class="el-icon--right">
                  <arrow-down />
                </el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    command="compare"
                    :disabled="!canCompare"
                  >
                    版本比较
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-permission="['api:write']"
                    command="delete"
                  >
                    删除版本
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 版本详情对话框 -->
    <el-dialog
      v-model="versionDetailVisible"
      title="版本详情"
      width="60%"
      destroy-on-close
    >
      <div
        v-if="selectedVersion"
        class="version-detail"
      >
        <el-descriptions
          border
          :column="2"
        >
          <el-descriptions-item label="版本ID">
            {{ selectedVersion.versionId }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatTime(selectedVersion.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="操作者">
            {{ selectedVersion.operator }}
          </el-descriptions-item>
          <el-descriptions-item label="Mock类型">
            <el-tag :type="getMockTypeTag(selectedVersion.mockType)">
              {{ selectedVersion.mockType }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item
            label="变更描述"
            :span="2"
          >
            {{ selectedVersion.changeDescription }}
          </el-descriptions-item>
        </el-descriptions>

        <div
          v-if="versionContent"
          class="version-content"
        >
          <h4>Mock规则内容</h4>
          <el-tabs v-model="activeTab">
            <el-tab-pane
              v-if="versionContent.staticData"
              label="静态数据"
              name="static"
            >
              <el-input
                type="textarea"
                :model-value="versionContent.staticData"
                :rows="10"
                readonly
              />
            </el-tab-pane>

            <el-tab-pane
              v-if="versionContent.dynamicConfig"
              label="动态配置"
              name="dynamic"
            >
              <el-input
                type="textarea"
                :model-value="versionContent.dynamicConfig"
                :rows="10"
                readonly
              />
            </el-tab-pane>

            <el-tab-pane
              label="基本信息"
              name="basic"
            >
              <el-form label-width="120px">
                <el-form-item label="规则名称">
                  <el-input
                    :model-value="versionContent.name"
                    readonly
                  />
                </el-form-item>
                <el-form-item label="状态码">
                  <el-input
                    :model-value="versionContent.statusCode"
                    readonly
                  />
                </el-form-item>
                <el-form-item label="延迟时间">
                  <el-input
                    :model-value="versionContent.delayMs + 'ms'"
                    readonly
                  />
                </el-form-item>
                <el-form-item label="优先级">
                  <el-input
                    :model-value="versionContent.priority"
                    readonly
                  />
                </el-form-item>
                <el-form-item label="启用状态">
                  <el-tag :type="versionContent.enabled ? 'success' : 'danger'">
                    {{ versionContent.enabled ? '启用' : '禁用' }}
                  </el-tag>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </el-dialog>

    <!-- 版本比较对话框 -->
    <el-dialog
      v-model="compareDialogVisible"
      title="版本比较"
      width="80%"
      destroy-on-close
    >
      <div class="compare-selector">
        <el-form
          :model="compareForm"
          inline
        >
          <el-form-item label="版本1">
            <el-select
              v-model="compareForm.version1"
              placeholder="选择版本"
            >
              <el-option
                v-for="version in versionList"
                :key="version.versionId"
                :label="`${version.versionId} - ${formatTime(version.createTime)}`"
                :value="version.versionId"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="版本2">
            <el-select
              v-model="compareForm.version2"
              placeholder="选择版本"
            >
              <el-option
                v-for="version in versionList"
                :key="version.versionId"
                :label="`${version.versionId} - ${formatTime(version.createTime)}`"
                :value="version.versionId"
              />
            </el-select>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              :disabled="!compareForm.version1 || !compareForm.version2"
              :loading="loading.comparison"
              @click="compareVersions"
            >
              开始比较
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <div
        v-if="versionComparison"
        class="comparison-result"
      >
        <el-alert
          v-if="!versionComparison.hasDifferences"
          title="两个版本完全相同"
          type="success"
          :closable="false"
        />

        <div v-else>
          <h4>版本差异</h4>
          <el-list>
            <el-list-item
              v-for="(diff, index) in versionComparison.differences"
              :key="index"
            >
              <el-icon class="diff-icon">
                <DArrowRight />
              </el-icon>
              {{ diff }}
            </el-list-item>
          </el-list>
        </div>
      </div>
    </el-dialog>

    <!-- 回滚确认对话框 -->
    <el-dialog
      v-model="rollbackDialogVisible"
      title="确认回滚"
      width="500px"
    >
      <div class="rollback-warning">
        <el-alert
          title="警告"
          type="warning"
          description="回滚操作将会创建一个新的版本，当前的Mock规则将被替换为选择的历史版本。"
          :closable="false"
          show-icon
        />

        <div
          v-if="rollbackTarget"
          class="rollback-info"
        >
          <h4>回滚目标版本</h4>
          <p><strong>版本ID:</strong> {{ rollbackTarget.versionId }}</p>
          <p><strong>创建时间:</strong> {{ formatTime(rollbackTarget.createTime) }}</p>
          <p><strong>变更描述:</strong> {{ rollbackTarget.changeDescription }}</p>
        </div>
      </div>

      <template #footer>
        <el-button @click="rollbackDialogVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          @click="confirmRollback"
        >
          确认回滚
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Refresh,
  Delete,
  ArrowDown,
  DArrowRight
} from '@element-plus/icons-vue'
import { useMockDataStore } from '@/stores/mockData'
import { storeToRefs } from 'pinia'
import { formatTime } from '@/utils/date'
import { useUserStore } from '@/stores/user'
import * as mockDataApi from '@/api/mockData'

const props = defineProps({
  apiId: {
    type: [String, Number],
    required: true
  }
})

const mockDataStore = useMockDataStore()
const userStore = useUserStore()
const {
  versionList,
  versionComparison,
  loading
} = storeToRefs(mockDataStore)

// 对话框状态
const versionDetailVisible = ref(false)
const compareDialogVisible = ref(false)
const rollbackDialogVisible = ref(false)

// 选中的版本
const selectedVersion = ref(null)
const versionContent = ref(null)
const rollbackTarget = ref(null)

// 版本比较表单
const compareForm = ref({
  version1: '',
  version2: ''
})

// 详情页签
const activeTab = ref('basic')

// 计算属性
const canCompare = computed(() => versionList.value.length >= 2)

// 获取Mock类型标签样式
const getMockTypeTag = (mockType) => {
  const typeMap = {
    'STATIC': 'success',
    'DYNAMIC': 'warning',
    'TEMPLATE': 'info'
  }
  return typeMap[mockType] || 'info'
}

// 刷新版本列表
const refreshVersions = async () => {
  try {
    await mockDataStore.fetchVersionList(props.apiId)
  } catch (error) {
    ElMessage.error('刷新版本列表失败: ' + error.message)
  }
}

// 查看版本详情
const handleViewVersion = async (version) => {
  try {
    selectedVersion.value = version

    // 获取版本内容
    const response = await mockDataApi.getMockRuleVersion(props.apiId, version.versionId)
    versionContent.value = response.data

    // 根据内容决定默认显示的标签页
    if (versionContent.value.staticData) {
      activeTab.value = 'static'
    } else if (versionContent.value.dynamicConfig) {
      activeTab.value = 'dynamic'
    } else {
      activeTab.value = 'basic'
    }

    versionDetailVisible.value = true
  } catch (error) {
    ElMessage.error('获取版本详情失败: ' + error.message)
  }
}

// 处理版本操作
const handleVersionAction = (command, version) => {
  switch (command) {
    case 'compare':
      openCompareDialog(version)
      break
    case 'delete':
      handleDeleteVersion(version)
      break
  }
}

// 打开比较对话框
const openCompareDialog = (version) => {
  compareForm.value.version1 = version.versionId
  compareForm.value.version2 = ''
  compareDialogVisible.value = true
}

// 比较版本
const compareVersions = async () => {
  if (!compareForm.value.version1 || !compareForm.value.version2) {
    ElMessage.warning('请选择两个版本进行比较')
    return
  }

  if (compareForm.value.version1 === compareForm.value.version2) {
    ElMessage.warning('不能选择相同的版本进行比较')
    return
  }

  try {
    await mockDataStore.compareVersions(
      props.apiId,
      compareForm.value.version1,
      compareForm.value.version2
    )
  } catch (error) {
    ElMessage.error('版本比较失败: ' + error.message)
  }
}

// 处理回滚
const handleRollback = (version) => {
  rollbackTarget.value = version
  rollbackDialogVisible.value = true
}

// 确认回滚
const confirmRollback = async () => {
  try {
    const operator = userStore.user?.username || 'Unknown'
    const success = await mockDataStore.rollbackToVersion(
      props.apiId,
      rollbackTarget.value.versionId,
      operator
    )

    if (success) {
      ElMessage.success('版本回滚成功')
      rollbackDialogVisible.value = false
      rollbackTarget.value = null
    } else {
      ElMessage.error('版本回滚失败')
    }
  } catch (error) {
    ElMessage.error('版本回滚失败: ' + error.message)
  }
}

// 删除版本
const handleDeleteVersion = async (version) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除版本 ${version.versionId} 吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const success = await mockDataStore.deleteVersion(props.apiId, version.versionId)
    if (success) {
      ElMessage.success('版本删除成功')
    } else {
      ElMessage.error('版本删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('版本删除失败: ' + error.message)
    }
  }
}

// 清理所有版本
const handleCleanupVersions = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清理该API的所有版本吗？此操作不可恢复。',
      '确认清理',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await mockDataStore.cleanupApiVersions(props.apiId)
    ElMessage.success('版本清理成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('版本清理失败: ' + error.message)
    }
  }
}

onMounted(() => {
  refreshVersions()
})
</script>

<style scoped>
.mock-version-manager {
  padding: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.version-detail {
  padding: 20px 0;
}

.version-content {
  margin-top: 20px;
}

.version-content h4 {
  margin: 0 0 16px 0;
  color: #303133;
}

.compare-selector {
  padding: 20px 0;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 20px;
}

.comparison-result {
  padding: 20px 0;
}

.comparison-result h4 {
  margin: 0 0 16px 0;
  color: #303133;
}

.diff-icon {
  margin-right: 8px;
  color: #409eff;
}

.rollback-warning {
  padding: 20px 0;
}

.rollback-info {
  margin-top: 20px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.rollback-info h4 {
  margin: 0 0 12px 0;
  color: #303133;
}

.rollback-info p {
  margin: 8px 0;
  color: #606266;
}
</style>