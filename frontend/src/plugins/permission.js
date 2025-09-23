import permissionDirective from '@/directives/permission'
import { usePermissionStore } from '@/stores/permission'

/**
 * 权限控制插件
 */
export default {
  install(app) {
    // 注册权限指令
    app.directive('permission', permissionDirective)

    // 全局权限检查方法
    app.config.globalProperties.$hasPermission = (permission, projectId = null) => {
      const route = app.config.globalProperties.$route
      const id = projectId || parseInt(route?.params?.projectId) || null
      if (!id) return false

      const permissionStore = usePermissionStore()
      return permissionStore.hasPermission(id, permission)
    }

    app.config.globalProperties.$hasAnyPermission = (permissions, projectId = null) => {
      const route = app.config.globalProperties.$route
      const id = projectId || parseInt(route?.params?.projectId) || null
      if (!id) return false

      const perms = Array.isArray(permissions) ? permissions : [permissions]
      const permissionStore = usePermissionStore()
      return permissionStore.hasAnyPermission(id, perms)
    }

    app.config.globalProperties.$hasAllPermissions = (permissions, projectId = null) => {
      const route = app.config.globalProperties.$route
      const id = projectId || parseInt(route?.params?.projectId) || null
      if (!id) return false

      const perms = Array.isArray(permissions) ? permissions : [permissions]
      const permissionStore = usePermissionStore()
      return permissionStore.hasAllPermissions(id, perms)
    }

    app.config.globalProperties.$hasRole = (role, projectId = null) => {
      const route = app.config.globalProperties.$route
      const id = projectId || parseInt(route?.params?.projectId) || null
      if (!id) return false

      const permissionStore = usePermissionStore()
      const userRole = permissionStore.getCurrentProjectRole(id)
      return userRole === role
    }

    app.config.globalProperties.$isOwner = (projectId = null) => {
      return app.config.globalProperties.$hasRole('OWNER', projectId)
    }

    app.config.globalProperties.$isAdmin = (projectId = null) => {
      const route = app.config.globalProperties.$route
      const id = projectId || parseInt(route?.params?.projectId) || null
      if (!id) return false

      const permissionStore = usePermissionStore()
      const userRole = permissionStore.getCurrentProjectRole(id)
      return ['OWNER', 'ADMIN'].includes(userRole)
    }

    // 初始化权限Store
    const permissionStore = usePermissionStore()
    permissionStore.initializePermissionDefinitions()
    permissionStore.initializeRolePermissions()
  }
}