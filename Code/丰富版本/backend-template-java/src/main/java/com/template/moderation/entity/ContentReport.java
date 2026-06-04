package com.template.moderation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 内容举报审核实体。
 */
@Getter
@Setter
@TableName("content_report")
public class ContentReport {

    /** 举报主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 目标类型：ARTICLE/COMMENT/PRIVATE_MESSAGE。 */
    private String targetType;

    /** 目标 ID。 */
    private Long targetId;

    /** 举报原因类型。 */
    private String reasonType;

    /** 补充说明。 */
    private String description;

    /** 举报人 ID。 */
    private Long reporterId;

    /** 举报人名称。 */
    private String reporterName;

    /** 状态：PENDING/PROCESSING/RESOLVED/REJECTED。 */
    private String status;

    /** 处理人 ID。 */
    private Long handlerId;

    /** 处理人名称。 */
    private String handlerName;

    /** 处理备注。 */
    private String handlingRemark;

    /** 处理时间。 */
    private LocalDateTime handledTime;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 更新时间。 */
    private LocalDateTime updateTime;

    /** 逻辑删除标记。 */
    private Long deleted;
}
