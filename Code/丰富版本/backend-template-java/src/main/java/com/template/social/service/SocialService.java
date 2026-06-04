package com.template.social.service;

import com.template.common.pagination.PageResult;
import com.template.security.auth.AppUserPrincipal;
import com.template.social.dto.SocialMessageQuery;
import com.template.social.dto.SocialMessageSendRequest;
import com.template.social.dto.SocialPageQuery;
import com.template.social.vo.SocialConversationVo;
import com.template.social.vo.SocialMessageVo;
import com.template.social.vo.SocialUnreadCountVo;
import com.template.social.vo.SocialUserVo;

/**
 * 社交关注与聊天业务服务。
 */
public interface SocialService {

    PageResult<SocialUserVo> searchUsers(SocialPageQuery query, AppUserPrincipal principal);

    SocialUserVo getUserProfile(Long targetUserId, AppUserPrincipal principal);

    PageResult<SocialUserVo> pageFollowing(SocialPageQuery query, AppUserPrincipal principal);

    PageResult<SocialUserVo> pageFollowers(SocialPageQuery query, AppUserPrincipal principal);

    void follow(Long targetUserId, AppUserPrincipal principal);

    void unfollow(Long targetUserId, AppUserPrincipal principal);

    PageResult<SocialUserVo> pageBlocks(SocialPageQuery query, AppUserPrincipal principal);

    void block(Long targetUserId, AppUserPrincipal principal);

    void unblock(Long targetUserId, AppUserPrincipal principal);

    PageResult<SocialConversationVo> pageConversations(SocialPageQuery query, AppUserPrincipal principal);

    PageResult<SocialMessageVo> pageMessages(Long conversationId, SocialMessageQuery query, AppUserPrincipal principal);

    SocialMessageVo sendMessage(SocialMessageSendRequest request, AppUserPrincipal principal);

    void markConversationRead(Long conversationId, AppUserPrincipal principal);

    SocialUnreadCountVo unreadCount(AppUserPrincipal principal);
}
