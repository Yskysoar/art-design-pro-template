package com.template.notification.controller;

import com.template.common.pagination.PageResult;
import com.template.common.response.ApiResponse;
import com.template.notification.dto.NotificationListQuery;
import com.template.notification.service.NotificationService;
import com.template.notification.vo.NotificationItemVo;
import com.template.notification.vo.NotificationUnreadCountVo;
import com.template.security.auth.AppUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户通知消息接口。
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<PageResult<NotificationItemVo>> list(
            @ModelAttribute NotificationListQuery query,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success(notificationService.pageNotifications(query, principal));
    }

    @GetMapping("/unread-count")
    public ApiResponse<NotificationUnreadCountVo> unreadCount(@AuthenticationPrincipal AppUserPrincipal principal) {
        return ApiResponse.success(notificationService.unreadCount(principal));
    }

    @PostMapping("/{id}/read")
    public ApiResponse<Void> markRead(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        notificationService.markRead(id, principal);
        return ApiResponse.success(null);
    }

    @PostMapping("/read-all")
    public ApiResponse<Void> markAllRead(@AuthenticationPrincipal AppUserPrincipal principal) {
        notificationService.markAllRead(principal);
        return ApiResponse.success(null);
    }
}
