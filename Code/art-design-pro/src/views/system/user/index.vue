<template>
  <div class="user-page art-full-height">
    <UserSearch v-model="searchForm" @search="handleSearch" @reset="resetSearchParams" />

    <ElCard class="art-table-card">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElButton v-auth="'system:user:add'" @click="showDialog('add')" v-ripple>{{ $t('systemManage.user.add') }}</ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <ArtTable
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />

      <UserDialog
        v-model:visible="dialogVisible"
        :type="dialogType"
        :user-data="currentUserData"
        @submit="handleDialogSubmit"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { fetchDeleteUser, fetchGetOrgTree, fetchGetUserList, fetchUpdateUserStatus } from '@/api/system-manage'
  import UserSearch from './modules/user-search.vue'
  import UserDialog from './modules/user-dialog.vue'
  import { ElTag, ElMessageBox, ElImage } from 'element-plus'
  import { DialogType } from '@/types'
  import defaultAvatar from '@/assets/images/user/avatar.webp'
  import { useI18n } from 'vue-i18n'

  defineOptions({ name: 'User' })
  const { t } = useI18n()

  type UserListItem = Api.SystemManage.UserListItem

  const dialogType = ref<DialogType>('add')
  const dialogVisible = ref(false)
  const currentUserData = ref<Partial<UserListItem>>({})
  const selectedRows = ref<UserListItem[]>([])
  const orgNameMap = ref<Record<number, string>>({})

  const searchForm = ref({
    userName: undefined,
    userGender: undefined,
    userPhone: undefined,
    userEmail: undefined,
    status: undefined
  })

  const USER_STATUS_CONFIG = {
    '1': { type: 'success' as const, text: t('common.enabled') },
    '2': { type: 'info' as const, text: t('systemManage.user.offline') },
    '3': { type: 'warning' as const, text: t('systemManage.user.abnormal') },
    '4': { type: 'danger' as const, text: t('common.disabled') }
  } as const

  const getUserStatusConfig = (status: string) => {
    return (
      USER_STATUS_CONFIG[status as keyof typeof USER_STATUS_CONFIG] || {
        type: 'info' as const,
        text: t('common.unknown')
      }
    )
  }

  const buildOrgNameMap = (items: Api.SystemManage.OrgTreeItem[], map: Record<number, string>) => {
    items.forEach((item) => {
      map[item.id] = item.orgName
      if (Array.isArray(item.children) && item.children.length) {
        buildOrgNameMap(item.children, map)
      }
    })
  }

  const loadOrgNames = async () => {
    const orgs = await fetchGetOrgTree()
    const map: Record<number, string> = {}
    buildOrgNameMap(orgs, map)
    orgNameMap.value = map
  }

  const formatOrgNames = (orgIds: number[]) => {
    if (!Array.isArray(orgIds) || !orgIds.length) {
      return '-'
    }
    return orgIds.map((id) => orgNameMap.value[id] || `#${id}`).join('、')
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
      apiFn: fetchGetUserList,
      apiParams: {
        current: 1,
        size: 20,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'selection' },
        { type: 'index', width: 60, label: t('systemManage.common.index') },
        {
          prop: 'userInfo',
          label: t('systemManage.user.user'),
          width: 280,
          formatter: (row) =>
            h('div', { class: 'user flex-c' }, [
              h(ElImage, {
                class: 'size-9.5 rounded-md',
                src: row.avatar || defaultAvatar,
                previewSrcList: [row.avatar || defaultAvatar],
                previewTeleported: true
              }),
              h('div', { class: 'ml-2' }, [
                h('p', { class: 'user-name' }, row.userName),
                h('p', { class: 'email' }, row.userEmail)
              ])
            ])
        },
        { prop: 'userGender', label: t('systemManage.user.gender'), sortable: true },
        {
          prop: 'userPhone',
          label: t('systemManage.user.phone'),
          width: 150,
          formatter: (row) => h('span', { style: { whiteSpace: 'nowrap' } }, row.userPhone || '-')
        },
        {
          prop: 'orgIds',
          label: t('systemManage.user.org'),
          minWidth: 180,
          formatter: (row) => h('span', formatOrgNames(row.orgIds))
        },
        {
          prop: 'status',
          label: t('common.status'),
          formatter: (row) => {
            const statusConfig = getUserStatusConfig(row.status)
            return h(ElTag, { type: statusConfig.type }, () => statusConfig.text)
          }
        },
        {
          prop: 'createTime',
          label: t('systemManage.common.createTime'),
          width: 190,
          sortable: true,
          showOverflowTooltip: true,
          formatter: (row) => h('span', { class: 'table-nowrap-cell' }, row.createTime || '-')
        },
        {
          prop: 'operation',
          label: t('common.operation'),
          width: 170,
          fixed: 'right',
          formatter: (row) => {
            const disabled = row.status === '4'
            return h('div', { class: 'table-action-nowrap' }, [
              h(ArtButtonTable, {
                type: 'edit',
                auth: 'system:user:edit',
                onClick: () => showDialog('edit', row)
              }),
              h(ArtButtonTable, {
                icon: disabled ? 'ri:play-circle-line' : 'ri:pause-circle-line',
                iconClass: disabled ? 'bg-success/12 text-success' : 'bg-warning/12 text-warning',
                auth: 'system:user:edit',
                onClick: () => toggleUserStatus(row)
              }),
              h(ArtButtonTable, {
                type: 'delete',
                auth: 'system:user:delete',
                onClick: () => deleteUser(row)
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

  const handleSearch = (params: Api.SystemManage.UserSearchParams) => {
    replaceSearchParams(params)
    getData()
  }

  const showDialog = (type: DialogType, row?: UserListItem): void => {
    dialogType.value = type
    currentUserData.value = row || {}
    nextTick(() => {
      dialogVisible.value = true
    })
  }

  const deleteUser = (row: UserListItem): void => {
    ElMessageBox.confirm(t('systemManage.user.deleteConfirm'), t('systemManage.user.deleteTitle'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'error'
    }).then(async () => {
      await fetchDeleteUser(row.id)
      ElMessage.success(t('common.success.delete'))
      refreshData()
    })
  }

  const toggleUserStatus = (row: UserListItem): void => {
    const disabled = row.status === '4'
    const nextStatus = disabled ? '1' : '4'
    const actionText = disabled ? t('common.enabled') : t('common.disabled')
    ElMessageBox.confirm(t('systemManage.user.statusConfirm', { action: actionText }), t('systemManage.user.statusTitle', { action: actionText }), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: disabled ? 'success' : 'warning'
    }).then(async () => {
      await fetchUpdateUserStatus(row.id, { status: nextStatus })
      ElMessage.success(t('systemManage.user.statusSuccess', { action: actionText }))
      refreshData()
    })
  }

  const handleDialogSubmit = async () => {
    dialogVisible.value = false
    currentUserData.value = {}
    refreshData()
  }

  const handleSelectionChange = (selection: UserListItem[]): void => {
    selectedRows.value = selection
  }

  onMounted(() => {
    loadOrgNames()
  })
</script>

<style scoped>
  :deep(.table-action-nowrap) {
    display: inline-flex;
    flex-wrap: nowrap;
    gap: 8px;
    align-items: center;
    white-space: nowrap;
  }

  :deep(.table-nowrap-cell) {
    white-space: nowrap;
  }
</style>
