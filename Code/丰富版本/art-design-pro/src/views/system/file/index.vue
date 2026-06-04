<template>
  <div class="art-full-height">
    <ArtSearchBar
      v-model="searchForm"
      :items="formItems"
      :showExpand="true"
      @reset="handleReset"
      @search="handleSearch"
    />

    <ElCard class="art-table-card">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData" />
      <ArtTable :loading="loading" :data="data" :columns="columns" :pagination="pagination" />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { fetchDeleteFileResource, fetchGetFileResourceList } from '@/api/system-manage'
  import { ElImage, ElMessage, ElMessageBox, ElTag } from 'element-plus'
  import { useI18n } from 'vue-i18n'

  defineOptions({ name: 'FileResource' })

  const { t } = useI18n()

  type FileResourceItem = Api.SystemManage.FileResourceItem

  const searchForm = reactive<Api.SystemManage.FileResourceSearchParams>({
    keyword: undefined,
    extension: undefined,
    contentType: undefined,
    storageType: undefined,
    referenced: undefined,
    minSize: undefined,
    maxSize: undefined,
    createTimeStart: undefined,
    createTimeEnd: undefined
  })

  const formItems = computed(() => [
    { label: t('systemManage.file.keyword'), key: 'keyword', type: 'input', props: { clearable: true } },
    { label: t('systemManage.file.extension'), key: 'extension', type: 'input', props: { clearable: true } },
    {
      label: t('systemManage.file.contentType'),
      key: 'contentType',
      labelWidth: 92,
      type: 'input',
      props: { clearable: true }
    },
    { label: t('systemManage.file.storageType'), key: 'storageType', type: 'input', props: { clearable: true } },
    {
      label: t('systemManage.file.referenced'),
      key: 'referenced',
      type: 'select',
      props: {
        clearable: true,
        options: [
          { label: t('common.yes'), value: true },
          { label: t('common.no'), value: false }
        ]
      }
    }
  ])

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
      apiFn: fetchGetFileResourceList,
      apiParams: {
        current: 1,
        size: 20
      },
      columnsFactory: () => [
        {
          prop: 'preview',
          label: t('systemManage.file.preview'),
          width: 82,
          formatter: (row) =>
            isImage(row)
              ? h(ElImage, {
                  class: 'file-preview',
                  src: row.url,
                  fit: 'cover',
                  previewSrcList: [row.url],
                  previewTeleported: true
                })
              : h('div', { class: 'file-type-box' }, fileTypeLabel(row))
        },
        {
          prop: 'originalName',
          label: t('systemManage.file.originalName'),
          minWidth: 240,
          showOverflowTooltip: true
        },
        {
          prop: 'extension',
          label: t('systemManage.file.extension'),
          width: 100,
          formatter: (row) => h(ElTag, { type: tagType(row.extension) }, () => row.extension?.toUpperCase() || '-')
        },
        {
          prop: 'contentType',
          label: t('systemManage.file.contentType'),
          minWidth: 240,
          showOverflowTooltip: true,
          formatter: (row) => h('span', { class: 'file-mime-nowrap' }, row.contentType || '-')
        },
        {
          prop: 'size',
          label: t('systemManage.file.size'),
          width: 110,
          formatter: (row) => formatFileSize(row.size)
        },
        { prop: 'uploaderName', label: t('systemManage.file.uploader'), width: 130 },
        {
          prop: 'referenced',
          label: t('systemManage.file.referenced'),
          width: 120,
          formatter: (row) =>
            h(ElTag, { type: row.referenced ? 'warning' : 'success' }, () =>
              row.referenced
                ? t('systemManage.file.referenceCount', { count: row.referenceCount })
                : t('systemManage.file.unreferenced')
            )
        },
        { prop: 'storageType', label: t('systemManage.file.storageType'), width: 120 },
        { prop: 'createTime', label: t('systemManage.common.createTime'), width: 180 },
        {
          prop: 'operation',
          label: t('common.operation'),
          width: 150,
          fixed: 'right',
          formatter: (row) =>
            h('div', { class: 'table-action-nowrap' }, [
              h(ArtButtonTable, { type: 'view', onClick: () => openFile(row) }),
              !row.referenced
                ? h(ArtButtonTable, {
                    type: 'delete',
                    auth: 'system:file:manage',
                    onClick: () => deleteFile(row)
                  })
                : null
            ])
        }
      ]
    }
  })

  const handleSearch = (params: Api.SystemManage.FileResourceSearchParams) => {
    replaceSearchParams(params)
    getData()
  }

  const handleReset = () => {
    resetSearchParams()
    getData()
  }

  const openFile = (row: FileResourceItem) => {
    window.open(row.url, '_blank', 'noopener,noreferrer')
  }

  const deleteFile = async (row: FileResourceItem) => {
    await ElMessageBox.confirm(
      t('systemManage.file.deleteConfirm', { name: row.originalName }),
      t('common.confirmDeleteTitle'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
    await fetchDeleteFileResource(row.id)
    ElMessage.success(t('common.success.delete'))
    refreshData()
  }

  const isImage = (row: FileResourceItem) => row.contentType?.startsWith('image/')

  const fileTypeLabel = (row: FileResourceItem) => {
    const extension = row.extension?.toUpperCase()
    return extension && extension.length <= 4 ? extension : 'FILE'
  }

  const tagType = (extension?: string) => {
    if (!extension) return 'info'
    if (['jpg', 'jpeg', 'png', 'webp', 'gif'].includes(extension)) return 'success'
    if (['pdf'].includes(extension)) return 'danger'
    if (['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx'].includes(extension)) return 'primary'
    return 'info'
  }

  const formatFileSize = (size?: number) => {
    if (!size) return '-'
    if (size < 1024) return `${size}B`
    if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)}KB`
    return `${(size / 1024 / 1024).toFixed(1)}MB`
  }
</script>

<style scoped>
  .file-preview {
    width: 44px;
    height: 44px;
    overflow: hidden;
    border-radius: 4px;
  }

  .file-type-box {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 44px;
    height: 44px;
    font-size: 12px;
    font-weight: 700;
    color: #fff;
    background: #8f99a8;
    border-radius: 4px;
  }

  :deep(.table-action-nowrap) {
    display: inline-flex;
    flex-wrap: nowrap;
    gap: 8px;
    align-items: center;
    white-space: nowrap;
  }

  :deep(.file-mime-nowrap) {
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
</style>
