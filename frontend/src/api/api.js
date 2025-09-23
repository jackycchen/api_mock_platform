import request from '@/utils/request'

// 获取项目API列表
export const getProjectApis = (params) => {
  return request.get('/api/v1/apis', { params })
}

// 获取分组API列表
export const getGroupApis = (groupId, params) => {
  return request.get(`/api/v1/apis/group/${groupId}`, { params })
}

// 创建API接口
export const createApi = (data) => {
  return request.post('/api/v1/apis', data)
}

// 获取API接口详情
export const getApiById = (id) => {
  return request.get(`/api/v1/apis/${id}`)
}

// 更新API接口
export const updateApi = (id, data) => {
  return request.put(`/api/v1/apis/${id}`, data)
}

// 删除API接口
export const deleteApi = (id) => {
  return request.delete(`/api/v1/apis/${id}`)
}

// 检查API路径和方法唯一性
export const checkApiUniqueness = (params) => {
  return request.get('/api/v1/apis/check-uniqueness', { params })
}

// 批量删除API接口
export const batchDeleteApis = (apiIds) => {
  return request.delete('/api/v1/apis/batch', { data: apiIds })
}

// 批量移动API接口到分组
export const batchMoveApisToGroup = (data) => {
  return request.put('/api/v1/apis/batch/move', data)
}

// 更新API接口排序
export const updateApiSortOrder = (id, data) => {
  return request.put(`/api/v1/apis/${id}/sort`, data)
}

// 复制API接口
export const copyApi = (id, data) => {
  return request.post(`/api/v1/apis/${id}/copy`, data)
}

// 获取API统计信息
export const getApiStatistics = (params) => {
  return request.get('/api/v1/apis/statistics', { params })
}