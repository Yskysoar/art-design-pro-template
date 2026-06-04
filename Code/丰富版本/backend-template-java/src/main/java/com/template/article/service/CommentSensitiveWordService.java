package com.template.article.service;

import com.template.article.dto.CommentSensitiveWordListQuery;
import com.template.article.dto.CommentSensitiveWordSaveRequest;
import com.template.article.dto.CommentSensitiveWordStatusRequest;
import com.template.article.vo.CommentSensitiveWordVo;
import com.template.common.pagination.PageResult;
import com.template.security.auth.AppUserPrincipal;

import java.util.List;

/**
 * 评论敏感词服务。
 */
public interface CommentSensitiveWordService {

    PageResult<CommentSensitiveWordVo> pageWords(CommentSensitiveWordListQuery query);

    void createWord(CommentSensitiveWordSaveRequest request, AppUserPrincipal principal);

    void updateWord(Long id, CommentSensitiveWordSaveRequest request, AppUserPrincipal principal);

    void updateStatus(Long id, CommentSensitiveWordStatusRequest request, AppUserPrincipal principal);

    void deleteWord(Long id);

    List<String> findHits(String content);
}
