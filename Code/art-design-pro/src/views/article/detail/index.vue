<!-- 文章详情页面 -->
<template>
  <div class="article-detail page-content">
    <div class="max-w-200 m-auto mt-15" v-loading="loading">
      <div class="flex items-center gap-3 text-sm text-g-600">
        <ElTag v-if="article?.categoryName" size="small">{{ article.categoryName }}</ElTag>
        <span>{{ article?.createTime }}</span>
        <span>浏览 {{ article?.viewCount || 0 }}</span>
      </div>

      <h1 class="text-3xl font-semibold mt-4">{{ article?.title }}</h1>
      <p v-if="article?.summary" class="mt-4 text-g-600 leading-7">{{ article.summary }}</p>
      <ElImage v-if="article?.coverUrl" class="cover-image" :src="article.coverUrl" fit="cover" />

      <div class="markdown-body mt-12.5" v-highlight v-html="safeHtml"></div>

      <div v-if="article?.attachments?.length" class="attachments">
        <h2>附件</h2>
        <ElLink
          v-for="item in article.attachments"
          :key="item.id"
          :href="item.url"
          target="_blank"
          type="primary"
        >
          {{ item.originalName }}
        </ElLink>
      </div>
    </div>
    <ArtBackToTop />
  </div>
</template>

<script setup lang="ts">
  import '@/assets/styles/core/md.scss'
  import '@/assets/styles/custom/one-dark-pro.scss'
  import { fetchArticleDetail } from '@/api/article'
  import { useCommon } from '@/hooks/core/useCommon'

  defineOptions({ name: 'ArticleDetail' })

  const route = useRoute()
  const articleId = computed(() => Number(route.params.id))
  const article = ref<Api.Article.ArticleDetail>()
  const loading = ref(false)
  const safeHtml = computed(() => sanitizeHtml(article.value?.contentHtml || article.value?.html_content || ''))

  const getArticleDetail = async () => {
    if (!articleId.value) return
    loading.value = true
    try {
      article.value = await fetchArticleDetail(articleId.value)
    } finally {
      loading.value = false
    }
  }

  const sanitizeHtml = (html: string) =>
    html
      .replace(/<script[\s\S]*?>[\s\S]*?<\/script>/gi, '')
      .replace(/\son\w+="[^"]*"/gi, '')
      .replace(/\son\w+='[^']*'/gi, '')
      .replace(/javascript:/gi, '')

  onMounted(() => {
    useCommon().scrollToTop()
    getArticleDetail()
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
      display: flex;
      flex-direction: column;
      gap: 8px;
      padding-top: 24px;
      margin-top: 32px;
      border-top: 1px solid var(--el-border-color);

      h2 {
        margin: 0;
        font-size: 18px;
        font-weight: 600;
      }
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
