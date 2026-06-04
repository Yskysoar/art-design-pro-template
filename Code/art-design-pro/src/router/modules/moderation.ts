import { AppRouteRecord } from '@/types/router'

export const moderationRoutes: AppRouteRecord = {
  path: '/moderation',
  name: 'Moderation',
  component: '/index/index',
  meta: {
    title: 'menus.moderation.title',
    icon: 'ri:shield-check-line',
    roles: ['R_SUPER', 'R_CONTENT_ADMIN']
  },
  children: [
    {
      path: 'reports',
      name: 'ContentReport',
      component: '/moderation/reports',
      meta: {
        title: 'menus.moderation.report',
        icon: 'ri:alarm-warning-line',
        keepAlive: true,
        roles: ['R_SUPER', 'R_CONTENT_ADMIN'],
        authList: [{ title: '管理举报审核', authMark: 'moderation:report:manage' }]
      }
    },
    {
      path: 'sensitive-word',
      name: 'SensitiveWord',
      component: '/article/comment',
      meta: {
        title: 'menus.moderation.sensitiveWord',
        icon: 'ri:shield-keyhole-line',
        keepAlive: false,
        roles: ['R_SUPER', 'R_CONTENT_ADMIN'],
        authList: [{ title: '管理敏感词', authMark: 'system:sensitive-word' }]
      }
    }
  ]
}
