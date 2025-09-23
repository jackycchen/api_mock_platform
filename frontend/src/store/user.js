import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null')
  }),

  getters: {
    isLoggedIn: (state) => !!state.token && !!state.userInfo,
    user: (state) => state.userInfo,
    username: (state) => state.userInfo?.username || '',
    email: (state) => state.userInfo?.email || '',
    role: (state) => state.userInfo?.role || '',
    roleDisplay: (state) => {
      const roleMap = {
        'SUPER_ADMIN': '超级管理员',
        'PROJECT_ADMIN': '项目管理员',
        'DEVELOPER': '开发者',
        'OBSERVER': '观察者'
      }
      return roleMap[state.userInfo?.role] || '未知角色'
    }
  },

  actions: {
    setToken(token) {
      this.token = token
      localStorage.setItem('token', token)
    },

    setUserInfo(userInfo) {
      this.userInfo = userInfo
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    },

    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    },

    // 初始化用户信息（从localStorage恢复）
    initializeStore() {
      const token = localStorage.getItem('token')
      const userInfo = localStorage.getItem('userInfo')

      if (token) {
        this.token = token
      }

      if (userInfo) {
        try {
          this.userInfo = JSON.parse(userInfo)
        } catch (error) {
          console.error('解析用户信息失败:', error)
          this.logout()
        }
      }
    }
  }
})