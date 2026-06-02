import { AppRouteRecord } from '@/types/router'

export const systemRoutes: AppRouteRecord = {
  path: '/system',
  name: 'System',
  component: '/index/index',
  meta: {
    title: 'menus.system.title',
    icon: 'ri:user-3-line',
    roles: ['R_SUPER', 'R_ADMIN']
  },
  children: [
    {
      path: 'user',
      name: 'User',
      component: '/system/user',
      meta: {
        title: 'menus.system.user',
        icon: 'ri:user-line',
        keepAlive: true,
        roles: ['R_SUPER', 'R_ADMIN'],
        authList: [
          { title: '新增用户', authMark: 'system:user:add' },
          { title: '编辑用户', authMark: 'system:user:edit' },
          { title: '删除用户', authMark: 'system:user:delete' }
        ]
      }
    },
    {
      path: 'role',
      name: 'Role',
      component: '/system/role',
      meta: {
        title: 'menus.system.role',
        icon: 'ri:user-settings-line',
        keepAlive: true,
        roles: ['R_SUPER'],
        authList: [
          { title: '新增角色', authMark: 'system:role:add' },
          { title: '编辑角色', authMark: 'system:role:edit' },
          { title: '分配角色权限', authMark: 'system:role:permission' }
        ]
      }
    },
    {
      path: 'org',
      name: 'Org',
      component: '/system/org',
      meta: {
        title: '组织管理',
        icon: 'ri:organization-chart',
        keepAlive: true,
        roles: ['R_SUPER'],
        authList: [{ title: '管理组织', authMark: 'system:org:manage' }]
      }
    },
    {
      path: 'config',
      name: 'Config',
      component: '/system/config',
      meta: {
        title: '配置管理',
        icon: 'ri:settings-3-line',
        keepAlive: true,
        roles: ['R_SUPER'],
        authList: [{ title: '管理配置', authMark: 'system:config:manage' }]
      }
    },
    {
      path: 'menu',
      name: 'Menus',
      component: '/system/menu',
      meta: {
        title: 'menus.system.menu',
        icon: 'ri:menu-line',
        keepAlive: true,
        roles: ['R_SUPER'],
        authList: [{ title: '管理菜单', authMark: 'system:menu:manage' }]
      }
    },
    {
      path: 'sensitive-word',
      name: 'SensitiveWord',
      component: '/article/comment',
      meta: {
        title: '敏感词库',
        icon: 'ri:shield-keyhole-line',
        keepAlive: false,
        roles: ['R_SUPER'],
        authList: [{ title: '管理敏感词', authMark: 'system:sensitive-word' }]
      }
    },
    {
      path: 'user-center',
      name: 'UserCenter',
      component: '/system/user-center',
      meta: {
        title: 'menus.system.userCenter',
        icon: 'ri:user-line',
        isHide: true,
        keepAlive: true,
        isHideTab: true
      }
    }
  ]
}
