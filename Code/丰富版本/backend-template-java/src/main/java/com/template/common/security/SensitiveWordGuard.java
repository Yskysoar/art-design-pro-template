package com.template.common.security;

import com.template.article.service.CommentSensitiveWordService;
import com.template.article.vo.SensitiveWordHitResponse;
import com.template.common.exception.BusinessException;
import com.template.common.response.ApiCode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 全站文本敏感词校验服务。
 */
@Service
public class SensitiveWordGuard {

    private final CommentSensitiveWordService sensitiveWordService;

    public SensitiveWordGuard(CommentSensitiveWordService sensitiveWordService) {
        this.sensitiveWordService = sensitiveWordService;
    }

    /**
     * 校验文本内容是否命中启用敏感词。
     *
     * @param fieldName 字段名称，用于错误提示
     * @param content   待校验内容
     */
    public void validate(String fieldName, String content) {
        if (!StringUtils.hasText(content)) {
            return;
        }
        List<String> hits = sensitiveWordService.findHits(content);
        if (!hits.isEmpty()) {
            throw new BusinessException(
                    ApiCode.BAD_REQUEST,
                    fieldName + "包含敏感词，请修改后再提交",
                    new SensitiveWordHitResponse(hits)
            );
        }
    }

    /**
     * 批量校验多个文本字段。
     *
     * @param fields 文本字段集合
     */
    public void validateAll(TextField... fields) {
        if (fields == null) {
            return;
        }
        for (TextField field : fields) {
            if (field != null) {
                validate(field.name(), field.content());
            }
        }
    }

    public record TextField(String name, String content) {
    }
}
