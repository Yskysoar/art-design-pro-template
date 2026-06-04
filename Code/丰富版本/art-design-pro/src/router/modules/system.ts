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
        title: 'menus.system.org',
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
        title: 'menus.system.config',
        icon: 'ri:settings-3-line',
        keepAlive: true,
        roles: ['R_SUPER'],
        authList: [{ title: '管理配置', authMark: 'system:config:manage' }]
      }
    },
    {
      path: 'file',
      name: 'FileResource',
      component: '/system/file',
      meta: {
        title: 'menus.system.fileResource',
        icon: 'ri:file-list-3-line',
        keepAlive: true,
        roles: ['R_SUPER'],
        authList: [{ title: '管理文件资源', authMark: 'system:file:manage' }]
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
