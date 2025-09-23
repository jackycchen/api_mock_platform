import request from '@/utils/request'

// 获取API的Mock规则列表
export const getApiMockRules = (apiId) => {
  return request.get(`/api/v1/mock-rules/api/${apiId}`)
}

// 获取项目的Mock规则列表
export const getProjectMockRules = (params) => {
  return request.get(`/api/v1/mock-rules/project/${params.projectId}`, { params })
}

// 创建Mock规则
export const createMockRule = (data) => {
  return request.post('/api/v1/mock-rules', data)
}

// 获取Mock规则详情
export const getMockRuleById = (id) => {
  return request.get(`/api/v1/mock-rules/${id}`)
}

// 更新Mock规则
export const updateMockRule = (id, data) => {
  return request.put(`/api/v1/mock-rules/${id}`, data)
}

// 删除Mock规则
export const deleteMockRule = (id) => {
  return request.delete(`/api/v1/mock-rules/${id}`)
}

// 启用/禁用Mock规则
export const toggleMockRuleStatus = (id, isEnabled) => {
  return request.put(`/api/v1/mock-rules/${id}/status`, { isEnabled })
}

// 批量更新Mock规则状态
export const batchUpdateMockRuleStatus = (data) => {
  return request.put('/api/v1/mock-rules/batch/status', data)
}

// 复制Mock规则
export const copyMockRule = (id, newName) => {
  return request.post(`/api/v1/mock-rules/${id}/copy`, { newName })
}

// 预览Mock数据
export const previewMockData = (id) => {
  return request.get(`/api/v1/mock-rules/${id}/preview`)
}

// 测试Mock规则
export const testMockRule = (data) => {
  return request.post('/api/v1/mock-rules/test', data)
}

// 获取Mock规则统计信息
export const getMockRuleStatistics = (projectId) => {
  return request.get('/api/v1/mock-rules/statistics', { params: { projectId } })
}

// Mock服务相关接口

// 获取Mock服务信息
export const getMockServiceInfo = (projectId) => {
  return request.get(`/mock/${projectId}/_info`)
}

// 健康检查
export const healthCheck = () => {
  return request.get('/mock/_health')
}