import request from '@/utils/request'

// ========== 缓存管理接口 ==========

/**
 * 获取Mock数据缓存
 */
export const getCachedMockData = (apiId) => {
  return request.get(`/api/v1/mock/data/cache/${apiId}`)
}

/**
 * 清除Mock数据缓存
 */
export const clearMockDataCache = (apiId) => {
  return request.delete(`/api/v1/mock/data/cache/${apiId}`)
}

/**
 * 批量清除Mock缓存
 */
export const batchClearMockCache = (apiIds) => {
  return request.delete('/api/v1/mock/data/cache/batch', {
    data: apiIds
  })
}

/**
 * 获取缓存统计信息
 */
export const getCacheStats = () => {
  return request.get('/api/v1/mock/data/cache/stats')
}

/**
 * 获取API调用次数
 */
export const getCallCount = (apiId) => {
  return request.get(`/api/v1/mock/data/cache/${apiId}/call-count`)
}

/**
 * 获取API最后调用时间
 */
export const getLastCallTime = (apiId) => {
  return request.get(`/api/v1/mock/data/cache/${apiId}/last-call-time`)
}

// ========== 统计分析接口 ==========

/**
 * 获取API调用统计
 */
export const getApiCallStats = (apiId, startTime, endTime) => {
  return request.get(`/api/v1/mock/data/stats/api/${apiId}`, {
    params: {
      startTime: startTime.toISOString(),
      endTime: endTime.toISOString()
    }
  })
}

/**
 * 获取项目调用统计
 */
export const getProjectCallStats = (projectId, startTime, endTime) => {
  return request.get(`/api/v1/mock/data/stats/project/${projectId}`, {
    params: {
      startTime: startTime.toISOString(),
      endTime: endTime.toISOString()
    }
  })
}

/**
 * 获取全局调用统计
 */
export const getGlobalCallStats = (startTime, endTime) => {
  return request.get('/api/v1/mock/data/stats/global', {
    params: {
      startTime: startTime.toISOString(),
      endTime: endTime.toISOString()
    }
  })
}

/**
 * 获取响应时间分布
 */
export const getResponseTimeDistribution = (apiId, startTime, endTime) => {
  return request.get(`/api/v1/mock/data/stats/api/${apiId}/response-time-distribution`, {
    params: {
      startTime: startTime.toISOString(),
      endTime: endTime.toISOString()
    }
  })
}

/**
 * 获取调用日志
 */
export const getCallLogs = (params) => {
  return request.get('/api/v1/mock/data/logs', {
    params: {
      ...params,
      startTime: params.startTime ? params.startTime.toISOString() : undefined,
      endTime: params.endTime ? params.endTime.toISOString() : undefined
    }
  })
}

/**
 * 清理过期调用日志
 */
export const cleanupExpiredLogs = (beforeTime) => {
  return request.delete('/api/v1/mock/data/logs/cleanup', {
    params: {
      beforeTime: beforeTime.toISOString()
    }
  })
}

// ========== 版本管理接口 ==========

/**
 * 获取Mock规则版本列表
 */
export const getMockRuleVersions = (apiId) => {
  return request.get(`/api/v1/mock/data/versions/api/${apiId}`)
}

/**
 * 获取指定版本的Mock规则
 */
export const getMockRuleVersion = (apiId, versionId) => {
  return request.get(`/api/v1/mock/data/versions/api/${apiId}/version/${versionId}`)
}

/**
 * 回滚到指定版本
 */
export const rollbackToVersion = (apiId, versionId, operator) => {
  return request.post(`/api/v1/mock/data/versions/api/${apiId}/rollback/${versionId}`, null, {
    params: { operator }
  })
}

/**
 * 比较两个版本的差异
 */
export const compareVersions = (apiId, version1, version2) => {
  return request.get(`/api/v1/mock/data/versions/api/${apiId}/compare`, {
    params: { version1, version2 }
  })
}

/**
 * 删除指定版本
 */
export const deleteVersion = (apiId, versionId) => {
  return request.delete(`/api/v1/mock/data/versions/api/${apiId}/version/${versionId}`)
}

/**
 * 清理API的所有版本
 */
export const cleanupApiVersions = (apiId) => {
  return request.delete(`/api/v1/mock/data/versions/api/${apiId}/cleanup`)
}

/**
 * 获取版本统计信息
 */
export const getVersionStats = () => {
  return request.get('/api/v1/mock/data/versions/stats')
}