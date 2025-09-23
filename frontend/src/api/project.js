import request from '@/utils/request'

// 获取项目列表
export const getProjects = (params) => {
  return request.get('/api/v1/projects', { params })
}

// 获取我的项目列表
export const getMyProjects = (params) => {
  return request.get('/api/v1/projects/my', { params })
}

// 创建项目
export const createProject = (data) => {
  return request.post('/api/v1/projects', data)
}

// 获取项目详情
export const getProjectById = (id) => {
  return request.get(`/api/v1/projects/${id}`)
}

// 更新项目
export const updateProject = (id, data) => {
  return request.put(`/api/v1/projects/${id}`, data)
}

// 删除项目
export const deleteProject = (id) => {
  return request.delete(`/api/v1/projects/${id}`)
}

// 检查项目名称是否可用
export const checkProjectName = (name) => {
  return request.get('/api/v1/projects/check-name', {
    params: { name }
  })
}

// 获取项目类型选项
export const getProjectTypes = () => {
  return request.get('/api/v1/projects/types')
}

// 更新项目统计信息
export const updateProjectStats = (id) => {
  return request.post(`/api/v1/projects/${id}/stats`)
}