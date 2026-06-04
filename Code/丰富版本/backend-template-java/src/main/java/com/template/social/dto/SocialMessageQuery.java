package com.template.social.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 社交消息分页查询参数。
 */
@Getter
@Setter
public class SocialMessageQuery {

    /** 当前页码。 */
    private long current = 1;

    /** 每页数量。 */
    private long size = 30;
}
