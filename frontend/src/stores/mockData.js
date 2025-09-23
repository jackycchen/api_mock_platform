import { defineStore } from 'pinia'
import * as mockDataApi from '@/api/mockData'

export const useMockDataStore = defineStore('mockData', {
  state: () => ({
    // 缓存管理
    cacheStats: {
      dataKeyCount: 0,
      ruleKeyCount: 0,
      callCountKeyCount: 0
    },
    apiCallCounts: new Map(),
    apiLastCallTimes: new Map(),

    // 统计分析
    apiStats: null,
    projectStats: null,
    globalStats: null,
    responseTimeDistribution: {},
    callLogs: {
      content: [],
      totalElements: 0,
      totalPages: 0,
      number: 0,
      size: 20
    },

    // 版本管理
    versionList: [],
    versionStats: {
      totalApis: 0,
      totalVersions: 0
    },
    versionComparison: null,

    // 加载状态
    loading: {
      cacheStats: false,
      apiStats: false,
      projectStats: false,
      globalStats: false,
      callLogs: false,
      versions: false,
      comparison: false
    }
  }),

  getters: {
    // 缓存统计
    totalCacheKeys: (state) => state.cacheStats.dataKeyCount + state.cacheStats.ruleKeyCount + state.cacheStats.callCountKeyCount,

    // 统计分析
    apiSuccessRate: (state) => {
      if (!state.apiStats || state.apiStats.totalCalls === 0) return 0
      return ((state.apiStats.successCalls / state.apiStats.totalCalls) * 100).toFixed(2)
    },

    projectSuccessRate: (state) => {
      if (!state.projectStats || state.projectStats.totalCalls === 0) return 0
      return ((state.projectStats.successCalls / state.projectStats.totalCalls) * 100).toFixed(2)
    },

    // 版本管理
    averageVersionsPerApi: (state) => {
      if (state.versionStats.totalApis === 0) return 0
      return (state.versionStats.totalVersions / state.versionStats.totalApis).toFixed(2)
    }
  },

  actions: {
    // ========== 缓存管理 ==========
    async fetchCacheStats() {
      this.loading.cacheStats = true
      try {
        const response = await mockDataApi.getCacheStats()
        this.cacheStats = response.data
      } catch (error) {
        console.error('获取缓存统计失败:', error)
        throw error
      } finally {
        this.loading.cacheStats = false
      }
    },

    async fetchApiCallCount(apiId) {
      try {
        const response = await mockDataApi.getCallCount(apiId)
        this.apiCallCounts.set(apiId, response.data)
        return response.data
      } catch (error) {
        console.error('获取API调用次数失败:', error)
        return 0
      }
    },

    async fetchApiLastCallTime(apiId) {
      try {
        const response = await mockDataApi.getLastCallTime(apiId)
        const timestamp = response.data
        this.apiLastCallTimes.set(apiId, timestamp)
        return timestamp ? new Date(timestamp) : null
      } catch (error) {
        console.error('获取API最后调用时间失败:', error)
        return null
      }
    },

    async clearMockCache(apiId) {
      try {
        await mockDataApi.clearMockDataCache(apiId)
        // 更新缓存统计
        await this.fetchCacheStats()
        // 清除本地缓存的调用次数和时间
        this.apiCallCounts.delete(apiId)
        this.apiLastCallTimes.delete(apiId)
      } catch (error) {
        console.error('清除Mock缓存失败:', error)
        throw error
      }
    },

    async batchClearMockCache(apiIds) {
      try {
        await mockDataApi.batchClearMockCache(apiIds)
        // 更新缓存统计
        await this.fetchCacheStats()
        // 清除本地缓存
        apiIds.forEach(apiId => {
          this.apiCallCounts.delete(apiId)
          this.apiLastCallTimes.delete(apiId)
        })
      } catch (error) {
        console.error('批量清除Mock缓存失败:', error)
        throw error
      }
    },

    // ========== 统计分析 ==========
    async fetchApiStats(apiId, startTime, endTime) {
      this.loading.apiStats = true
      try {
        const response = await mockDataApi.getApiCallStats(apiId, startTime, endTime)
        this.apiStats = response.data
      } catch (error) {
        console.error('获取API统计失败:', error)
        throw error
      } finally {
        this.loading.apiStats = false
      }
    },

    async fetchProjectStats(projectId, startTime, endTime) {
      this.loading.projectStats = true
      try {
        const response = await mockDataApi.getProjectCallStats(projectId, startTime, endTime)
        this.projectStats = response.data
      } catch (error) {
        console.error('获取项目统计失败:', error)
        throw error
      } finally {
        this.loading.projectStats = false
      }
    },

    async fetchGlobalStats(startTime, endTime) {
      this.loading.globalStats = true
      try {
        const response = await mockDataApi.getGlobalCallStats(startTime, endTime)
        this.globalStats = response.data
      } catch (error) {
        console.error('获取全局统计失败:', error)
        throw error
      } finally {
        this.loading.globalStats = false
      }
    },

    async fetchResponseTimeDistribution(apiId, startTime, endTime) {
      try {
        const response = await mockDataApi.getResponseTimeDistribution(apiId, startTime, endTime)
        this.responseTimeDistribution = response.data
      } catch (error) {
        console.error('获取响应时间分布失败:', error)
        throw error
      }
    },

    async fetchCallLogs(params = {}) {
      this.loading.callLogs = true
      try {
        const response = await mockDataApi.getCallLogs({
          page: 0,
          size: 20,
          ...params
        })
        this.callLogs = response.data
      } catch (error) {
        console.error('获取调用日志失败:', error)
        throw error
      } finally {
        this.loading.callLogs = false
      }
    },

    async cleanupExpiredLogs(beforeTime) {
      try {
        const response = await mockDataApi.cleanupExpiredLogs(beforeTime)
        return response.data // 返回删除的记录数
      } catch (error) {
        console.error('清理过期日志失败:', error)
        throw error
      }
    },

    // ========== 版本管理 ==========
    async fetchVersionList(apiId) {
      this.loading.versions = true
      try {
        const response = await mockDataApi.getMockRuleVersions(apiId)
        this.versionList = response.data
      } catch (error) {
        console.error('获取版本列表失败:', error)
        throw error
      } finally {
        this.loading.versions = false
      }
    },

    async fetchVersionStats() {
      try {
        const response = await mockDataApi.getVersionStats()
        this.versionStats = response.data
      } catch (error) {
        console.error('获取版本统计失败:', error)
        throw error
      }
    },

    async rollbackToVersion(apiId, versionId, operator) {
      try {
        const response = await mockDataApi.rollbackToVersion(apiId, versionId, operator)
        if (response.data) {
          // 回滚成功，重新加载版本列表
          await this.fetchVersionList(apiId)
        }
        return response.data
      } catch (error) {
        console.error('版本回滚失败:', error)
        throw error
      }
    },

    async compareVersions(apiId, version1, version2) {
      this.loading.comparison = true
      try {
        const response = await mockDataApi.compareVersions(apiId, version1, version2)
        this.versionComparison = response.data
      } catch (error) {
        console.error('版本比较失败:', error)
        throw error
      } finally {
        this.loading.comparison = false
      }
    },

    async deleteVersion(apiId, versionId) {
      try {
        const response = await mockDataApi.deleteVersion(apiId, versionId)
        if (response.data) {
          // 删除成功，重新加载版本列表
          await this.fetchVersionList(apiId)
        }
        return response.data
      } catch (error) {
        console.error('删除版本失败:', error)
        throw error
      }
    },

    async cleanupApiVersions(apiId) {
      try {
        await mockDataApi.cleanupApiVersions(apiId)
        // 清理成功，重新加载版本列表
        await this.fetchVersionList(apiId)
      } catch (error) {
        console.error('清理API版本失败:', error)
        throw error
      }
    },

    // ========== 重置状态 ==========
    resetApiStats() {
      this.apiStats = null
    },

    resetProjectStats() {
      this.projectStats = null
    },

    resetGlobalStats() {
      this.globalStats = null
    },

    resetVersionComparison() {
      this.versionComparison = null
    },

    resetAllStats() {
      this.resetApiStats()
      this.resetProjectStats()
      this.resetGlobalStats()
      this.resetVersionComparison()
      this.responseTimeDistribution = {}
      this.callLogs = {
        content: [],
        totalElements: 0,
        totalPages: 0,
        number: 0,
        size: 20
      }
      this.versionList = []
    }
  }
})