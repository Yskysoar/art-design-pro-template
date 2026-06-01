import request from '@/utils/http'

/**
 * 获取文章分类。
 */
export function fetchArticleTypes() {
  return request.get<Api.Article.ArticleType[]>({
    url: '/api/article/types'
  })
}

/**
 * 获取文章分页列表。
 */
export function fetchArticleList(params: Api.Article.ArticleSearchParams) {
  return request.get<Api.Article.ArticleList>({
    url: '/api/article/list',
    params
  })
}

/**
 * 获取文章详情。
 */
export function fetchArticleDetail(id: number) {
  return request.get<Api.Article.ArticleDetail>({
    url: '/api/article/detail',
    params: { id }
  })
}

/**
 * 新增文章。
 */
export function createArticle(data: Api.Article.ArticleSaveParams) {
  return request.post<number>({
    url: '/api/article',
    data
  })
}

/**
 * 更新文章。
 */
export function updateArticle(id: number, data: Api.Article.ArticleSaveParams) {
  return request.put<void>({
    url: `/api/article/${id}`,
    data
  })
}

/**
 * 删除文章。
 */
export function deleteArticle(id: number) {
  return request.del<void>({
    url: `/api/article/${id}`
  })
}

/**
 * 更新文章状态。
 */
export function updateArticleStatus(id: number, status: Api.Article.ArticleStatus) {
  return request.request<void>({
    url: `/api/article/${id}/status`,
    method: 'PATCH',
    data: { status }
  })
}

/**
 * 获取文章评论分页列表。
 */
export function fetchArticleComments(params: Api.Article.ArticleCommentSearchParams) {
  return request.get<Api.Article.ArticleCommentList>({
    url: '/api/article/comment/list',
    params
  })
}

/**
 * 新增文章评论或回复。
 */
export function createArticleComment(data: Api.Article.ArticleCommentSaveParams) {
  return request.post<number>({
    url: '/api/article/comment',
    data
  })
}

/**
 * 更新文章评论状态。
 */
export function updateArticleCommentStatus(id: number, status: Api.Article.ArticleCommentStatus) {
  return request.request<void>({
    url: `/api/article/comment/${id}/status`,
    method: 'PATCH',
    data: { status }
  })
}

/**
 * 删除文章评论。
 */
export function deleteArticleComment(id: number) {
  return request.del<void>({
    url: `/api/article/comment/${id}`
  })
}
