<template>
  <div class="art-full-height">
    <ArtSearchBar
      v-model="searchForm"
      :items="formItems"
      :showExpand="false"
      @reset="handleReset"
      @search="handleSearch"
    />

    <ElCard class="art-table-card">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData" />
      <ArtTable :loading="loading" :data="data" :columns="columns" :pagination="pagination" />
    </ElCard>

    <ElDialog v-model="dialogVisible" :title="$t('moderation.report.handle')" width="520px" align-center>
      <ElForm ref="formRef" :model="form" :rules="rules" label-width="100px">
        <ElFormItem :label="$t('moderation.report.status')" prop="status">
          <ElSelect v-model="form.status" class="w-full">
            <ElOption :label="$t('moderation.report.statuses.PROCESSING')" value="PROCESSING" />
            <ElOption :label="$t('moderation.report.statuses.RESOLVED')" value="RESOLVED" />
            <ElOption :label="$t('moderation.report.statuses.REJECTED')" value="REJECTED" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem :label="$t('moderation.report.remark')" prop="remark">
          <ElInput v-model="form.remark" type="textarea" :rows="4" maxlength="500" show-word-limit />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" @click="submitHandle">{{ $t('common.save') }}</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { fetchContentReports, handleContentReport } from '@/api/moderation'
  import { useTable } from '@/hooks/core/useTable'
  import { ElMessage, ElSelect, ElTag, type FormInstance, type FormRules } from 'element-plus'
  import { useI18n } from 'vue-i18n'

  defineOptions({ name: 'ContentReport' })

  const { t } = useI18n()

  type ReportItem = Api.Moderation.ContentReportItem

  const searchForm = reactive<Api.Moderation.ContentReportSearchParams>({
    targetType: undefined,
    reasonType: undefined,
    status: undefined
  })

  const formItems = computed(() => [
    {
      label: t('moderation.report.targetType'),
      key: 'targetType',
      type: 'select',
      props: {
        clearable: true,
        options: [
          { label: t('moderation.report.targets.ARTICLE'), value: 'ARTICLE' },
          { label: t('moderation.report.targets.COMMENT'), value: 'COMMENT' },
          { label: t('moderation.report.targets.PRIVATE_MESSAGE'), value: 'PRIVATE_MESSAGE' }
        ]
      }
    },
    {
      label: t('moderation.report.status'),
      key: 'status',
      type: 'select',
      props: {
        clearable: true,
        options: [
          { label: t('moderation.report.statuses.PENDING'), value: 'PENDING' },
          { label: t('moderation.report.statuses.PROCESSING'), value: 'PROCESSING' },
          { label: t('moderation.report.statuses.RESOLVED'), value: 'RESOLVED' },
          { label: t('moderation.report.statuses.REJECTED'), value: 'REJECTED' }
        ]
      }
    },
    {
      label: t('moderation.report.reasonType'),
      key: 'reasonType',
      type: 'select',
      props: {
        clearable: true,
        options: reasonOptions.value
      }
    }
  ])

  const reasonOptions = computed(() =>
    [
      'SPAM',
      'HARASSMENT',
      'PERSONAL_INFO',
      'COPYRIGHT',
      'FRAUD',
      'MISLEADING',
      'FILE_RISK',
      'OUTDATED',
      'OFF_TOPIC',
      'LOW_QUALITY',
      'OTHER'
    ].map((value) => ({
      label: t(`moderation.report.reasons.${value}`),
      value
    }))
  )

  const dialogVisible = ref(false)
  const currentReport = ref<ReportItem | null>(null)
  const formRef = ref<FormInstance>()
  const form = reactive<Api.Moderation.ContentReportHandleParams>({
    status: 'PROCESSING',
    remark: ''
  })

  const rules: FormRules = {
    status: [{ required: true, message: t('moderation.report.statusRequired'), trigger: 'change' }]
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
    refreshData
  } = useTable({
    core: {
      apiFn: fetchContentReports,
      apiParams: {
        current: 1,
        size: 20
      },
      columnsFactory: () => [
        { prop: 'id', label: 'ID', width: 90 },
        {
          prop: 'targetType',
          label: t('moderation.report.targetType'),
          width: 150,
          formatter: (row) => h(ElTag, { type: 'info' }, () => targetLabel(row.targetType))
        },
        { prop: 'targetId', label: t('moderation.report.targetId'), width: 110 },
        {
          prop: 'reasonType',
          label: t('moderation.report.reasonType'),
          width: 150,
          formatter: (row) => reasonLabel(row.reasonType)
        },
        { prop: 'description', label: t('moderation.report.description'), minWidth: 220, showOverflowTooltip: true },
        { prop: 'reporterName', label: t('moderation.report.reporter'), width: 130 },
        {
          prop: 'status',
          label: t('moderation.report.status'),
          width: 120,
          formatter: (row) => h(ElTag, { type: statusTagType(row.status) }, () => statusLabel(row.status))
        },
        { prop: 'handlerName', label: t('moderation.report.handler'), width: 130 },
        { prop: 'handlingRemark', label: t('moderation.report.remark'), minWidth: 180, showOverflowTooltip: true },
        { prop: 'createTime', label: t('systemManage.common.createTime'), width: 180 },
        {
          prop: 'operation',
          label: t('common.operation'),
          width: 100,
          fixed: 'right',
          formatter: (row) =>
            h('div', { class: 'table-action-nowrap' }, [
              h(ArtButtonTable, { type: 'edit', auth: 'moderation:report:manage', onClick: () => openHandle(row) })
            ])
        }
      ]
    }
  })

  const handleSearch = (params: Api.Moderation.ContentReportSearchParams) => {
    replaceSearchParams(params)
    getData()
  }

  const handleReset = () => {
    resetSearchParams()
    getData()
  }

  const openHandle = (row: ReportItem) => {
    currentReport.value = row
    form.status = row.status === 'PENDING' ? 'PROCESSING' : row.status
    form.remark = row.handlingRemark || ''
    dialogVisible.value = true
    nextTick(() => formRef.value?.clearValidate())
  }

  const submitHandle = async () => {
    await formRef.value?.validate()
    if (!currentReport.value) return
    await handleContentReport(currentReport.value.id, form)
    ElMessage.success(t('common.success.update'))
    dialogVisible.value = false
    refreshData()
  }

  const targetLabel = (type: string) => t(`moderation.report.targets.${type}`)
  const reasonLabel = (type: string) => t(`moderation.report.reasons.${type}`)
  const statusLabel = (status: string) => t(`moderation.report.statuses.${status}`)

  const statusTagType = (status: string) => {
    if (status === 'PENDING') return 'danger'
    if (status === 'PROCESSING') return 'warning'
    if (status === 'RESOLVED') return 'success'
    return 'info'
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
