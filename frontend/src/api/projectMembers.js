import request from '@/utils/request'

/**
 * 获取项目成员列表
 */
export const getProjectMembers = (projectId, params) => {
  return request.get(`/api/v1/projects/${projectId}/members`, { params })
}

/**
 * 邀请成员加入项目
 */
export const inviteProjectMember = (projectId, data) => {
  return request.post(`/api/v1/projects/${projectId}/members/invite`, data)
}

/**
 * 更新成员角色
 */
export const updateMemberRole = (projectId, memberId, data) => {
  return request.put(`/api/v1/projects/${projectId}/members/${memberId}/role`, data)
}

/**
 * 移除项目成员
 */
export const removeProjectMember = (projectId, memberId) => {
  return request.delete(`/api/v1/projects/${projectId}/members/${memberId}`)
}

/**
 * 获取项目角色列表
 */
export const getProjectRoles = () => {
  return request.get('/api/v1/project-roles')
}

/**
 * 搜索用户（用于邀请）
 */
export const searchUsers = (keyword) => {
  return request.get('/api/v1/users/search', {
    params: { keyword }
  })
}

/**
 * 获取邀请链接
 */
export const getInviteLink = (projectId, role) => {
  return request.post(`/api/v1/projects/${projectId}/members/invite-link`, { role })
}

/**
 * 通过邀请链接加入项目
 */
export const joinProjectByInvite = (token) => {
  return request.post('/api/v1/projects/join', { token })
}

/**
 * 获取成员操作日志
 */
export const getMemberLogs = (projectId, params) => {
  return request.get(`/api/v1/projects/${projectId}/members/logs`, { params })
}

/**
 * 批量更新成员角色
 */
export const batchUpdateMemberRoles = (projectId, data) => {
  return request.put(`/api/v1/projects/${projectId}/members/batch-update`, data)
}

/**
 * 离开项目
 */
export const leaveProject = (projectId) => {
  return request.post(`/api/v1/projects/${projectId}/members/leave`)
}