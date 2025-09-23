<template>
  <div class="project-settings">
    <div class="settings-container">
      <!-- 设置导航 -->
      <div class="settings-sidebar">
        <el-menu
          :default-active="activeTab"
          mode="vertical"
          class="settings-menu"
          @select="handleTabChange"
        >
          <el-menu-item index="basic">
            <el-icon><Setting /></el-icon>
            <span>基本设置</span>
          </el-menu-item>
          <el-menu-item index="environment">
            <el-icon><Monitor /></el-icon>
            <span>环境配置</span>
          </el-menu-item>
          <el-menu-item
            v-permission="['project:read']"
            index="proxy"
          >
            <el-icon><SwitchButton /></el-icon>
            <span>代理配置</span>
          </el-menu-item>
          <el-menu-item
            v-permission="['project:admin']"
            index="members"
          >
            <el-icon><User /></el-icon>
            <span>成员管理</span>
          </el-menu-item>
          <el-menu-item
            v-permission="['project:admin']"
            index="danger"
          >
            <el-icon><Warning /></el-icon>
            <span>危险操作</span>
          </el-menu-item>
        </el-menu>
      </div>

      <!-- 设置内容 -->
      <div class="settings-content">
        <div class="content-header">
          <h2>{{ getTabTitle() }}</h2>
          <p class="content-description">
            {{ getTabDescription() }}
          </p>
        </div>

        <!-- 基本设置 -->
        <div
          v-if="activeTab === 'basic'"
          class="settings-panel"
        >
          <ProjectBasicSettings
            :project-id="projectId"
            :settings="projectSettings"
            @updated="handleSettingsUpdated"
          />
        </div>

        <!-- 环境配置 -->
        <div
          v-if="activeTab === 'environment'"
          class="settings-panel"
        >
          <ProjectEnvironmentSettings
            :project-id="projectId"
            :settings="projectSettings"
            @updated="handleSettingsUpdated"
          />
        </div>

        <!-- 代理配置 -->
        <div
          v-if="activeTab === 'proxy'"
          class="settings-panel"
        >
          <ProjectProxySettings
            :project-id="projectId"
          />
        </div>

        <!-- 成员管理 -->
        <div
          v-if="activeTab === 'members'"
          class="settings-panel"
        >
          <ProjectMemberManagement
            :project-id="projectId"
            :project="projectSettings"
          />
        </div>

        <!-- 危险操作 -->
        <div
          v-if="activeTab === 'danger'"
          class="settings-panel"
        >
          <ProjectDangerZone
            :project-id="projectId"
            :project="projectSettings"
            @project-deleted="handleProjectDeleted"
            @ownership-transferred="handleOwnershipTransferred"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Setting,
  Monitor,
  SwitchButton,
  User,
  Warning
} from '@element-plus/icons-vue'
import { getProjectSettings } from '@/api/projectSettings'
import ProjectBasicSettings from './components/ProjectBasicSettings.vue'
import ProjectEnvironmentSettings from './components/ProjectEnvironmentSettings.vue'
import ProjectMemberManagement from './components/ProjectMemberManagement.vue'
import ProjectDangerZone from './components/ProjectDangerZone.vue'
import ProjectProxySettings from './components/ProjectProxySettings.vue'

const route = useRoute()
const router = useRouter()

const projectId = ref(parseInt(route.params.id))
const activeTab = ref(route.query.tab || 'basic')
const projectSettings = ref(null)
const loading = ref(false)

// 标签页信息
const tabInfo = {
  basic: {
    title: '基本设置',
    description: '管理项目的基本信息，包括名称、描述、类型等'
  },
  environment: {
    title: '环境配置',
    description: '配置项目的运行环境、域名、代理等设置'
  },
  proxy: {
    title: '代理配置',
    description: '管理请求代理规则，在真实服务与Mock之间切换'
  },
  members: {
    title: '成员管理',
    description: '管理项目成员权限和角色分配'
  },
  danger: {
    title: '危险操作',
    description: '项目转让、删除等不可逆操作'
  }
}

const getTabTitle = () => tabInfo[activeTab.value]?.title || '项目设置'
const getTabDescription = () => tabInfo[activeTab.value]?.description || ''

// 加载项目设置
const loadProjectSettings = async () => {
  loading.value = true
  try {
    const response = await getProjectSettings(projectId.value)
    projectSettings.value = response.data
  } catch (error) {
    ElMessage.error('加载项目设置失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 处理标签页切换
const handleTabChange = (tabName) => {
  activeTab.value = tabName
  router.push({
    name: 'ProjectSettings',
    params: { id: projectId.value },
    query: { tab: tabName }
  })
}

// 处理设置更新
const handleSettingsUpdated = (updatedSettings) => {
  projectSettings.value = updatedSettings
  ElMessage.success('设置更新成功')
}

// 处理项目删除
const handleProjectDeleted = () => {
  ElMessage.success('项目已删除')
  router.push('/projects')
}

// 处理所有权转让
const handleOwnershipTransferred = () => {
  ElMessage.success('项目所有权转让成功')
  loadProjectSettings() // 重新加载设置
}

// 监听路由参数变化
watch(() => route.params.id, (newId) => {
  if (newId) {
    projectId.value = parseInt(newId)
    loadProjectSettings()
  }
})

watch(() => route.query.tab, (newTab) => {
  if (newTab) {
    activeTab.value = newTab
  }
})

onMounted(() => {
  loadProjectSettings()
})
</script>

<style scoped>
.project-settings {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.settings-container {
  display: flex;
  max-width: 1200px;
  margin: 0 auto;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.settings-sidebar {
  width: 250px;
  background-color: #fafbfc;
  border-right: 1px solid #e4e7ed;
}

.settings-menu {
  border: none;
  background-color: transparent;
}

.settings-menu .el-menu-item {
  height: 56px;
  line-height: 56px;
  padding-left: 24px;
  font-size: 14px;
  color: #606266;
  border-radius: 0;
}

.settings-menu .el-menu-item:hover {
  background-color: #f0f2f5;
  color: #409eff;
}

.settings-menu .el-menu-item.is-active {
  background-color: #ecf5ff;
  color: #409eff;
  border-right: 3px solid #409eff;
}

.settings-content {
  flex: 1;
  padding: 0;
}

.content-header {
  padding: 32px 32px 24px;
  border-bottom: 1px solid #e4e7ed;
}

.content-header h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 500;
  color: #303133;
}

.content-description {
  margin: 0;
  color: #909399;
  font-size: 14px;
  line-height: 1.5;
}

.settings-panel {
  padding: 32px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .settings-container {
    flex-direction: column;
  }

  .settings-sidebar {
    width: 100%;
  }

  .settings-menu {
    display: flex;
    overflow-x: auto;
  }

  .settings-menu .el-menu-item {
    flex-shrink: 0;
    white-space: nowrap;
  }

  .settings-panel {
    padding: 20px;
  }
}
</style>
