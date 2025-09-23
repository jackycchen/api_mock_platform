/**
 * 权限控制指令
 * 用法：v-permission="['api:create', 'api:update']"
 * 或者：v-permission:any="['api:create', 'api:update']"  // 有任意一个权限即显示
 * 或者：v-permission:all="['api:create', 'api:update']"  // 需要所有权限才显示
 */
export default {
  name: 'permission',

  mounted(el, binding, vnode) {
    checkPermission(el, binding, vnode)
  },

  updated(el, binding, vnode) {
    checkPermission(el, binding, vnode)
  }
}

function checkPermission(el, binding, vnode) {
  const { value, arg = 'any' } = binding
  const instance = vnode.ctx

  if (!value) {
    return
  }

  // 获取权限列表
  const permissions = Array.isArray(value) ? value : [value]

  // 获取用户权限检查函数
  const hasPermission = instance?.$hasPermission || instance?.proxy?.$hasPermission
  const hasAnyPermission = instance?.$hasAnyPermission || instance?.proxy?.$hasAnyPermission
  const hasAllPermissions = instance?.$hasAllPermissions || instance?.proxy?.$hasAllPermissions

  if (!hasPermission) {
    console.warn('权限检查函数未找到，请确保已正确安装权限插件')
    return
  }

  let allowed = false

  if (arg === 'all') {
    // 需要所有权限
    allowed = hasAllPermissions(permissions)
  } else if (arg === 'any') {
    // 有任意一个权限即可
    allowed = hasAnyPermission(permissions)
  } else {
    // 默认为单个权限检查
    allowed = hasPermission(permissions[0])
  }

  // 控制元素显示/隐藏
  if (!allowed) {
    el.style.display = 'none'
    el.setAttribute('data-permission-hidden', 'true')
  } else {
    if (el.getAttribute('data-permission-hidden') === 'true') {
      el.style.display = ''
      el.removeAttribute('data-permission-hidden')
    }
  }
}
