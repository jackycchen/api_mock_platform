// 访问统计相关API
import request from '@/utils/request'

/**
 * 获取API访问统计数据
 * @param {Object} params - 统计参数
 * @param {number} params.apiId - API ID
 * @param {string} params.startTime - 开始时间 (yyyy-MM-dd)
 * @param {string} params.endTime - 结束时间 (yyyy-MM-dd)
 * @param {string} params.timeGranularity - 时间粒度 (hour|day|week|month)
 */
export const getApiStatistics = (params) => {
  const { apiId, ...queryParams } = params
  return request.get(`/api/v1/statistics/api/${apiId}`, { params: queryParams })
}

/**
 * 获取项目访问统计概览
 * @param {number} projectId - 项目ID
 */
export const getProjectStatisticsOverview = (projectId) => {
  return request.get(`/api/v1/statistics/project/${projectId}/overview`)
}

/**
 * 获取项目访问统计数据
 * @param {Object} params - 统计参数
 * @param {number} params.projectId - 项目ID
 * @param {string} params.startTime - 开始时间 (yyyy-MM-dd)
 * @param {string} params.endTime - 结束时间 (yyyy-MM-dd)
 * @param {string} params.timeGranularity - 时间粒度 (hour|day|week|month)
 */
export const getProjectStatistics = (params) => {
  const { projectId, ...queryParams } = params
  return request.get(`/api/v1/statistics/project/${projectId}`, { params: queryParams })
}

/**
 * 获取详细的访问统计数据（POST方式）
 * @param {Object} data - 统计请求数据
 */
export const getAccessStatistics = (data) => {
  return request.post('/api/v1/statistics/access', data)
}

/**
 * 获取最近的调用日志
 * @param {number} apiId - API ID
 * @param {number} limit - 限制条数
 */
export const getRecentCallLogs = (apiId, limit = 10) => {
  return request.get(`/api/v1/statistics/api/${apiId}/recent-logs`, { params: { limit } })
}

/**
 * 清理过期日志
 * @param {number} days - 保留天数
 */
export const cleanExpiredLogs = (days = 90) => {
  return request.delete('/api/v1/statistics/logs/cleanup', { params: { days } })
}