package com.template.article.service;

/**
 * 富文本净化服务。
 */
public interface HtmlSanitizerService {

    /**
     * 净化富文本 HTML。
     *
     * @param html 原始 HTML
     * @return 净化后的 HTML
     */
    String sanitize(String html);

    /**
     * 提取纯文本。
     *
     * @param html HTML 内容
     * @return 纯文本
     */
    String toPlainText(String html);
}
