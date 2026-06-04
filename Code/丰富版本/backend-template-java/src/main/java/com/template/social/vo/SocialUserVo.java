package com.template.social.vo;

/**
 * 社交用户资料响应。
 */
public record SocialUserVo(
        Long id,
        String userName,
        String nickName,
        String avatar,
        String email,
        boolean following,
        boolean followedBy,
        boolean mutualFollow,
        boolean blockedByMe,
        boolean blockedMe
) {
}
