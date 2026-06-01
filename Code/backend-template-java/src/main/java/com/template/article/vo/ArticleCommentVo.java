package com.template.article.vo;

import java.util.List;

/**
 * 文章评论响应。
 *
 * @param id         评论 ID
 * @param articleId  所属文章 ID
 * @param parentId   父评论 ID
 * @param rootId     根评论 ID
 * @param content    评论内容
 * @param status     评论状态
 * @param userId     评论用户 ID
 * @param userName   评论用户名称
 * @param userAvatar 评论用户头像
 * @param replyToUserName 被回复用户名称，一级评论为空
 * @param createTime 创建时间
 * @param canHide    当前用户是否可隐藏
 * @param canRestore 当前用户是否可恢复
 * @param canDelete  当前用户是否可删除
 * @param replies    子回复
 */
public record ArticleCommentVo(
        Long id,
        Long articleId,
        Long parentId,
        Long rootId,
        String content,
        String status,
        Long userId,
        String userName,
        String userAvatar,
        String replyToUserName,
        String createTime,
        Boolean canHide,
        Boolean canRestore,
        Boolean canDelete,
        List<ArticleCommentVo> replies
) {
}
