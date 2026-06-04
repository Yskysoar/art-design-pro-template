package com.template.social.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 社交分页查询参数。
 */
@Getter
@Setter
public class SocialPageQuery {

    /** 当前页码。 */
    private long current = 1;

    /** 每页数量。 */
    private long size = 20;

    /** 用户关键词。 */
    private String keyword;
}
