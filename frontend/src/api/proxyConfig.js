import request from '@/utils/request'

export const getProxyConfigs = (projectId) => {
  return request.get(`/api/v1/projects/${projectId}/proxy-configs`)
}

export const createProxyConfig = (projectId, data) => {
  return request.post(`/api/v1/projects/${projectId}/proxy-configs`, data)
}

export const updateProxyConfig = (projectId, configId, data) => {
  return request.put(`/api/v1/projects/${projectId}/proxy-configs/${configId}`, data)
}

export const toggleProxyConfig = (projectId, configId, enabled) => {
  return request.post(`/api/v1/projects/${projectId}/proxy-configs/${configId}/toggle`, null, {
    params: { enabled }
  })
}

export const deleteProxyConfig = (projectId, configId) => {
  return request.delete(`/api/v1/projects/${projectId}/proxy-configs/${configId}`)
}
