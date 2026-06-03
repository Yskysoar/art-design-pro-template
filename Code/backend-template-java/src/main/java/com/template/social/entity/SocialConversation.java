package com.template.social.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 双人聊天会话实体。
 */
@Getter
@Setter
@TableName("social_conversation")
public class SocialConversation {

    /** 会话主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话较小用户 ID。 */
    private Long userAId;

    /** 会话较大用户 ID。 */
    private Long userBId;

    /** 最后一条消息 ID。 */
    private Long lastMessageId;

    /** 最后一条消息时间。 */
    private LocalDateTime lastMessageTime;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 更新时间。 */
    private LocalDateTime updateTime;

    /** 逻辑删除标记。 */
    private Long deleted;
}
