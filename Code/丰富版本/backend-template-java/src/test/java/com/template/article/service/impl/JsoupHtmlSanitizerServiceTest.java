package com.template.article.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 富文本净化服务测试。
 */
class JsoupHtmlSanitizerServiceTest {

    private final JsoupHtmlSanitizerService sanitizerService = new JsoupHtmlSanitizerService();

    @Test
    @DisplayName("富文本净化应移除脚本、事件属性和危险协议")
    void sanitizeShouldRemoveScriptEventAndJavascriptProtocol() {
        String html = "<p onclick=\"alert(1)\">正文</p><script>alert(1)</script><a href=\"javascript:alert(1)\">链接</a>";

        String sanitized = sanitizerService.sanitize(html);

        assertThat(sanitized).contains("正文");
        assertThat(sanitized).doesNotContain("script", "onclick", "javascript:");
    }
}
