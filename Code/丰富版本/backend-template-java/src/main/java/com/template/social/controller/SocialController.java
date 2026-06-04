package com.template.social.controller;

import com.template.common.pagination.PageResult;
import com.template.common.response.ApiResponse;
import com.template.file.service.FileStorageService;
import com.template.file.vo.UploadResponse;
import com.template.security.auth.AppUserPrincipal;
import com.template.social.dto.SocialMessageQuery;
import com.template.social.dto.SocialMessageSendRequest;
import com.template.social.dto.SocialPageQuery;
import com.template.social.service.SocialService;
import com.template.social.vo.SocialConversationVo;
import com.template.social.vo.SocialMessageVo;
import com.template.social.vo.SocialUnreadCountVo;
import com.template.social.vo.SocialUserVo;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 社交关注与聊天接口。
 */
@RestController
@RequestMapping("/api/social")
public class SocialController {

    private final SocialService socialService;
    private final FileStorageService fileStorageService;

    public SocialController(SocialService socialService, FileStorageService fileStorageService) {
        this.socialService = socialService;
        this.fileStorageService = fileStorageService;
    }

    /**
     * 搜索可聊天用户。
     *
     * @param query     查询参数
     * @param principal 当前用户
     * @return 用户分页
     */
    @GetMapping("/users")
    public ApiResponse<PageResult<SocialUserVo>> searchUsers(
            @ModelAttribute SocialPageQuery query,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success(socialService.searchUsers(query, principal));
    }

    /**
     * 查询用户社交资料。
     *
     * @param id        目标用户 ID
     * @param principal 当前用户
     * @return 用户资料
     */
    @GetMapping("/users/{id}")
    public ApiResponse<SocialUserVo> getUserProfile(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success(socialService.getUserProfile(id, principal));
    }

    /**
     * 查询我的关注。
     */
    @GetMapping("/following")
    public ApiResponse<PageResult<SocialUserVo>> following(
            @ModelAttribute SocialPageQuery query,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success(socialService.pageFollowing(query, principal));
    }

    /**
     * 查询我的粉丝。
     */
    @GetMapping("/followers")
    public ApiResponse<PageResult<SocialUserVo>> followers(
            @ModelAttribute SocialPageQuery query,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success(socialService.pageFollowers(query, principal));
    }

    /**
     * 关注用户。
     */
    @PostMapping("/follow/{targetUserId}")
    public ApiResponse<Void> follow(
            @PathVariable Long targetUserId,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        socialService.follow(targetUserId, principal);
        return ApiResponse.success(null);
    }

    /**
     * 取消关注用户。
     */
    @DeleteMapping("/follow/{targetUserId}")
    public ApiResponse<Void> unfollow(
            @PathVariable Long targetUserId,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        socialService.unfollow(targetUserId, principal);
        return ApiResponse.success(null);
    }

    /**
     * 查询拉黑列表。
     */
    @GetMapping("/blocks")
    public ApiResponse<PageResult<SocialUserVo>> blocks(
            @ModelAttribute SocialPageQuery query,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success(socialService.pageBlocks(query, principal));
    }

    /**
     * 拉黑用户。
     */
    @PostMapping("/block/{targetUserId}")
    public ApiResponse<Void> block(
            @PathVariable Long targetUserId,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        socialService.block(targetUserId, principal);
        return ApiResponse.success(null);
    }

    /**
     * 解除拉黑用户。
     */
    @DeleteMapping("/block/{targetUserId}")
    public ApiResponse<Void> unblock(
            @PathVariable Long targetUserId,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        socialService.unblock(targetUserId, principal);
        return ApiResponse.success(null);
    }

    /**
     * 查询会话列表。
     */
    @GetMapping("/conversations")
    public ApiResponse<PageResult<SocialConversationVo>> conversations(
            @ModelAttribute SocialPageQuery query,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success(socialService.pageConversations(query, principal));
    }

    /**
     * 查询会话消息。
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public ApiResponse<PageResult<SocialMessageVo>> messages(
            @PathVariable Long conversationId,
            @ModelAttribute SocialMessageQuery query,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success(socialService.pageMessages(conversationId, query, principal));
    }

    /**
     * 标记会话已读。
     */
    @PostMapping("/conversations/{conversationId}/read")
    public ApiResponse<Void> markRead(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        socialService.markConversationRead(conversationId, principal);
        return ApiResponse.success(null);
    }

    /**
     * 发送私信。
     */
    @PostMapping("/messages")
    public ApiResponse<SocialMessageVo> sendMessage(
            @Valid @RequestBody SocialMessageSendRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success(socialService.sendMessage(request, principal));
    }

    /**
     * 上传社交图片。
     */
    @PostMapping("/upload/image")
    public ApiResponse<UploadResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success(fileStorageService.uploadSocialImage(file, principal));
    }

    /**
     * 上传社交附件。
     */
    @PostMapping("/upload/file")
    public ApiResponse<UploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success(fileStorageService.uploadSocialFile(file, principal));
    }

    /**
     * 查询当前用户未读总数。
     */
    @GetMapping("/unread-count")
    public ApiResponse<SocialUnreadCountVo> unreadCount(
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ApiResponse.success(socialService.unreadCount(principal));
    }
}
