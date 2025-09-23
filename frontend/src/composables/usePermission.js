import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { usePermissionStore } from '@/stores/permission'

/**
 * 权限控制 Composable
 * 提供权限检查的组合式函数
 */
export function usePermission() {
  const permissionStore = usePermissionStore()
  const route = useRoute()

  // 获取当前项目ID
  const currentProjectId = computed(() => {
    return parseInt(route.params.projectId) || null
  })

  // 获取当前用户在项目中的角色
  const currentRole = computed(() => {
    if (!currentProjectId.value) return null
    return permissionStore.getCurrentProjectRole(currentProjectId.value)
  })

  // 获取当前用户在项目中的权限列表
  const currentPermissions = computed(() => {
    if (!currentProjectId.value) return []
    return permissionStore.getCurrentProjectPermissions(currentProjectId.value)
  })

  /**
   * 检查单个权限
   * @param {string} permission - 权限代码
   * @param {number} projectId - 项目ID（可选，默认使用当前项目）
   * @returns {boolean}
   */
  const hasPermission = (permission, projectId = null) => {
    const id = projectId || currentProjectId.value
    if (!id) return false
    return permissionStore.hasPermission(id, permission)
  }

  /**
   * 检查是否有任意一个权限
   * @param {string|string[]} permissions - 权限代码或权限代码数组
   * @param {number} projectId - 项目ID（可选，默认使用当前项目）
   * @returns {boolean}
   */
  const hasAnyPermission = (permissions, projectId = null) => {
    const id = projectId || currentProjectId.value
    if (!id) return false

    const perms = Array.isArray(permissions) ? permissions : [permissions]
    return permissionStore.hasAnyPermission(id, perms)
  }

  /**
   * 检查是否有所有权限
   * @param {string|string[]} permissions - 权限代码或权限代码数组
   * @param {number} projectId - 项目ID（可选，默认使用当前项目）
   * @returns {boolean}
   */
  const hasAllPermissions = (permissions, projectId = null) => {
    const id = projectId || currentProjectId.value
    if (!id) return false

    const perms = Array.isArray(permissions) ? permissions : [permissions]
    return permissionStore.hasAllPermissions(id, perms)
  }

  /**
   * 检查角色
   * @param {string} role - 角色代码
   * @param {number} projectId - 项目ID（可选，默认使用当前项目）
   * @returns {boolean}
   */
  const hasRole = (role, projectId = null) => {
    const id = projectId || currentProjectId.value
    if (!id) return false

    const userRole = permissionStore.getCurrentProjectRole(id)
    return userRole === role
  }

  /**
   * 检查角色等级
   * @param {number} level - 所需等级
   * @param {number} projectId - 项目ID（可选，默认使用当前项目）
   * @returns {boolean}
   */
  const hasRoleLevel = (level, projectId = null) => {
    const id = projectId || currentProjectId.value
    if (!id) return false
    return permissionStore.hasRoleLevel(id, level)
  }

  /**
   * 是否是项目所有者
   * @param {number} projectId - 项目ID（可选，默认使用当前项目）
   * @returns {boolean}
   */
  const isOwner = (projectId = null) => {
    return hasRole('OWNER', projectId)
  }

  /**
   * 是否是项目管理员（所有者或管理员）
   * @param {number} projectId - 项目ID（可选，默认使用当前项目）
   * @returns {boolean}
   */
  const isAdmin = (projectId = null) => {
    return hasAnyPermission(['OWNER', 'ADMIN'], projectId)
  }

  /**
   * 是否可以管理指定角色
   * @param {string} targetRole - 目标角色
   * @param {number} projectId - 项目ID（可选，默认使用当前项目）
   * @returns {boolean}
   */
  const canManageRole = (targetRole, projectId = null) => {
    const id = projectId || currentProjectId.value
    if (!id) return false

    const userRole = permissionStore.getCurrentProjectRole(id)
    if (!userRole) return false

    const roleLevels = {
      'OWNER': 4,
      'ADMIN': 3,
      'DEVELOPER': 2,
      'VIEWER': 1
    }

    const currentLevel = roleLevels[userRole] || 0
    const targetLevel = roleLevels[targetRole] || 0

    return currentLevel > targetLevel
  }

  /**
   * 加载项目权限
   * @param {number} projectId - 项目ID
   * @returns {Promise}
   */
  const loadProjectPermissions = async (projectId) => {
    return await permissionStore.loadProjectPermissions(projectId)
  }

  /**
   * 刷新当前项目权限
   * @returns {Promise}
   */
  const refreshCurrentProjectPermissions = async () => {
    if (!currentProjectId.value) return null
    return await permissionStore.refreshProjectPermissions(currentProjectId.value)
  }

  /**
   * 获取角色显示名称
   * @param {string} role - 角色代码
   * @returns {string}
   */
  const getRoleDisplayName = (role) => {
    return permissionStore.getRoleDisplayName(role)
  }

  /**
   * 获取权限显示名称
   * @param {string} permission - 权限代码
   * @returns {string}
   */
  const getPermissionDisplayName = (permission) => {
    return permissionStore.getPermissionDisplayName(permission)
  }

  return {
    // 状态
    currentProjectId,
    currentRole,
    currentPermissions,

    // 权限检查方法
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    hasRole,
    hasRoleLevel,
    isOwner,
    isAdmin,
    canManageRole,

    // 数据加载方法
    loadProjectPermissions,
    refreshCurrentProjectPermissions,

    // 工具方法
    getRoleDisplayName,
    getPermissionDisplayName
  }
}