package com.template.social.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 用户拉黑关系实体。
 */
@Getter
@Setter
@TableName("social_block")
public class SocialBlock {

    /** 拉黑关系主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 拉黑人用户 ID。 */
    private Long blockerId;

    /** 被拉黑人用户 ID。 */
    private Long blockedId;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 逻辑删除标记。 */
    private Long deleted;
}
