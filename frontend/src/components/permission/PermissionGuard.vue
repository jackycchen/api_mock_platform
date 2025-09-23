<template>
  <span v-if="hasPermissionToShow">
    <slot />
  </span>
</template>

<script setup>
import { computed } from 'vue'
import { usePermissionStore } from '@/stores/permission'
import { useRoute } from 'vue-router'

/**
 * 权限控制组件
 * 用法：
 * <PermissionGuard permission="api:create">
 *   <el-button>创建API</el-button>
 * </PermissionGuard>
 *
 * <PermissionGuard :permissions="['api:create', 'api:update']" check-type="any">
 *   <el-button>操作API</el-button>
 * </PermissionGuard>
 */

const props = defineProps({
  // 单个权限
  permission: {
    type: String,
    default: ''
  },
  // 多个权限
  permissions: {
    type: Array,
    default: () => []
  },
  // 检查类型：any(任意一个) 或 all(所有)
  checkType: {
    type: String,
    default: 'any',
    validator: (value) => ['any', 'all'].includes(value)
  },
  // 角色要求
  role: {
    type: String,
    default: ''
  },
  // 角色等级要求
  roleLevel: {
    type: Number,
    default: 0
  },
  // 项目ID（如果不提供，从路由获取）
  projectId: {
    type: [String, Number],
    default: null
  },
  // 是否需要项目成员身份
  requireProjectMember: {
    type: Boolean,
    default: true
  }
})

const permissionStore = usePermissionStore()
const route = useRoute()

// 获取项目ID
const currentProjectId = computed(() => {
  if (props.projectId) {
    return typeof props.projectId === 'string' ? parseInt(props.projectId) : props.projectId
  }
  return parseInt(route.params.projectId) || null
})

// 获取要检查的权限列表
const permissionsToCheck = computed(() => {
  if (props.permission) {
    return [props.permission]
  }
  return props.permissions || []
})

// 检查是否有权限显示
const hasPermissionToShow = computed(() => {
  // 如果不需要项目成员身份，直接通过
  if (!props.requireProjectMember) {
    return true
  }

  // 如果没有项目ID，不显示
  if (!currentProjectId.value) {
    return false
  }

  // 检查角色要求
  if (props.role) {
    const currentRole = permissionStore.getCurrentProjectRole(currentProjectId.value)
    if (currentRole !== props.role) {
      return false
    }
  }

  // 检查角色等级要求
  if (props.roleLevel > 0) {
    if (!permissionStore.hasRoleLevel(currentProjectId.value, props.roleLevel)) {
      return false
    }
  }

  // 检查权限要求
  if (permissionsToCheck.value.length > 0) {
    if (props.checkType === 'all') {
      return permissionStore.hasAllPermissions(currentProjectId.value, permissionsToCheck.value)
    } else {
      return permissionStore.hasAnyPermission(currentProjectId.value, permissionsToCheck.value)
    }
  }

  // 如果没有任何要求，默认显示
  return true
})
</script>
