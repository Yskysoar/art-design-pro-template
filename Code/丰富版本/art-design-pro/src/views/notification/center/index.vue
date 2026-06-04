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
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElButton @click="readAll" v-ripple>{{ $t('notificationCenter.readAll') }}</ElButton>
        </template>
      </ArtTableHeader>
      <ArtTable :loading="loading" :data="data" :columns="columns" :pagination="pagination" />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { fetchNotifications, markAllNotificationsRead, markNotificationRead } from '@/api/notification'
  import { useTable } from '@/hooks/core/useTable'
  import { ElMessage, ElTag } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import { useRouter } from 'vue-router'

  defineOptions({ name: 'NotificationCenter' })

  const { t } = useI18n()
  const router = useRouter()

  type NotificationItem = Api.Notification.NotificationItem

  const searchForm = reactive<Api.Notification.NotificationSearchParams>({
    noticeType: undefined,
    unread: undefined
  })

  const typeOptions = computed(() => [
    { label: t('notificationCenter.types.SYSTEM'), value: 'SYSTEM' },
    { label: t('notificationCenter.types.FOLLOW'), value: 'FOLLOW' },
    { label: t('notificationCenter.types.PRIVATE_MESSAGE'), value: 'PRIVATE_MESSAGE' },
    { label: t('notificationCenter.types.COMMENT_REPLY'), value: 'COMMENT_REPLY' }
  ])

  const formItems = computed(() => [
    {
      label: t('notificationCenter.type'),
      key: 'noticeType',
      type: 'select',
      props: { clearable: true, options: typeOptions.value }
    },
    {
      label: t('notificationCenter.unreadOnly'),
      key: 'unread',
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
      apiFn: fetchNotifications,
      apiParams: {
        current: 1,
        size: 20
      },
      columnsFactory: () => [
        {
          prop: 'read',
          label: t('notificationCenter.status'),
          width: 90,
          formatter: (row) =>
            h(ElTag, { type: row.read ? 'info' : 'danger' }, () =>
              row.read ? t('notificationCenter.read') : t('notificationCenter.unread')
            )
        },
        {
          prop: 'noticeType',
          label: t('notificationCenter.type'),
          width: 140,
          formatter: (row) => h(ElTag, { type: tagType(row.noticeType) }, () => typeLabel(row.noticeType))
        },
        { prop: 'title', label: t('notificationCenter.title'), minWidth: 220, showOverflowTooltip: true },
        { prop: 'content', label: t('notificationCenter.content'), minWidth: 260, showOverflowTooltip: true },
        { prop: 'actorName', label: t('notificationCenter.actor'), width: 140 },
        { prop: 'createTime', label: t('systemManage.common.createTime'), width: 180 },
        {
          prop: 'operation',
          label: t('common.operation'),
          width: 130,
          fixed: 'right',
          formatter: (row) =>
            h('div', { class: 'table-action-nowrap' }, [
              !row.read ? h(ArtButtonTable, { type: 'view', onClick: () => readOne(row) }) : null,
              row.targetUrl ? h(ArtButtonTable, { type: 'view', onClick: () => openTarget(row) }) : null
            ])
        }
      ]
    }
  })

  const handleSearch = (params: Api.Notification.NotificationSearchParams) => {
    replaceSearchParams(params)
    getData()
  }

  const handleReset = () => {
    resetSearchParams()
    getData()
  }

  const readOne = async (row: NotificationItem) => {
    await markNotificationRead(row.id)
    ElMessage.success(t('notificationCenter.readSuccess'))
    refreshData()
  }

  const readAll = async () => {
    await markAllNotificationsRead()
    ElMessage.success(t('notificationCenter.readSuccess'))
    refreshData()
  }

  const openTarget = async (row: NotificationItem) => {
    if (!row.read) {
      await markNotificationRead(row.id)
    }
    if (row.targetUrl) {
      router.push(row.targetUrl)
    }
  }

  const typeLabel = (type: string) => t(`notificationCenter.types.${type}`)

  const tagType = (type: string) => {
    if (type === 'PRIVATE_MESSAGE') return 'success'
    if (type === 'COMMENT_REPLY') return 'warning'
    if (type === 'FOLLOW') return 'primary'
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
