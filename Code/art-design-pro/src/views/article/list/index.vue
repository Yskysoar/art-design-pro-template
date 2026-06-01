<!-- 文章列表页面 -->
<template>
  <div class="page-content !mb-5">
    <ElRow justify="space-between" :gutter="10">
      <ElCol :lg="6" :md="6" :sm="14" :xs="16">
        <ElInput
          v-model.trim="query.title"
          :prefix-icon="Search"
          clearable
          placeholder="输入文章标题查询"
          @keyup.enter="searchArticle"
        />
      </ElCol>
      <ElCol :lg="12" :md="12" :sm="0" :xs="0">
        <div class="custom-segmented">
          <ElSegmented v-model="yearValue" :options="YEAR_OPTIONS" @change="searchArticle" />
        </div>
      </ElCol>
      <ElCol :lg="6" :md="6" :sm="10" :xs="6" style="display: flex; justify-content: end">
        <ElButton type="primary" @click="toAddArticle" v-auth="'article:publish:add'">
          新增文章
        </ElButton>
      </ElCol>
    </ElRow>

    <div class="mt-4 article-filter-row">
      <ElSelect v-model="query.categoryId" clearable placeholder="全部分类" @change="searchArticle">
        <ElOption v-for="item in articleTypes" :key="item.id" :label="item.name" :value="item.id" />
      </ElSelect>
      <ElSelect v-model="query.status" clearable placeholder="全部状态" @change="searchArticle">
        <ElOption label="草稿" value="DRAFT" />
        <ElOption label="已发布" value="PUBLISHED" />
        <ElOption label="已下架" value="OFFLINE" />
      </ElSelect>
    </div>

    <div class="mt-5" v-loading="loading">
      <div
        class="grid grid-cols-5 gap-5 max-2xl:grid-cols-4 max-xl:grid-cols-3 max-lg:grid-cols-2 max-sm:grid-cols-1"
      >
        <div
          class="group c-p overflow-hidden border border-g-300/60 rounded-custom-sm"
          v-for="item in articleList"
          :key="item.id"
          @click="toDetail(item)"
        >
          <div class="relative aspect-[16/9.5]">
            <ElImage
              class="flex align-center justify-center w-full h-full object-cover bg-gray-200"
              :src="item.coverUrl || item.home_img || fallbackCover"
              lazy
              fit="cover"
            />

            <span class="absolute top-1 right-1 bg-black/50 rounded text-xs px-1 py-0.5 text-white">
              {{ item.categoryName || item.type_name }}
            </span>
            <ElTag class="absolute bottom-1 left-1" size="small" :type="statusTagType(item.status)">
              {{ statusText(item.status) }}
            </ElTag>
          </div>
          <div class="px-2 py-1">
            <h2 class="text-base text-g-800 font-medium line-clamp-1">{{ item.title }}</h2>
            <p class="text-sm text-g-500 h-5 line-clamp-1">{{ item.summary || '暂无摘要' }}</p>
            <div class="flex-b w-full h-6 mt-1">
              <div class="flex-c text-g-500">
                <ArtSvgIcon icon="ri:time-line" class="mr-1 text-sm" />
                <span class="text-sm">{{ formatDate(item.createTime || item.create_time) }}</span>
                <div class="w-px h-3 bg-g-400 mx-3.5"></div>
                <ArtSvgIcon icon="ri:eye-line" class="mr-1 text-sm" />
                <span class="text-sm">{{ item.viewCount ?? item.count ?? 0 }}</span>
              </div>
              <ElButton
                class="opacity-0 group-hover:opacity-100"
                v-auth="'article:publish:edit'"
                size="small"
                @click.stop="toEdit(item)"
              >
                编辑
              </ElButton>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div style="margin-top: 16vh" v-if="showEmpty">
      <ElEmpty :description="`未找到相关数据 ${EmojiText[0]}`" />
    </div>

    <div style="display: flex; justify-content: center; margin-top: 20px">
      <ElPagination
        background
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :pager-count="9"
        layout="prev, pager, next, total, jumper"
        :total="pagination.total"
        :hide-on-single-page="true"
        @current-change="getArticleList"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
  import { Search } from '@element-plus/icons-vue'
  import { router } from '@/router'
  import { useDateFormat } from '@vueuse/core'
  import EmojiText from '@/utils/ui/emojo'
  import { fetchArticleList, fetchArticleTypes } from '@/api/article'
  import { useCommon } from '@/hooks/core/useCommon'

  defineOptions({ name: 'ArticleList' })

  const YEAR_OPTIONS = ['All', '2026', '2025', '2024', '2023', '2022', '2021']
  const fallbackCover = '/src/assets/images/cover/img1.webp'

  const articleTypes = ref<Api.Article.ArticleType[]>([])
  const articleList = ref<Api.Article.ArticleListItem[]>([])
  const loading = ref(false)
  const yearValue = ref('All')
  const query = reactive<Api.Article.ArticleSearchParams>({
    title: '',
    categoryId: undefined,
    status: undefined
  })
  const pagination = reactive({
    current: 1,
    size: 20,
    total: 0
  })

  const showEmpty = computed(() => articleList.value.length === 0 && !loading.value)

  const getArticleTypes = async () => {
    articleTypes.value = await fetchArticleTypes()
  }

  const getArticleList = async () => {
    loading.value = true
    try {
      const result = await fetchArticleList({
        ...query,
        current: pagination.current,
        size: pagination.size,
        year: yearValue.value === 'All' ? undefined : Number(yearValue.value)
      })
      articleList.value = result.records
      pagination.total = result.total
    } finally {
      loading.value = false
    }
  }

  const searchArticle = () => {
    pagination.current = 1
    getArticleList()
    useCommon().scrollToTop()
  }

  const toDetail = (item: Api.Article.ArticleListItem) => {
    router.push({ name: 'ArticleDetail', params: { id: item.id } })
  }

  const toEdit = (item: Api.Article.ArticleListItem) => {
    router.push({ name: 'ArticlePublish', query: { id: item.id } })
  }

  const toAddArticle = () => {
    router.push({ name: 'ArticlePublish' })
  }

  const statusText = (status: Api.Article.ArticleStatus) => {
    const map = {
      DRAFT: '草稿',
      PUBLISHED: '已发布',
      OFFLINE: '已下架'
    }
    return map[status]
  }

  const statusTagType = (status: Api.Article.ArticleStatus) => {
    const map = {
      DRAFT: 'info',
      PUBLISHED: 'success',
      OFFLINE: 'warning'
    } as const
    return map[status]
  }

  const formatDate = (date?: string) => {
    return date ? useDateFormat(date, 'YYYY-MM-DD').value : '-'
  }

  onMounted(async () => {
    await getArticleTypes()
    await getArticleList()
  })
</script>

<style lang="scss" scoped>
  .custom-segmented .el-segmented {
    height: 40px;
    padding: 6px;

    --el-border-radius-base: 8px;
  }

  .article-filter-row {
    display: flex;
    gap: 12px;

    .el-select {
      width: 180px;
    }
  }
</style>
