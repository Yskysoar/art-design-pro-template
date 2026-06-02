<template>
  <div class="sensitive-word-page">
    <div class="page-header">
      <div>
        <h2>评论敏感词</h2>
        <p>维护评论发布前的敏感词拦截规则，命中后评论不会写入数据库。</p>
      </div>
      <ElButton type="primary" @click="openDialog()">
        <template #icon><Plus /></template>
        新增
      </ElButton>
    </div>

    <div class="toolbar">
      <ElInput
        v-model.trim="query.word"
        class="search-input"
        clearable
        placeholder="搜索敏感词"
        @keyup.enter="loadWords"
      />
      <ElSelect v-model="query.enabled" class="status-select" clearable placeholder="状态">
        <ElOption label="启用" :value="1" />
        <ElOption label="禁用" :value="0" />
      </ElSelect>
      <ElButton type="primary" @click="loadWords">
        <template #icon><Search /></template>
        查询
      </ElButton>
    </div>

    <ElTable v-loading="loading" :data="words" row-key="id">
      <ElTableColumn prop="word" label="敏感词" min-width="180" />
      <ElTableColumn prop="matchType" label="匹配方式" width="120" />
      <ElTableColumn label="状态" width="120">
        <template #default="{ row }">
          <ElSwitch
            :model-value="row.enabled === 1"
            @change="(checked) => changeStatus(row as Api.Article.SensitiveWordItem, checked as boolean)"
          />
        </template>
      </ElTableColumn>
      <ElTableColumn prop="remark" label="备注" min-width="220" show-overflow-tooltip />
      <ElTableColumn prop="updateTime" label="更新时间" width="180" />
      <ElTableColumn label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <ElButton link type="primary" @click="openDialog(row as Api.Article.SensitiveWordItem)">编辑</ElButton>
          <ElButton link type="danger" @click="removeWord(row as Api.Article.SensitiveWordItem)">删除</ElButton>
        </template>
      </ElTableColumn>
    </ElTable>

    <div class="pagination">
      <ElPagination
        v-model:current-page="query.current"
        v-model:page-size="query.size"
        background
        layout="total, sizes, prev, pager, next"
        :total="total"
        @current-change="loadWords"
        @size-change="loadWords"
      />
    </div>

    <ElDialog v-model="dialogVisible" :title="editingWord ? '编辑敏感词' : '新增敏感词'" width="460px">
      <ElForm ref="formRef" :model="form" :rules="rules" label-position="top">
        <ElFormItem label="敏感词" prop="word">
          <ElInput v-model.trim="form.word" maxlength="100" show-word-limit />
        </ElFormItem>
        <ElFormItem label="启用状态" prop="enabled">
          <ElSwitch v-model="form.enabled" :active-value="1" :inactive-value="0" />
        </ElFormItem>
        <ElFormItem label="备注" prop="remark">
          <ElInput v-model.trim="form.remark" maxlength="255" show-word-limit type="textarea" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="saving" @click="saveWord">保存</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
  import {
    createSensitiveWord,
    deleteSensitiveWord,
    fetchSensitiveWords,
    updateSensitiveWord,
    updateSensitiveWordStatus
  } from '@/api/article'
  import { Plus, Search } from '@element-plus/icons-vue'
  import { ElMessageBox, type FormInstance, type FormRules } from 'element-plus'

  defineOptions({ name: 'ArticleComment' })

  const loading = ref(false)
  const saving = ref(false)
  const dialogVisible = ref(false)
  const formRef = ref<FormInstance>()
  const words = ref<Api.Article.SensitiveWordItem[]>([])
  const total = ref(0)
  const editingWord = ref<Api.Article.SensitiveWordItem>()

  const query = reactive<Api.Article.SensitiveWordSearchParams>({
    current: 1,
    size: 10,
    word: '',
    enabled: undefined
  })

  const form = reactive<Api.Article.SensitiveWordSaveParams>({
    word: '',
    enabled: 1,
    remark: ''
  })

  const rules: FormRules = {
    word: [
      { required: true, message: '请输入敏感词', trigger: 'blur' },
      { max: 100, message: '敏感词不能超过100字', trigger: 'blur' }
    ]
  }

  const loadWords = async () => {
    loading.value = true
    try {
      const result = await fetchSensitiveWords(query)
      words.value = result.records
      total.value = result.total
    } finally {
      loading.value = false
    }
  }

  const openDialog = (row?: Api.Article.SensitiveWordItem) => {
    editingWord.value = row
    form.word = row?.word ?? ''
    form.enabled = row?.enabled ?? 1
    form.remark = row?.remark ?? ''
    dialogVisible.value = true
    nextTick(() => formRef.value?.clearValidate())
  }

  const saveWord = async () => {
    if (!formRef.value) return
    await formRef.value.validate()
    saving.value = true
    try {
      if (editingWord.value) {
        await updateSensitiveWord(editingWord.value.id, form)
      } else {
        await createSensitiveWord(form)
      }
      dialogVisible.value = false
      ElMessage.success('保存成功')
      await loadWords()
    } finally {
      saving.value = false
    }
  }

  const changeStatus = async (row: Api.Article.SensitiveWordItem, checked: boolean) => {
    await updateSensitiveWordStatus(row.id, checked ? 1 : 0)
    ElMessage.success('状态已更新')
    await loadWords()
  }

  const removeWord = async (row: Api.Article.SensitiveWordItem) => {
    await ElMessageBox.confirm(`确认删除敏感词“${row.word}”？`, '提示', {
      type: 'warning'
    })
    await deleteSensitiveWord(row.id)
    ElMessage.success('删除成功')
    await loadWords()
  }

  onMounted(loadWords)
</script>

<style scoped>
  .sensitive-word-page {
    padding: 24px;
  }

  .page-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20px;
  }

  .page-header h2 {
    margin: 0;
    font-size: 22px;
    font-weight: 600;
  }

  .page-header p {
    margin: 6px 0 0;
    color: var(--el-text-color-secondary);
  }

  .toolbar {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;
  }

  .search-input {
    width: 260px;
  }

  .status-select {
    width: 140px;
  }

  .pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
</style>
