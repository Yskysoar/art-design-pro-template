package com.template.article.vo;

import java.util.List;

/**
 * 敏感词命中响应。
 *
 * @param sensitiveWords 命中的敏感词
 */
public record SensitiveWordHitResponse(List<String> sensitiveWords) {
}
