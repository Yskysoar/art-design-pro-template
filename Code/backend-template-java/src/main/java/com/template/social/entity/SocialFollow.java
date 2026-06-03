package com.template.social.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 用户关注关系实体。
 */
@Getter
@Setter
@TableName("social_follow")
public class SocialFollow {

    /** 关注关系主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关注发起用户 ID。 */
    private Long followerId;

    /** 被关注用户 ID。 */
    private Long followingId;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 逻辑删除标记。 */
    private Long deleted;
}
