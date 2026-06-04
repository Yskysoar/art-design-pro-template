package com.template.notification.dto;

/**
 * 通知分页查询参数。
 *
 * @param current    当前页码
 * @param size       每页条数
 * @param noticeType 通知类型
 * @param unread     是否只查询未读
 */
public record NotificationListQuery(Long current, Long size, String noticeType, Boolean unread) {
}
