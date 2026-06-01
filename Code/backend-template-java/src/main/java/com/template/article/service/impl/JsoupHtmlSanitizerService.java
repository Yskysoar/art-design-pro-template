package com.template.article.service.impl;

import com.template.article.service.HtmlSanitizerService;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

/**
 * 基于 jsoup 的富文本净化服务。
 */
@Service
public class JsoupHtmlSanitizerService implements HtmlSanitizerService {

    private final Safelist safelist;

    public JsoupHtmlSanitizerService() {
        this.safelist = Safelist.relaxed()
                .addTags("span", "pre", "code", "blockquote", "figure", "figcaption")
                .addAttributes(":all", "class")
                .addAttributes("a", "target", "rel")
                .addAttributes("img", "alt", "title")
                .addProtocols("a", "href", "http", "https", "mailto")
                .addProtocols("img", "src", "http", "https", "/api/common/files");
    }

    @Override
    public String sanitize(String html) {
        return Jsoup.clean(html == null ? "" : html, safelist);
    }

    @Override
    public String toPlainText(String html) {
        return Jsoup.parse(html == null ? "" : html).text();
    }
}
