package com.template.article.controller;

import com.template.article.dto.CommentSensitiveWordListQuery;
import com.template.article.dto.CommentSensitiveWordSaveRequest;
import com.template.article.dto.CommentSensitiveWordStatusRequest;
import com.template.article.service.CommentSensitiveWordService;
import com.template.article.vo.CommentSensitiveWordVo;
import com.template.common.pagination.PageResult;
import com.template.common.response.ApiResponse;
import com.template.security.auth.AppUserPrincipal;
import com.template.security.permission.PermissionService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评论敏感词管理接口。
 */
@RestController
@RequestMapping("/api/article/comment/sensitive-words")
public class CommentSensitiveWordController {

    private static final String SENSITIVE_WORD_PERMISSION = "article:comment:sensitive-word";

    private final CommentSensitiveWordService wordService;
    private final PermissionService permissionService;

    public CommentSensitiveWordController(CommentSensitiveWordService wordService, PermissionService permissionService) {
        this.wordService = wordService;
        this.permissionService = permissionService;
    }

    @GetMapping
    public ApiResponse<PageResult<CommentSensitiveWordVo>> listWords(
            @ModelAttribute CommentSensitiveWordListQuery query,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, SENSITIVE_WORD_PERMISSION);
        return ApiResponse.success(wordService.pageWords(query));
    }

    @PostMapping
    public ApiResponse<Void> createWord(
            @Valid @RequestBody CommentSensitiveWordSaveRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, SENSITIVE_WORD_PERMISSION);
        wordService.createWord(request, principal);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updateWord(
            @PathVariable Long id,
            @Valid @RequestBody CommentSensitiveWordSaveRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, SENSITIVE_WORD_PERMISSION);
        wordService.updateWord(id, request, principal);
        return ApiResponse.success(null);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody CommentSensitiveWordStatusRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, SENSITIVE_WORD_PERMISSION);
        wordService.updateStatus(id, request, principal);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteWord(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, SENSITIVE_WORD_PERMISSION);
        wordService.deleteWord(id);
        return ApiResponse.success(null);
    }
}
