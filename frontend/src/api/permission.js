import request from '@/utils/request'

// 获取所有权限
export const getAllPermissions = () => {
  return request.get('/api/v1/permissions')
}

// 根据分类获取权限
export const getPermissionsByCategory = (category) => {
  return request.get(`/api/v1/permissions/category/${category}`)
}

// 获取所有权限分类
export const getAllCategories = () => {
  return request.get('/api/v1/permissions/categories')
}

// 获取角色的权限
export const getRolePermissions = (role) => {
  return request.get(`/api/v1/permissions/role/${role}`)
}

// 检查角色权限
export const checkPermission = (role, permission) => {
  return request.get('/api/v1/permissions/check', {
    params: { role, permission }
  })
}

// 为角色添加权限
export const addPermissionToRole = (role, permissionCode) => {
  return request.post(`/api/v1/permissions/role/${role}/add`, null, {
    params: { permissionCode }
  })
}

// 从角色移除权限
export const removePermissionFromRole = (role, permissionCode) => {
  return request.post(`/api/v1/permissions/role/${role}/remove`, null, {
    params: { permissionCode }
  })
}

// 重置角色权限
export const resetRolePermissions = (role, permissionCodes) => {
  return request.post(`/api/v1/permissions/role/${role}/reset`, permissionCodes)
}

// 创建新权限
export const createPermission = (data) => {
  return request.post('/api/v1/permissions', data)
}

// 更新权限
export const updatePermission = (id, data) => {
  return request.put(`/api/v1/permissions/${id}`, data)
}

// 启用/禁用权限
export const togglePermission = (id, enabled) => {
  return request.patch(`/api/v1/permissions/${id}/toggle`, null, {
    params: { enabled }
  })
}

// 删除权限
export const deletePermission = (id) => {
  return request.delete(`/api/v1/permissions/${id}`)
}

// 搜索权限
export const searchPermissions = (keyword) => {
  return request.get('/api/v1/permissions/search', {
    params: { keyword }
  })
}

// 获取用户操作日志
export const getUserOperationLogs = (userId, page = 0, size = 20) => {
  return request.get(`/api/v1/permissions/logs/user/${userId}`, {
    params: { page, size }
  })
}

// 获取资源操作日志
export const getResourceOperationLogs = (resource, resourceId, page = 0, size = 20) => {
  return request.get('/api/v1/permissions/logs/resource', {
    params: { resource, resourceId, page, size }
  })
}

// 获取操作统计
export const getOperationStats = (days) => {
  return request.get('/api/v1/permissions/logs/stats', {
    params: { days }
  })
}

// 获取项目成员信息
export const getProjectMember = (projectId) => {
  return request.get(`/api/v1/project-members/current/${projectId}`)
}

// 获取用户角色
export const getUserRole = (projectId) => {
  return request.get(`/api/v1/project-members/role/${projectId}`)
}