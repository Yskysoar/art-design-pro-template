import request from '@/utils/http'

export function fetchNotifications(params: Api.Notification.NotificationSearchParams) {
  return request.get<Api.Notification.NotificationList>({
    url: '/api/notifications',
    params
  })
}

export function fetchNotificationUnreadCount() {
  return request.get<Api.Notification.NotificationUnreadCount>({
    url: '/api/notifications/unread-count'
  })
}

export function markNotificationRead(id: number) {
  return request.post<void>({
    url: `/api/notifications/${id}/read`
  })
}

export function markAllNotificationsRead() {
  return request.post<void>({
    url: '/api/notifications/read-all'
  })
}
