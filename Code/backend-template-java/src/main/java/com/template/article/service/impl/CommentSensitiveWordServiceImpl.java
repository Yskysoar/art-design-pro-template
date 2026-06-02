package com.template.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.template.article.dto.CommentSensitiveWordListQuery;
import com.template.article.dto.CommentSensitiveWordSaveRequest;
import com.template.article.dto.CommentSensitiveWordStatusRequest;
import com.template.article.entity.CommentSensitiveWord;
import com.template.article.mapper.CommentSensitiveWordMapper;
import com.template.article.service.CommentSensitiveWordService;
import com.template.article.vo.CommentSensitiveWordVo;
import com.template.common.exception.BusinessException;
import com.template.common.pagination.PageResult;
import com.template.common.response.ApiCode;
import com.template.security.auth.AppUserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * 评论敏感词服务实现。
 */
@Service
public class CommentSensitiveWordServiceImpl implements CommentSensitiveWordService {

    private static final int ENABLED = 1;
    private static final int DISABLED = 0;
    private static final long NOT_DELETED = 0L;
    private static final long DEFAULT_CURRENT = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;
    private static final String MATCH_CONTAINS = "CONTAINS";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CommentSensitiveWordMapper wordMapper;

    public CommentSensitiveWordServiceImpl(CommentSensitiveWordMapper wordMapper) {
        this.wordMapper = wordMapper;
    }

    @Override
    public PageResult<CommentSensitiveWordVo> pageWords(CommentSensitiveWordListQuery query) {
        long current = query.current() == null || query.current() < 1 ? DEFAULT_CURRENT : query.current();
        long size = query.size() == null || query.size() < 1 ? DEFAULT_SIZE : Math.min(query.size(), MAX_SIZE);
        IPage<CommentSensitiveWord> page = wordMapper.selectPage(Page.of(current, size), new LambdaQueryWrapper<CommentSensitiveWord>()
                .like(StringUtils.hasText(query.word()), CommentSensitiveWord::getWord, query.word())
                .eq(query.enabled() != null, CommentSensitiveWord::getEnabled, query.enabled())
                .eq(CommentSensitiveWord::getDeleted, NOT_DELETED)
                .orderByDesc(CommentSensitiveWord::getCreateTime)
                .orderByDesc(CommentSensitiveWord::getId));
        return new PageResult<>(
                page.getRecords().stream().map(this::toVo).toList(),
                page.getCurrent(),
                page.getSize(),
                page.getTotal()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createWord(CommentSensitiveWordSaveRequest request, AppUserPrincipal principal) {
        String word = normalizeWord(request.word());
        assertWordUnique(word, null);
        CommentSensitiveWord entity = new CommentSensitiveWord();
        entity.setWord(word);
        entity.setMatchType(MATCH_CONTAINS);
        entity.setEnabled(normalizeEnabled(request.enabled()));
        entity.setRemark(request.remark());
        entity.setCreateBy(principal.userName());
        entity.setUpdateBy(principal.userName());
        entity.setDeleted(NOT_DELETED);
        wordMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWord(Long id, CommentSensitiveWordSaveRequest request, AppUserPrincipal principal) {
        CommentSensitiveWord entity = getExistingWord(id);
        String word = normalizeWord(request.word());
        assertWordUnique(word, id);
        entity.setWord(word);
        entity.setEnabled(normalizeEnabled(request.enabled()));
        entity.setRemark(request.remark());
        entity.setUpdateBy(principal.userName());
        wordMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, CommentSensitiveWordStatusRequest request, AppUserPrincipal principal) {
        CommentSensitiveWord entity = getExistingWord(id);
        entity.setEnabled(normalizeEnabled(request.enabled()));
        entity.setUpdateBy(principal.userName());
        wordMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWord(Long id) {
        getExistingWord(id);
        wordMapper.deleteById(id);
    }

    @Override
    public List<String> findHits(String content) {
        if (!StringUtils.hasText(content)) {
            return List.of();
        }
        String lowerContent = content.toLowerCase(Locale.ROOT);
        return wordMapper.selectList(new LambdaQueryWrapper<CommentSensitiveWord>()
                        .eq(CommentSensitiveWord::getEnabled, ENABLED)
                        .eq(CommentSensitiveWord::getDeleted, NOT_DELETED))
                .stream()
                .map(CommentSensitiveWord::getWord)
                .filter(StringUtils::hasText)
                .filter(word -> lowerContent.contains(word.toLowerCase(Locale.ROOT)))
                .distinct()
                .toList();
    }

    private CommentSensitiveWord getExistingWord(Long id) {
        if (id == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "敏感词ID不能为空");
        }
        CommentSensitiveWord entity = wordMapper.selectOne(new LambdaQueryWrapper<CommentSensitiveWord>()
                .eq(CommentSensitiveWord::getId, id)
                .eq(CommentSensitiveWord::getDeleted, NOT_DELETED));
        if (entity == null) {
            throw new BusinessException(ApiCode.NOT_FOUND, "敏感词不存在");
        }
        return entity;
    }

    private String normalizeWord(String word) {
        if (!StringUtils.hasText(word)) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "敏感词不能为空");
        }
        String normalized = word.trim();
        if (normalized.length() > 100) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "敏感词长度不能超过100字");
        }
        return normalized;
    }

    private Integer normalizeEnabled(Integer enabled) {
        if (enabled == null) {
            return ENABLED;
        }
        if (enabled != ENABLED && enabled != DISABLED) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "启用状态只能为0或1");
        }
        return enabled;
    }

    private void assertWordUnique(String word, Long excludeId) {
        Long count = wordMapper.selectCount(new LambdaQueryWrapper<CommentSensitiveWord>()
                .eq(CommentSensitiveWord::getWord, word)
                .eq(CommentSensitiveWord::getDeleted, NOT_DELETED)
                .ne(excludeId != null, CommentSensitiveWord::getId, excludeId));
        if (count != null && count > 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "敏感词已存在");
        }
    }

    private CommentSensitiveWordVo toVo(CommentSensitiveWord entity) {
        return new CommentSensitiveWordVo(
                entity.getId(),
                entity.getWord(),
                entity.getMatchType(),
                entity.getEnabled(),
                entity.getRemark(),
                formatDateTime(entity.getCreateTime()),
                formatDateTime(entity.getUpdateTime())
        );
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : DATE_TIME_FORMATTER.format(dateTime);
    }
}
