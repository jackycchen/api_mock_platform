<template>
  <el-container style="height: 100vh">
    <el-aside
      width="250px"
      style="background-color: #001529"
    >
      <div class="logo">
        <h2 style="color: white; text-align: center; padding: 20px 0">
          API Mock Platform
        </h2>
      </div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#001529"
        text-color="#fff"
        active-text-color="#ffd04b"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>控制台</span>
        </el-menu-item>
        <el-menu-item index="/projects">
          <el-icon><Folder /></el-icon>
          <span>项目管理</span>
        </el-menu-item>
        <el-menu-item index="/mock">
          <el-icon><Setting /></el-icon>
          <span>Mock管理</span>
        </el-menu-item>
        <el-menu-item index="/data">
          <el-icon><Document /></el-icon>
          <span>数据管理</span>
        </el-menu-item>
        <el-menu-item index="/monitor">
          <el-icon><Monitor /></el-icon>
          <span>监控统计</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header style="background-color: #fff; padding: 0; box-shadow: 0 1px 4px rgba(0,21,41,.08);">
        <div style="display: flex; justify-content: space-between; align-items: center; padding: 0 20px; height: 100%">
          <div style="font-size: 18px; font-weight: 500">
            {{ $route.meta.title || '控制台' }}
          </div>
          <div style="display: flex; align-items: center; gap: 16px;">
            <!-- 用户信息显示 -->
            <div style="display: flex; align-items: center; gap: 8px;">
              <el-avatar
                :size="32"
                style="background-color: #409eff;"
              >
                {{ userStore.username.charAt(0).toUpperCase() }}
              </el-avatar>
              <div style="font-size: 14px;">
                <div style="font-weight: 500;">
                  {{ userStore.username }}
                </div>
                <div style="color: #909399; font-size: 12px;">
                  {{ userStore.roleDisplay }}
                </div>
              </div>
            </div>

            <!-- 用户操作下拉菜单 -->
            <el-dropdown @command="handleCommand">
              <span style="cursor: pointer; display: flex; align-items: center;">
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    个人信息
                  </el-dropdown-item>
                  <el-dropdown-item command="settings">
                    <el-icon><Setting /></el-icon>
                    设置
                  </el-dropdown-item>
                  <el-dropdown-item
                    divided
                    command="logout"
                  >
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>

      <el-main style="background-color: #f0f2f5">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { HomeFilled, Folder, Setting, Document, Monitor, ArrowDown, User, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { logout as logoutApi } from '@/api/auth'

const userStore = useUserStore()
const router = useRouter()

const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      ElMessage.info('个人信息功能开发中')
      break
    case 'settings':
      ElMessage.info('设置功能开发中')
      break
    case 'logout':
      await handleLogout()
      break
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 调用后端登出接口
    try {
      await logoutApi()
    } catch (error) {
      console.error('登出接口调用失败:', error)
      // 即使接口失败也继续执行前端登出
    }

    // 清除本地状态
    userStore.logout()

    ElMessage.success('已退出登录')
    router.push('/login')

  } catch (error) {
    // 用户取消了登出操作
  }
}
</script>

<style scoped>
.logo h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}
</style>