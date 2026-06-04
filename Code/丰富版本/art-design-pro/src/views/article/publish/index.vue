<!-- 文章发布页面 -->
<template>
  <div>
    <div class="max-w-250 mx-auto my-5">
      <ElRow :gutter="10">
        <ElCol :span="18">
          <ElInput
            v-model.trim="form.title"
            placeholder="请输入文章标题（最多120个字符）"
            maxlength="120"
            show-word-limit
          />
        </ElCol>
        <ElCol :span="6">
          <ElSelect v-model="form.categoryId" placeholder="请选择文章类型" filterable>
            <ElOption v-for="item in articleTypes" :key="item.id" :label="item.name" :value="item.id" />
          </ElSelect>
        </ElCol>
      </ElRow>

      <ElInput
        class="mt-2.5"
        v-model.trim="form.summary"
        type="textarea"
        maxlength="300"
        show-word-limit
        :rows="3"
        placeholder="请输入文章摘要"
      />

      <ArtWangEditor class="mt-2.5" v-model="form.contentHtml" />

      <div class="p-5 mt-5 art-card-xs">
        <h2 class="mb-5 text-xl font-medium">发布设置</h2>
        <ElForm label-width="80px">
          <ElFormItem label="封面">
            <div class="mt-2.5">
              <ElUpload
                :action="uploadImageUrl"
                :headers="uploadHeaders"
                :show-file-list="false"
                :on-success="onCoverSuccess"
                :on-error="onUploadError"
                :before-upload="beforeImageUpload"
              >
                <div
                  v-if="!form.coverUrl"
                  class="flex-cc flex-col w-65 h-40 border border-dashed border-[#d9d9d9] rounded-md"
                >
                  <ElIcon class="!text-xl !text-g-600"><Plus /></ElIcon>
                  <div class="mt-2 text-sm text-g-600">点击上传封面</div>
                </div>
                <img v-else :src="form.coverUrl" class="block w-65 h-40 object-cover rounded-md" />
              </ElUpload>
              <div class="mt-2 text-xs text-g-700">建议尺寸 16:9，jpg/png/webp/gif，最大 5MB</div>
            </div>
          </ElFormItem>

          <ElFormItem label="附件">
            <ElUpload
              :http-request="uploadFile"
              :file-list="fileList"
              :on-remove="removeAttachment"
              multiple
            >
              <ElButton>上传附件</ElButton>
            </ElUpload>
          </ElFormItem>

          <ElFormItem label="状态">
            <ElRadioGroup v-model="form.status">
              <ElRadioButton label="DRAFT">草稿</ElRadioButton>
              <ElRadioButton label="PUBLISHED">发布</ElRadioButton>
              <ElRadioButton label="OFFLINE">下架</ElRadioButton>
            </ElRadioGroup>
          </ElFormItem>

          <ElFormItem label="可见">
            <ElSwitch v-model="form.visible" />
          </ElFormItem>
        </ElForm>

        <div class="flex justify-end gap-3">
          <ElButton @click="router.back()">返回</ElButton>
          <ElButton @click="previewVisible = true" :disabled="!form.title">预览</ElButton>
          <ElButton type="primary" :loading="saving" @click="submit" class="w-25">
            {{ articleId ? '保存' : '发布' }}
          </ElButton>
        </div>
      </div>
    </div>

    <!-- 预览弹窗 -->
    <ElDialog
      v-model="previewVisible"
      title="文章预览"
      width="760px"
      top="5vh"
      align-center
      :close-on-click-modal="false"
    >
      <div class="preview-body" style="max-height: 65vh; overflow-y: auto; padding: 0 4px;">
        <h1 style="font-size: 22px; font-weight: 600; margin-bottom: 12px; line-height: 1.4;">{{ form.title }}</h1>
        <p v-if="form.summary" style="color: #666; margin-bottom: 20px; line-height: 1.7; font-size: 14px;">{{ form.summary }}</p>
        <div v-if="form.coverUrl" style="margin-bottom: 20px;">
          <img :src="form.coverUrl" style="width: 100%; max-height: 340px; object-fit: cover; border-radius: 8px;" alt="封面" />
        </div>
        <div class="markdown-body" v-html="sanitizedPreviewHtml" style="font-size: 15px;"></div>
      </div>
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 10px;">
          <ElButton @click="previewVisible = false">返回</ElButton>
          <ElButton type="primary" :loading="saving" @click="previewVisible = false; submit()">发布</ElButton>
        </div>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
  import { Plus } from '@element-plus/icons-vue'
  import type { UploadFile, UploadRequestOptions, UploadUserFile } from 'element-plus'
  import { router } from '@/router'
  import {
    createArticle,
    fetchArticleDetail,
    fetchArticleTypes,
    updateArticle
  } from '@/api/article'
  import { uploadAttachment } from '@/api/upload'
  import { useUserStore } from '@/store/modules/user'
  import EmojiText from '@/utils/ui/emojo'
  import { useCommon } from '@/hooks/core/useCommon'
  import { sanitizeRichHtml } from '@/utils/security/html'

  defineOptions({ name: 'ArticlePublish' })

  const MAX_IMAGE_SIZE = 5

  const route = useRoute()
  const userStore = useUserStore()
  const articleId = computed(() => Number(route.query.id || 0))
  const apiBase = (import.meta.env.VITE_API_URL || '').replace(/\/$/, '')

  const uploadImageUrl = `${apiBase}/api/common/upload`
  const uploadHeaders = computed(() => ({ Authorization: `Bearer ${userStore.accessToken}` }))
  const articleTypes = ref<Api.Article.ArticleType[]>([])
  const previewVisible = ref(false)
  const sanitizedPreviewHtml = computed(() => sanitizeRichHtml(form.contentHtml || ''))
  const saving = ref(false)
  const fileList = ref<UploadUserFile[]>([])
  const form = reactive<Api.Article.ArticleSaveParams>({
    title: '',
    categoryId: null as number | null,
    coverUrl: '',
    summary: '',
    contentHtml: '',
    visible: true,
    status: 'PUBLISHED',
    attachmentIds: []
  })

  const getArticleTypes = async () => {
    articleTypes.value = await fetchArticleTypes()
  }

  const getArticleDetail = async () => {
    if (!articleId.value) return
    const detail = await fetchArticleDetail(articleId.value)
    form.title = detail.title
    form.categoryId = detail.categoryId
    form.coverUrl = detail.coverUrl || ''
    form.summary = detail.summary || ''
    form.contentHtml = detail.contentHtml || detail.html_content || ''
    form.visible = detail.visible
    form.status = detail.status
    form.attachmentIds = detail.attachments.map((item) => item.id)
    fileList.value = detail.attachments.map((item) => ({
      name: item.originalName,
      url: item.url,
      uid: item.id
    }))
  }

  const validateArticle = () => {
    if (!form.title) return ElMessage.error('请输入文章标题'), false
    if (!form.categoryId) return ElMessage.error('请选择文章类型'), false
    if (!stripHtml(form.contentHtml)) return ElMessage.error('请输入文章内容'), false
    return true
  }

  const submit = async () => {
    if (!validateArticle()) return
    saving.value = true
    try {
      if (articleId.value) {
        await updateArticle(articleId.value, form)
        ElMessage.success('文章已保存')
      } else {
        await createArticle(form)
        ElMessage.success('文章已发布')
      }
      router.push({ name: 'ArticleList' })
    } finally {
      saving.value = false
    }
  }

  const beforeImageUpload = (file: File) => {
    const isImage = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'].includes(file.type)
    const isLtMax = file.size / 1024 / 1024 <= MAX_IMAGE_SIZE
    if (!isImage) ElMessage.error('仅支持 jpg/png/webp/gif 图片')
    if (!isLtMax) ElMessage.error(`图片大小不能超过 ${MAX_IMAGE_SIZE}MB`)
    return isImage && isLtMax
  }

  const onCoverSuccess = (response: { code: number; data: Api.Upload.UploadResponse }) => {
    form.coverUrl = response.data.url
    ElMessage.success(`图片上传成功 ${EmojiText[200]}`)
  }

  const onUploadError = () => {
    ElMessage.error(`图片上传失败 ${EmojiText[500]}`)
  }

  const uploadFile = async (options: UploadRequestOptions) => {
    try {
      const response = await uploadAttachment(options.file)
      form.attachmentIds.push(response.id)
      fileList.value.push({
        name: response.fileName,
        url: response.url,
        uid: response.id
      })
      options.onSuccess(response)
      ElMessage.success('附件上传成功')
    } catch (error) {
      ElMessage.error('附件上传失败')
    }
  }

  const removeAttachment = (uploadFile: UploadFile) => {
    form.attachmentIds = form.attachmentIds.filter((id) => id !== Number(uploadFile.uid))
  }

  const stripHtml = (html: string) => {
    const container = document.createElement('div')
    container.innerHTML = sanitizeRichHtml(html)
    return container.textContent?.trim()
  }

  onMounted(async () => {
    useCommon().scrollToTop()
    await getArticleTypes()
    await getArticleDetail()
  })
</script>
