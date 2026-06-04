<template>
  <div class="sensitive-word-page art-full-height">
    <ArtSearchBar
      v-model="searchForm"
      :items="formItems"
      :showExpand="false"
      @reset="handleReset"
      @search="handleSearch"
    />

    <ElCard class="art-table-card">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElButton v-auth="'system:sensitive-word'" @click="openDialog()" v-ripple>
              {{ $t('systemManage.sensitiveWord.add') }}
            </ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <ArtTable
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <ElDialog
      v-model="dialogVisible"
      :title="editingWord ? $t('systemManage.sensitiveWord.edit') : $t('systemManage.sensitiveWord.add')"
      width="520px"
      align-center
    >
      <ElForm ref="formRef" :model="form" :rules="rules" label-width="100px">
        <ElFormItem :label="$t('systemManage.sensitiveWord.word')" prop="word">
          <ElInput v-model.trim="form.word" maxlength="100" show-word-limit />
        </ElFormItem>
        <ElFormItem :label="$t('systemManage.sensitiveWord.enabledStatus')" prop="enabled">
          <ElSwitch v-model="form.enabled" :active-value="1" :inactive-value="0" />
        </ElFormItem>
        <ElFormItem :label="$t('systemManage.common.remark')" prop="remark">
          <ElInput v-model.trim="form.remark" maxlength="255" show-word-limit type="textarea" :rows="3" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="saving" @click="saveWord">{{ $t('common.save') }}</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import {
    createSensitiveWord,
    deleteSensitiveWord,
    fetchSensitiveWords,
    updateSensitiveWord,
    updateSensitiveWordStatus
  } from '@/api/article'
  import { ElMessageBox, ElTag, type FormInstance, type FormRules } from 'element-plus'
  import { useI18n } from 'vue-i18n'

  defineOptions({ name: 'SensitiveWord' })
  const { t } = useI18n()

  type SensitiveWordItem = Api.Article.SensitiveWordItem

  const saving = ref(false)
  const dialogVisible = ref(false)
  const formRef = ref<FormInstance>()
  const editingWord = ref<SensitiveWordItem>()

  const searchForm = reactive<Api.Article.SensitiveWordSearchParams>({
    current: 1,
    size: 20,
    word: undefined,
    enabled: undefined
  })

  const formItems = computed(() => [
    { label: t('systemManage.sensitiveWord.word'), key: 'word', type: 'input', props: { clearable: true } },
    {
      label: t('common.status'),
      key: 'enabled',
      type: 'select',
      props: {
        clearable: true,
        options: [
          { label: t('common.enabled'), value: 1 },
          { label: t('common.disabled'), value: 0 }
        ]
      }
    }
  ])

  const form = reactive<Api.Article.SensitiveWordSaveParams>({
    word: '',
    enabled: 1,
    remark: ''
  })

  const rules: FormRules = {
    word: [
      { required: true, message: t('systemManage.sensitiveWord.placeholders.word'), trigger: 'blur' },
      { max: 100, message: t('systemManage.sensitiveWord.rules.max'), trigger: 'blur' }
    ]
  }

  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    getData,
    replaceSearchParams,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData
  } = useTable({
    core: {
      apiFn: fetchSensitiveWords,
      apiParams: {
        current: 1,
        size: 20
      },
      columnsFactory: () => [
        { type: 'index', width: 60, label: t('systemManage.common.index') },
        { prop: 'word', label: t('systemManage.sensitiveWord.word'), minWidth: 180 },
        {
          prop: 'matchType',
          label: t('systemManage.sensitiveWord.matchType'),
          width: 120,
          formatter: (row) => (row.matchType === 'CONTAINS' ? t('systemManage.sensitiveWord.contains') : row.matchType)
        },
        {
          prop: 'enabled',
          label: t('common.status'),
          width: 100,
          formatter: (row) =>
            h(ElTag, { type: row.enabled === 1 ? 'success' : 'info' }, () =>
              row.enabled === 1 ? t('common.enabled') : t('common.disabled')
            )
        },
        { prop: 'remark', label: t('systemManage.common.remark'), minWidth: 220, showOverflowTooltip: true },
        { prop: 'updateTime', label: t('systemManage.common.updateTime'), width: 180 },
        {
          prop: 'operation',
          label: t('common.operation'),
          width: 156,
          fixed: 'right',
          formatter: (row) => {
            const enabled = row.enabled === 1
            return h('div', { class: 'table-action-nowrap' }, [
              h(ArtButtonTable, {
                type: 'edit',
                auth: 'system:sensitive-word',
                onClick: () => openDialog(row)
              }),
              h(ArtButtonTable, {
                icon: enabled ? 'ri:pause-circle-line' : 'ri:play-circle-line',
                iconClass: enabled ? 'bg-warning/12 text-warning' : 'bg-success/12 text-success',
                auth: 'system:sensitive-word',
                onClick: () => changeStatus(row, !enabled)
              }),
              h(ArtButtonTable, {
                type: 'delete',
                auth: 'system:sensitive-word',
                onClick: () => removeWord(row)
              })
            ])
          }
        }
      ]
    },
    transform: {
      dataTransformer: (records) => (Array.isArray(records) ? records : [])
    }
  })

  const handleSearch = (params: Record<string, any>) => {
    replaceSearchParams(params as Api.Article.SensitiveWordSearchParams)
    getData()
  }

  const handleReset = () => {
    resetSearchParams()
    getData()
  }

  const openDialog = (row?: SensitiveWordItem) => {
    editingWord.value = row
    Object.assign(form, {
      word: row?.word || '',
      enabled: row?.enabled ?? 1,
      remark: row?.remark || ''
    })
    dialogVisible.value = true
    nextTick(() => formRef.value?.clearValidate())
  }

  const saveWord = async () => {
    await formRef.value?.validate()
    saving.value = true
    try {
      if (editingWord.value) {
        await updateSensitiveWord(editingWord.value.id, form)
      } else {
        await createSensitiveWord(form)
      }
      dialogVisible.value = false
      ElMessage.success(editingWord.value ? t('common.success.update') : t('common.success.add'))
      refreshData()
    } finally {
      saving.value = false
    }
  }

  const changeStatus = async (row: SensitiveWordItem, checked: boolean) => {
    await updateSensitiveWordStatus(row.id, checked ? 1 : 0)
    ElMessage.success(t('common.success.status'))
    refreshData()
  }

  const removeWord = async (row: SensitiveWordItem) => {
    await ElMessageBox.confirm(t('systemManage.sensitiveWord.deleteConfirm', { name: row.word }), t('common.confirmDeleteTitle'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    await deleteSensitiveWord(row.id)
    ElMessage.success(t('common.success.delete'))
    refreshData()
  }
</script>

<style scoped>
  :deep(.table-action-nowrap) {
    display: inline-flex;
    flex-wrap: nowrap;
    gap: 8px;
    align-items: center;
    white-space: nowrap;
  }
</style>
