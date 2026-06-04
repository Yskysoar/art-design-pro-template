package com.template.social.convert;

import com.template.social.entity.SocialMessage;
import com.template.social.vo.SocialMessageVo;
import com.template.social.vo.SocialUserVo;
import com.template.system.user.entity.SysUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 社交模块对象转换器。
 */
@Mapper(componentModel = "spring")
public interface SocialStructMapper {

    /**
     * 系统用户转换为社交用户基础响应。
     *
     * @param user         系统用户
     * @param displayName  展示名称
     * @param following    当前用户是否关注对方
     * @param followedBy   对方是否关注当前用户
     * @param mutualFollow 是否互关
     * @param blockedByMe  当前用户是否拉黑对方
     * @param blockedMe    对方是否拉黑当前用户
     * @return 社交用户响应
     */
    @Mapping(target = "email", source = "user.userEmail")
    @Mapping(target = "nickName", source = "displayName")
    SocialUserVo toUserVo(
            SysUser user,
            String displayName,
            boolean following,
            boolean followedBy,
            boolean mutualFollow,
            boolean blockedByMe,
            boolean blockedMe
    );

    /**
     * 私信消息转换为前端响应。
     *
     * @param message      私信消息
     * @param senderName   发送人展示名称
     * @param senderAvatar 发送人头像
     * @param mine         是否为当前用户发送
     * @param read         是否已读
     * @param createTime   格式化后的创建时间
     * @return 消息响应
     */
    @Mapping(target = "senderName", source = "senderName")
    @Mapping(target = "senderAvatar", source = "senderAvatar")
    @Mapping(target = "mine", source = "mine")
    @Mapping(target = "read", source = "read")
    @Mapping(target = "createTime", source = "createTime")
    SocialMessageVo toMessageVo(
            SocialMessage message,
            String senderName,
            String senderAvatar,
            boolean mine,
            boolean read,
            String createTime
    );
}
