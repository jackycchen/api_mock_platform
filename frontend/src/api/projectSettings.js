import request from '@/utils/request'

/**
 * 获取项目设置
 */
export const getProjectSettings = (projectId) => {
  return request.get(`/api/v1/projects/${projectId}/settings`)
}

/**
 * 更新项目基本设置
 */
export const updateProjectBasicSettings = (projectId, data) => {
  return request.put(`/api/v1/projects/${projectId}/settings/basic`, data)
}

/**
 * 更新项目环境设置
 */
export const updateProjectEnvironmentSettings = (projectId, data) => {
  return request.put(`/api/v1/projects/${projectId}/settings/environment`, data)
}

/**
 * 转让项目所有权
 */
export const transferProjectOwnership = (projectId, data) => {
  return request.post(`/api/v1/projects/${projectId}/settings/transfer`, data)
}

/**
 * 永久删除项目
 */
export const permanentDeleteProject = (projectId, confirmPassword) => {
  return request.delete(`/api/v1/projects/${projectId}/settings/permanent`, {
    params: { confirmPassword }
  })
}

/**
 * 恢复项目
 */
export const restoreProject = (projectId) => {
  return request.post(`/api/v1/projects/${projectId}/settings/restore`)
}

/**
 * 检查项目名称可用性
 */
export const checkProjectNameAvailability = (projectId, name) => {
  return request.get(`/api/v1/projects/${projectId}/settings/check-name`, {
    params: { name }
  })
}

/**
 * 生成Mock域名
 */
export const generateMockDomain = (projectId) => {
  return request.post(`/api/v1/projects/${projectId}/settings/generate-mock-domain`)
}

/**
 * 验证用户密码
 */
export const verifyPassword = (projectId, password) => {
  return request.post(`/api/v1/projects/${projectId}/settings/verify-password`, null, {
    params: { password }
  })
}
