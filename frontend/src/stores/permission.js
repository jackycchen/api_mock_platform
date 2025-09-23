import { defineStore } from 'pinia'
import { getProjectMember } from '@/api/permission'

/**
 * 权限管理Store
 */
export const usePermissionStore = defineStore('permission', {
  state: () => ({
    // 当前用户在各项目中的权限
    projectPermissions: new Map(),
    // 权限定义
    permissionDefinitions: new Map(),
    // 角色权限映射
    rolePermissions: new Map()
  }),

  getters: {
    /**
     * 获取当前项目的用户权限
     */
    getCurrentProjectPermissions() {
      return (projectId) => {
        return this.projectPermissions.get(projectId) || []
      }
    },

    /**
     * 获取当前项目的用户角色
     */
    getCurrentProjectRole() {
      return (projectId) => {
        const permissions = this.projectPermissions.get(projectId)
        return permissions?.role || null
      }
    }
  },

  actions: {
    /**
     * 初始化权限定义
     */
    initializePermissionDefinitions() {
      const permissions = new Map([
        // 项目管理权限
        ['project:manage', { name: '项目管理', category: 'project', description: '管理项目基本信息' }],
        ['project:delete', { name: '删除项目', category: 'project', description: '删除项目' }],
        ['project:settings', { name: '项目设置', category: 'project', description: '修改项目设置' }],

        // 成员管理权限
        ['member:manage', { name: '成员管理', category: 'member', description: '管理项目成员' }],
        ['member:invite', { name: '邀请成员', category: 'member', description: '邀请新成员加入项目' }],
        ['member:remove', { name: '移除成员', category: 'member', description: '从项目中移除成员' }],
        ['member:role', { name: '角色管理', category: 'member', description: '修改成员角色' }],

        // API接口权限
        ['api:create', { name: '创建接口', category: 'api', description: '创建新的API接口' }],
        ['api:read', { name: '查看接口', category: 'api', description: '查看API接口信息' }],
        ['api:update', { name: '修改接口', category: 'api', description: '修改API接口信息' }],
        ['api:delete', { name: '删除接口', category: 'api', description: '删除API接口' }],
        ['api:test', { name: '测试接口', category: 'api', description: '测试API接口' }],

        // Mock规则权限
        ['mock:create', { name: '创建Mock', category: 'mock', description: '创建Mock规则' }],
        ['mock:read', { name: '查看Mock', category: 'mock', description: '查看Mock规则' }],
        ['mock:update', { name: '修改Mock', category: 'mock', description: '修改Mock规则' }],
        ['mock:delete', { name: '删除Mock', category: 'mock', description: '删除Mock规则' }],
        ['mock:test', { name: '测试Mock', category: 'mock', description: '测试Mock规则' }],

        // 文档权限
        ['doc:read', { name: '查看文档', category: 'doc', description: '查看API文档' }],
        ['doc:export', { name: '导出文档', category: 'doc', description: '导出API文档' }],
        ['doc:generate', { name: '生成文档', category: 'doc', description: '生成API文档' }],

        // 统计权限
        ['stats:view', { name: '查看统计', category: 'stats', description: '查看项目统计信息' }],
        ['stats:export', { name: '导出统计', category: 'stats', description: '导出统计数据' }]
      ])

      this.permissionDefinitions = permissions
    },

    /**
     * 初始化角色权限映射
     */
    initializeRolePermissions() {
      const rolePermissions = new Map([
        ['OWNER', [
          'project:manage', 'project:delete', 'project:settings',
          'member:manage', 'member:invite', 'member:remove', 'member:role',
          'api:create', 'api:read', 'api:update', 'api:delete', 'api:test',
          'mock:create', 'mock:read', 'mock:update', 'mock:delete', 'mock:test',
          'doc:read', 'doc:export', 'doc:generate', 'stats:view', 'stats:export'
        ]],
        ['ADMIN', [
          'project:settings', 'member:invite', 'member:remove', 'member:role',
          'api:create', 'api:read', 'api:update', 'api:delete', 'api:test',
          'mock:create', 'mock:read', 'mock:update', 'mock:delete', 'mock:test',
          'doc:read', 'doc:export', 'doc:generate', 'stats:view'
        ]],
        ['DEVELOPER', [
          'api:create', 'api:read', 'api:update', 'api:delete', 'api:test',
          'mock:create', 'mock:read', 'mock:update', 'mock:delete', 'mock:test',
          'doc:read', 'doc:export'
        ]],
        ['VIEWER', [
          'api:read', 'mock:read', 'doc:read'
        ]]
      ])

      this.rolePermissions = rolePermissions
    },

    /**
     * 加载项目权限
     */
    async loadProjectPermissions(projectId) {
      try {
        const response = await getProjectMember(projectId)
        const member = response.data

        if (member) {
          const role = member.role
          const permissions = this.rolePermissions.get(role) || []

          this.projectPermissions.set(projectId, {
            role,
            permissions,
            member
          })

          return { role, permissions }
        }

        return null
      } catch (error) {
        console.error('加载项目权限失败:', error)
        return null
      }
    },

    /**
     * 检查单个权限
     */
    hasPermission(projectId, permission) {
      const projectPerms = this.projectPermissions.get(projectId)
      if (!projectPerms) return false

      return projectPerms.permissions.includes(permission)
    },

    /**
     * 检查是否有任意一个权限
     */
    hasAnyPermission(projectId, permissions) {
      if (!Array.isArray(permissions)) {
        permissions = [permissions]
      }

      return permissions.some(permission => this.hasPermission(projectId, permission))
    },

    /**
     * 检查是否有所有权限
     */
    hasAllPermissions(projectId, permissions) {
      if (!Array.isArray(permissions)) {
        permissions = [permissions]
      }

      return permissions.every(permission => this.hasPermission(projectId, permission))
    },

    /**
     * 检查角色等级
     */
    hasRoleLevel(projectId, requiredLevel) {
      const projectPerms = this.projectPermissions.get(projectId)
      if (!projectPerms) return false

      const roleLevels = {
        'OWNER': 4,
        'ADMIN': 3,
        'DEVELOPER': 2,
        'VIEWER': 1
      }

      const currentLevel = roleLevels[projectPerms.role] || 0
      return currentLevel >= requiredLevel
    },

    /**
     * 获取角色显示名称
     */
    getRoleDisplayName(role) {
      const roleNames = {
        'OWNER': '所有者',
        'ADMIN': '管理员',
        'DEVELOPER': '开发者',
        'VIEWER': '观察者'
      }
      return roleNames[role] || role
    },

    /**
     * 获取权限显示名称
     */
    getPermissionDisplayName(permission) {
      const permissionDef = this.permissionDefinitions.get(permission)
      return permissionDef?.name || permission
    },

    /**
     * 清除项目权限缓存
     */
    clearProjectPermissions(projectId) {
      if (projectId) {
        this.projectPermissions.delete(projectId)
      } else {
        this.projectPermissions.clear()
      }
    },

    /**
     * 刷新项目权限
     */
    async refreshProjectPermissions(projectId) {
      this.clearProjectPermissions(projectId)
      return await this.loadProjectPermissions(projectId)
    }
  }
})
