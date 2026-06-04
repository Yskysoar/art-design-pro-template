<!-- 文章详情页面 -->
<template>
  <div class="article-detail page-content">
    <div class="max-w-200 m-auto mt-15" v-loading="loading">
      <div class="flex items-center gap-3 text-sm text-g-600">
        <ElTag v-if="article?.categoryName" size="small">{{ article.categoryName }}</ElTag>
        <span>{{ article?.createTime }}</span>
        <span>浏览 {{ article?.viewCount || 0 }}</span>
        <span>评论 {{ article?.commentCount || 0 }}</span>
      </div>

      <h1 class="text-3xl font-semibold mt-4">{{ article?.title }}</h1>
      <div class="article-actions">
        <ElButton text type="danger" :disabled="!userStore.isLogin || !article?.id" @click="openArticleReport">
          <ArtSvgIcon icon="ri:flag-line" />
          <span>{{ $t('moderation.report.reportAction') }}</span>
        </ElButton>
      </div>
      <p v-if="article?.summary" class="mt-4 text-g-600 leading-7">{{ article.summary }}</p>
      <ElImage v-if="article?.coverUrl" class="cover-image" :src="article.coverUrl" fit="cover" />

      <div class="markdown-body mt-12.5" v-highlight v-html="safeHtml"></div>

      <div v-if="article?.attachments?.length" class="attachments">
        <h2>附件</h2>
        <div
          v-for="item in article.attachments"
          :key="item.id"
          class="attachment-card"
          @click="openAttachment(item)"
        >
          <ElImage
            v-if="isImageAttachment(item)"
            class="attachment-thumb"
            :src="item.url"
            :alt="item.originalName"
            :preview-src-list="[item.url]"
            :preview-teleported="true"
            fit="cover"
            @click.stop
          />
          <div v-else class="attachment-type" :class="attachmentTypeClass(item)">
            <span>{{ attachmentTypeLabel(item) }}</span>
          </div>
          <div class="attachment-info">
            <strong>{{ item.originalName }}</strong>
            <span>{{ formatFileSize(item.size) }}</span>
            <em>{{ item.contentType || '-' }}</em>
          </div>
          <ElButton text class="attachment-download" @click.stop="openAttachment(item)">
            <ArtSvgIcon icon="ri:download-2-line" />
          </ElButton>
        </div>
      </div>

      <section class="comments-section">
        <div class="comments-header">
          <h2>评论</h2>
          <span>{{ commentSummaryText }}</span>
        </div>

        <div class="comment-editor">
          <ElInput
            v-model="commentContent"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            :disabled="!userStore.isLogin || submittingComment"
            :placeholder="userStore.isLogin ? '写下你的评论' : '登录后可以发表评论'"
          />
          <div class="comment-editor-actions">
            <ElButton type="primary" :disabled="!canSubmitComment" :loading="submittingComment" @click="submitComment()">
              发布
            </ElButton>
          </div>
        </div>

        <div v-loading="commentsLoading" class="comments-list">
          <ElEmpty v-if="showCommentsEmpty" description="暂无评论" />
          <article v-for="comment in comments" :key="comment.id" class="comment-item">
            <div class="comment-main">
              <ElAvatar :size="36" :src="commentAvatar(comment)">{{ avatarText(comment.userName) }}</ElAvatar>
              <div class="comment-body">
                <div class="comment-meta">
                  <span class="comment-user">{{ comment.userName }}</span>
                  <ElTag v-if="comment.status === 'HIDDEN'" size="small" type="warning">已隐藏</ElTag>
                  <ElTag v-if="comment.status === 'DELETED'" size="small" type="info">已删除</ElTag>
                  <span>{{ comment.createTime }}</span>
                </div>
                <p class="comment-content">{{ displayCommentContent(comment) }}</p>
                <div class="comment-actions">
                  <ElButton
                    link
                    type="primary"
                    :disabled="!userStore.isLogin || comment.status === 'DELETED'"
                    @click="startReply(comment)"
                  >
                    回复
                  </ElButton>
                  <ElButton
                    link
                    type="danger"
                    :disabled="!userStore.isLogin || comment.status === 'DELETED'"
                    @click="openCommentReport(comment)"
                  >
                    举报
                  </ElButton>
                  <ElButton
                    v-if="comment.canHide"
                    link
                    type="warning"
                    @click="changeCommentStatus(comment, 'HIDDEN')"
                  >
                    隐藏
                  </ElButton>
                  <ElButton
                    v-if="comment.canRestore"
                    link
                    type="success"
                    @click="changeCommentStatus(comment, 'NORMAL')"
                  >
                    恢复
                  </ElButton>
                  <ElButton
                    v-if="comment.canDelete"
                    link
                    type="danger"
                    @click="removeComment(comment)"
                  >
                    删除
                  </ElButton>
                </div>

                <div v-if="replyTarget && (replyTarget.id === comment.id || replyTarget.rootId === comment.id)" class="reply-editor">
                  <ElInput
                    v-model="replyContent"
                    type="textarea"
                    :rows="2"
                    maxlength="500"
                    show-word-limit
                    :placeholder="replyPlaceholder"
                  />
                  <div class="reply-actions">
                    <ElButton @click="cancelReply">取消</ElButton>
                    <ElButton type="primary" :disabled="!canSubmitReply" :loading="submittingComment" @click="submitComment(replyTarget)">
                      回复
                    </ElButton>
                  </div>
                </div>

                <div v-if="comment.replies?.length" class="reply-list">
                  <div v-for="reply in visibleReplies(comment)" :key="reply.id" class="reply-item">
                    <div class="reply-header">
                      <ElAvatar :size="28" :src="commentAvatar(reply)">{{ avatarText(reply.userName) }}</ElAvatar>
                      <div class="reply-meta">
                      <span class="comment-user">{{ reply.userName }}</span>
                      <template v-if="reply.replyToUserName">
                        <span>回复</span>
                        <span class="comment-user">{{ reply.replyToUserName }}</span>
                      </template>
                      <ElTag v-if="reply.status === 'HIDDEN'" size="small" type="warning">已隐藏</ElTag>
                      <ElTag v-if="reply.status === 'DELETED'" size="small" type="info">已删除</ElTag>
                      <span>{{ reply.createTime }}</span>
                    </div>
                    </div>
                    <p class="comment-content">{{ displayCommentContent(reply) }}</p>
                    <div class="comment-actions">
                      <ElButton
                        link
                        type="primary"
                        :disabled="!userStore.isLogin || reply.status === 'DELETED'"
                        @click="startReply(reply)"
                      >
                        回复
                      </ElButton>
                      <ElButton
                        link
                        type="danger"
                        :disabled="!userStore.isLogin || reply.status === 'DELETED'"
                        @click="openCommentReport(reply)"
                      >
                        举报
                      </ElButton>
                      <ElButton
                        v-if="reply.canHide"
                        link
                        type="warning"
                        @click="changeCommentStatus(reply, 'HIDDEN')"
                      >
                        隐藏
                      </ElButton>
                      <ElButton
                        v-if="reply.canRestore"
                        link
                        type="success"
                        @click="changeCommentStatus(reply, 'NORMAL')"
                      >
                        恢复
                      </ElButton>
                      <ElButton
                        v-if="reply.canDelete"
                        link
                        type="danger"
                        @click="removeComment(reply)"
                      >
                        删除
                      </ElButton>
                    </div>
                  </div>
                  <ElButton
                    v-if="comment.replies.length > REPLY_COLLAPSE_LIMIT"
                    class="reply-toggle"
                    link
                    type="primary"
                    @click="toggleReplies(comment.id)"
                  >
                    {{ expandedReplyIds.has(comment.id) ? '收起' : `展开全部 ${comment.replies.length} 条回复` }}
                  </ElButton>
                </div>
              </div>
            </div>
          </article>
        </div>

        <div v-if="commentsPagination.total > commentsPagination.size" class="comments-pagination">
          <ElPagination
            background
            v-model:current-page="commentsPagination.current"
            :page-size="commentsPagination.size"
            layout="prev, pager, next"
            :total="commentsPagination.total"
            @current-change="getComments"
          />
        </div>
      </section>
    </div>
    <ContentReportDialog
      v-model:visible="reportDialog.visible"
      :target-type="reportDialog.targetType"
      :target-id="reportDialog.targetId"
    />
    <ArtBackToTop />
  </div>
</template>

<script setup lang="ts">
  import '@/assets/styles/core/md.scss'
  import '@/assets/styles/custom/one-dark-pro.scss'
  import {
    createArticleComment,
    deleteArticleComment,
    fetchArticleComments,
    fetchArticleDetail,
    updateArticleCommentStatus
  } from '@/api/article'
  import { useCommon } from '@/hooks/core/useCommon'
  import { useUserStore } from '@/store/modules/user'
  import { isHttpError } from '@/utils/http/error'
  import { sanitizeRichHtml } from '@/utils/security/html'
  import { ElMessageBox } from 'element-plus'
  import ContentReportDialog from '@/components/business/content-report-dialog/index.vue'
  import defaultAvatar from '@/assets/images/user/avatar.webp'

  defineOptions({ name: 'ArticleDetail' })

  const REPLY_COLLAPSE_LIMIT = 3
  const route = useRoute()
  const userStore = useUserStore()
  const articleId = computed(() => Number(route.params.id))
  const article = ref<Api.Article.ArticleDetail>()
  const loading = ref(false)
  const comments = ref<Api.Article.ArticleCommentItem[]>([])
  const commentsLoading = ref(false)
  const submittingComment = ref(false)
  const commentContent = ref('')
  const replyContent = ref('')
  const replyTarget = ref<Api.Article.ArticleCommentItem>()
  const expandedReplyIds = reactive(new Set<number>())
  const reportDialog = reactive<{
    visible: boolean
    targetType: 'ARTICLE' | 'COMMENT'
    targetId?: number
  }>({
    visible: false,
    targetType: 'ARTICLE',
    targetId: undefined
  })
  const commentsPagination = reactive({
    current: 1,
    size: 10,
    total: 0
  })
  const safeHtml = computed(() =>
    sanitizeRichHtml(article.value?.contentHtml || article.value?.html_content || '')
  )
  const canSubmitComment = computed(
    () => userStore.isLogin && commentContent.value.trim().length > 0 && commentContent.value.trim().length <= 500
  )
  const canSubmitReply = computed(
    () => userStore.isLogin && replyContent.value.trim().length > 0 && replyContent.value.trim().length <= 500
  )
  const replyPlaceholder = computed(() =>
    replyTarget.value?.userName ? `回复 ${replyTarget.value.userName}` : '回复评论'
  )
  const commentSummaryText = computed(() => {
    const rootCount = article.value?.rootCommentCount ?? commentsPagination.total
    const replyCount = article.value?.replyCount ?? 0
    return replyCount > 0 ? `${rootCount} 条主评论 · ${replyCount} 条回复` : `${rootCount} 条主评论`
  })
  const showCommentsEmpty = computed(() => comments.value.length === 0 && !commentsLoading.value)

  const getArticleDetail = async () => {
    if (!articleId.value) return
    loading.value = true
    try {
      article.value = await fetchArticleDetail(articleId.value)
    } finally {
      loading.value = false
    }
  }

  const getComments = async () => {
    if (!articleId.value) return
    commentsLoading.value = true
    try {
      const result = await fetchArticleComments({
        articleId: articleId.value,
        current: commentsPagination.current,
        size: commentsPagination.size
      })
      comments.value = result.records
      commentsPagination.total = result.total
      syncExpandedReplyIds(result.records)
    } finally {
      commentsLoading.value = false
    }
  }

  const submitComment = async (parent?: Api.Article.ArticleCommentItem) => {
    const content = parent ? replyContent.value.trim() : commentContent.value.trim()
    if (!content || !articleId.value) return
    submittingComment.value = true
    try {
      await createArticleComment({
        articleId: articleId.value,
        parentId: parent?.id ?? 0,
        content
      })
      if (parent) {
        replyContent.value = ''
        replyTarget.value = undefined
      } else {
        commentContent.value = ''
        commentsPagination.current = 1
      }
      await getArticleDetail()
      await getComments()
      ElMessage.success('评论已发布')
    } catch (error) {
      const words = getSensitiveWords(error)
      if (words.length > 0) {
        ElMessage.warning(`内容包含敏感词：${words.join('、')}，请修改后再发布`)
      } else if (isHttpError(error)) {
        ElMessage.error(error.message)
      } else {
        throw error
      }
    } finally {
      submittingComment.value = false
    }
  }

  const getSensitiveWords = (error: unknown): string[] => {
    if (!isHttpError(error)) return []
    const data = error.data as { sensitiveWords?: unknown } | undefined
    if (!data || !Array.isArray(data.sensitiveWords)) return []
    return data.sensitiveWords.filter((word): word is string => typeof word === 'string')
  }

  const startReply = (comment: Api.Article.ArticleCommentItem) => {
    replyTarget.value = comment
    replyContent.value = ''
  }

  const cancelReply = () => {
    replyTarget.value = undefined
    replyContent.value = ''
  }

  const visibleReplies = (comment: Api.Article.ArticleCommentItem) => {
    if (!comment.replies?.length) return []
    if (comment.replies.length <= REPLY_COLLAPSE_LIMIT || expandedReplyIds.has(comment.id)) {
      return comment.replies
    }
    return comment.replies.slice(0, REPLY_COLLAPSE_LIMIT)
  }

  const toggleReplies = (commentId: number) => {
    if (expandedReplyIds.has(commentId)) {
      expandedReplyIds.delete(commentId)
    } else {
      expandedReplyIds.add(commentId)
    }
  }

  const syncExpandedReplyIds = (items: Api.Article.ArticleCommentItem[]) => {
    const ids = new Set(items.map((item) => item.id))
    Array.from(expandedReplyIds).forEach((id) => {
      if (!ids.has(id)) expandedReplyIds.delete(id)
    })
  }

  const changeCommentStatus = async (
    comment: Api.Article.ArticleCommentItem,
    status: Api.Article.ArticleCommentStatus
  ) => {
    await updateArticleCommentStatus(comment.id, status)
    await getArticleDetail()
    await getComments()
    ElMessage.success(status === 'NORMAL' ? '评论已恢复' : '评论已隐藏')
  }

  const removeComment = async (comment: Api.Article.ArticleCommentItem) => {
    await ElMessageBox.confirm('删除后会隐藏当前评论内容，历史子回复仍会保留。', '删除评论', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    await deleteArticleComment(comment.id)
    await getArticleDetail()
    await getComments()
    ElMessage.success('评论已删除')
  }

  const openArticleReport = () => {
    reportDialog.targetType = 'ARTICLE'
    reportDialog.targetId = articleId.value
    reportDialog.visible = true
  }

  const openCommentReport = (comment: Api.Article.ArticleCommentItem) => {
    reportDialog.targetType = 'COMMENT'
    reportDialog.targetId = comment.id
    reportDialog.visible = true
  }

  const openAttachment = (attachment: Api.Article.ArticleAttachment) => {
    window.open(attachment.url, '_blank', 'noopener,noreferrer')
  }

  const isImageAttachment = (attachment: Api.Article.ArticleAttachment) =>
    attachment.contentType?.toLowerCase().startsWith('image/')

  const attachmentTypeLabel = (attachment: Api.Article.ArticleAttachment) => {
    const fileName = attachment.originalName || ''
    const extension = fileName.includes('.') ? fileName.split('.').pop() : attachment.contentType?.split('/').pop()
    const label = (extension || 'file').toUpperCase()
    if (['DOC', 'DOCX'].includes(label)) return 'DOC'
    if (['XLS', 'XLSX'].includes(label)) return 'XLS'
    if (['PPT', 'PPTX'].includes(label)) return 'PPT'
    return label.length > 4 ? 'FILE' : label
  }

  const attachmentTypeClass = (attachment: Api.Article.ArticleAttachment) => {
    const label = attachmentTypeLabel(attachment).toLowerCase()
    if (['pdf', 'doc', 'xls', 'ppt', 'zip'].includes(label)) return `is-${label}`
    return 'is-file'
  }

  const formatFileSize = (size?: number) => {
    if (!size) return '-'
    if (size < 1024) return `${size}B`
    if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)}KB`
    return `${(size / 1024 / 1024).toFixed(1)}MB`
  }

  const displayCommentContent = (comment: Api.Article.ArticleCommentItem) => {
    if (comment.status === 'DELETED') return '该评论已删除'
    if (comment.status === 'HIDDEN') return '该评论已隐藏'
    return comment.content
  }

  const commentAvatar = (comment: Api.Article.ArticleCommentItem) => comment.userAvatar || defaultAvatar

  const avatarText = (name?: string) => name?.slice(0, 1) || '评'

  onMounted(() => {
    useCommon().scrollToTop()
    getArticleDetail()
    getComments()
  })
</script>

<style lang="scss" scoped>
  .article-detail {
    .cover-image {
      width: 100%;
      max-height: 420px;
      margin-top: 24px;
      border: 1px solid var(--art-gray-200);
      border-radius: 8px;
    }

    .attachments {
      padding-top: 24px;
      margin-top: 32px;
      border-top: 1px solid var(--el-border-color);

      h2 {
        margin: 0;
        font-size: 18px;
        font-weight: 600;
      }
    }

    .attachment-card {
      display: grid;
      grid-template-columns: 52px minmax(0, 1fr) 32px;
      gap: 12px;
      align-items: center;
      max-width: 520px;
      padding: 12px;
      margin-top: 12px;
      cursor: pointer;
      background: rgb(250 250 250);
      border: 1px solid var(--el-border-color-lighter);
      border-radius: 8px;
      transition:
        border-color 0.2s ease,
        box-shadow 0.2s ease;

      &:hover {
        border-color: var(--el-color-primary-light-5);
        box-shadow: 0 6px 18px rgb(15 23 42 / 8%);
      }
    }

    .attachment-thumb,
    .attachment-type {
      width: 52px;
      height: 52px;
      overflow: hidden;
      border-radius: 6px;
    }

    .attachment-type {
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      background: #8f99a8;

      span {
        font-size: 12px;
        font-weight: 700;
        line-height: 1;
      }

      &.is-pdf {
        background: #e34d4d;
      }

      &.is-doc {
        background: #3478f6;
      }

      &.is-xls {
        background: #21a366;
      }

      &.is-ppt {
        background: #d85f32;
      }

      &.is-zip {
        background: #7c5cff;
      }
    }

    .attachment-info {
      min-width: 0;

      strong,
      span,
      em {
        display: block;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      strong {
        font-size: 14px;
        line-height: 20px;
        color: var(--el-text-color-primary);
      }

      span,
      em {
        margin-top: 3px;
        font-size: 12px;
        font-style: normal;
        line-height: 18px;
        color: var(--el-text-color-secondary);
      }
    }

    .attachment-download {
      width: 32px;
      height: 32px;
      padding: 0;

      .art-svg-icon {
        font-size: 18px;
      }
    }

    .article-actions {
      display: flex;
      justify-content: flex-end;
      margin-top: 8px;

      .art-svg-icon {
        margin-right: 4px;
      }
    }

    .comments-section {
      padding-top: 32px;
      margin-top: 40px;
      border-top: 1px solid var(--el-border-color);
    }

    .comments-header {
      display: flex;
      align-items: baseline;
      gap: 12px;
      margin-bottom: 18px;

      h2 {
        margin: 0;
        font-size: 20px;
        font-weight: 600;
      }

      span {
        font-size: 14px;
        color: var(--el-text-color-secondary);
      }
    }

    .comment-editor,
    .reply-editor {
      display: flex;
      flex-direction: column;
      gap: 10px;
    }

    .comment-editor-actions,
    .reply-actions,
    .comments-pagination {
      display: flex;
      justify-content: flex-end;
    }

    .comments-list {
      min-height: 120px;
      margin-top: 24px;
    }

    .comment-item {
      padding: 18px 0;
      border-top: 1px solid var(--el-border-color-lighter);
    }

    .comment-main {
      display: flex;
      gap: 12px;
      align-items: flex-start;
    }

    .comment-body {
      flex: 1;
      min-width: 0;
    }

    .comment-meta,
    .reply-meta {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      align-items: center;
      font-size: 13px;
      color: var(--el-text-color-secondary);
    }

    .comment-user {
      font-weight: 600;
      color: var(--el-text-color-primary);
    }

    .comment-content {
      margin: 8px 0;
      line-height: 1.7;
      color: var(--el-text-color-primary);
      white-space: pre-wrap;
      overflow-wrap: anywhere;
    }

    .comment-actions {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      min-height: 28px;
    }

    .reply-editor {
      margin-top: 12px;
    }

    .reply-list {
      padding: 12px 14px;
      margin-top: 12px;
      background: var(--el-fill-color-lighter);
      border-radius: 8px;
    }

    .reply-header {
      display: flex;
      gap: 10px;
      align-items: center;
    }

    .reply-item + .reply-item {
      padding-top: 12px;
      margin-top: 12px;
      border-top: 1px solid var(--el-border-color-lighter);
    }

    .reply-item > .comment-content,
    .reply-item > .comment-actions {
      margin-left: 38px;
    }

    .reply-toggle {
      margin-top: 8px;
      padding: 0;
    }

    .comments-pagination {
      margin-top: 20px;
    }

    :deep(.markdown-body) {
      margin-top: 60px;

      img {
        width: 100%;
        border: 1px solid var(--art-gray-200);
      }

      pre {
        position: relative;

        &:hover {
          .copy-button {
            opacity: 1;
          }
        }

        &::before {
          position: absolute;
          top: 0;
          left: 50px;
          width: 1px;
          height: 100%;
          content: '';
          background: #0a0a0e;
        }
      }

      .code-wrapper {
        overflow-x: auto;
      }

      .line-number {
        position: sticky;
        left: 0;
        z-index: 2;
        box-sizing: border-box;
        display: inline-block;
        width: 50px;
        margin-right: 10px;
        font-size: 14px;
        color: #9e9e9e;
        text-align: center;
      }

      .copy-button {
        position: absolute;
        top: 6px;
        right: 6px;
        z-index: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        width: 40px;
        height: 40px;
        font-size: 20px;
        line-height: 40px;
        color: #999;
        text-align: center;
        cursor: pointer;
        background-color: #000;
        border: none;
        border-radius: 8px;
        opacity: 0;
        transition: all 0.2s;
      }
    }
  }
</style>
