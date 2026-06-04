import { AppRouteRecord } from '@/types/router'

export const notificationRoutes: AppRouteRecord = {
  path: '/notification',
  name: 'Notification',
  component: '/index/index',
  meta: {
    title: 'menus.notification.title',
    icon: 'ri:notification-3-line',
    isHide: true
  },
  children: [
    {
      path: 'center',
      name: 'NotificationCenter',
      component: '/notification/center',
      meta: {
        title: 'menus.notification.center',
        icon: 'ri:notification-3-line',
        keepAlive: true,
        isHide: true
      }
    }
  ]
}
