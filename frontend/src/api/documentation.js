import request from '@/utils/request'

// 获取项目OpenAPI文档
export const getOpenApiDocument = (projectId) => {
  return request.get(`/api/v1/documentation/${projectId}/openapi.json`)
}

// 获取Swagger UI页面
export const getSwaggerUI = (projectId) => {
  return request.get(`/api/v1/documentation/${projectId}/swagger-ui`)
}

// 导出HTML文档
export const exportHtmlDocument = (projectId) => {
  return request.get(`/api/v1/documentation/${projectId}/export/html`, {
    responseType: 'blob'
  })
}

// 导出PDF文档
export const exportPdfDocument = (projectId) => {
  return request.get(`/api/v1/documentation/${projectId}/export/pdf`, {
    responseType: 'blob'
  })
}

// 获取文档统计信息
export const getDocumentationStats = (projectId) => {
  return request.get(`/api/v1/documentation/${projectId}/stats`)
}

// 刷新文档缓存
export const refreshDocumentation = (projectId) => {
  return request.post(`/api/v1/documentation/${projectId}/refresh`)
}

// 在线测试API接口
export const testApiEndpoint = (projectId, testData) => {
  return request.post(`/api/v1/documentation/${projectId}/test`, testData)
}