package com.template.social.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 社交私信消息实体。
 */
@Getter
@Setter
@TableName("social_message")
public class SocialMessage {

    /** 消息主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话 ID。 */
    private Long conversationId;

    /** 发送人用户 ID。 */
    private Long senderId;

    /** 接收人用户 ID。 */
    private Long receiverId;

    /** 文本内容；图片或附件消息可为空。 */
    private String content;

    /** 消息类型，取值：TEXT、IMAGE、FILE。 */
    private String messageType;

    /** 文件资源 ID。 */
    private Long fileId;

    /** 文件访问 URL。 */
    private String fileUrl;

    /** 文件原始名称。 */
    private String fileName;

    /** 文件大小，单位字节。 */
    private Long fileSize;

    /** 文件 MIME 类型。 */
    private String fileContentType;

    /** 接收方已读时间，空表示未读。 */
    private LocalDateTime readTime;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 逻辑删除标记。 */
    private Long deleted;
}
