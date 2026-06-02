package com.template.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.template.article.entity.CommentSensitiveWord;
import com.template.article.mapper.CommentSensitiveWordMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.BadSqlGrammarException;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 评论敏感词服务单元测试。
 */
@ExtendWith(MockitoExtension.class)
class CommentSensitiveWordServiceImplTest {

    @Mock
    private CommentSensitiveWordMapper wordMapper;

    @Test
    @DisplayName("敏感词表未初始化时评论发布检查应降级为空命中")
    void findHitsShouldFallbackWhenSensitiveWordTableMissing() {
        CommentSensitiveWordServiceImpl service = new CommentSensitiveWordServiceImpl(wordMapper);
        when(wordMapper.selectList(anyWrapper())).thenThrow(new BadSqlGrammarException(
                "select",
                "select * from comment_sensitive_word",
                new SQLException("Table 'comment_sensitive_word' doesn't exist")
        ));

        List<String> hits = service.findHits("正常评论内容");

        assertThat(hits).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private <T> Wrapper<T> anyWrapper() {
        return any(Wrapper.class);
    }
}
