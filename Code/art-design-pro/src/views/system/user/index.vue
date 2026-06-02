<template>
  <div class="user-page art-full-height">
    <UserSearch v-model="searchForm" @search="handleSearch" @reset="resetSearchParams" />

    <ElCard class="art-table-card">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElButton v-auth="'system:user:add'" @click="showDialog('add')" v-ripple>新增用户</ElButton>
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

  defineOptions({ name: 'User' })

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
    '1': { type: 'success' as const, text: '启用' },
    '2': { type: 'info' as const, text: '离线' },
    '3': { type: 'warning' as const, text: '异常' },
    '4': { type: 'danger' as const, text: '禁用' }
  } as const

  const getUserStatusConfig = (status: string) => {
    return (
      USER_STATUS_CONFIG[status as keyof typeof USER_STATUS_CONFIG] || {
        type: 'info' as const,
        text: '未知'
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
        { type: 'index', width: 60, label: '序号' },
        {
          prop: 'userInfo',
          label: '用户',
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
        { prop: 'userGender', label: '性别', sortable: true },
        {
          prop: 'userPhone',
          label: '手机号',
          width: 150,
          formatter: (row) => h('span', { style: { whiteSpace: 'nowrap' } }, row.userPhone || '-')
        },
        {
          prop: 'orgIds',
          label: '组织',
          minWidth: 180,
          formatter: (row) => h('span', formatOrgNames(row.orgIds))
        },
        {
          prop: 'status',
          label: '状态',
          formatter: (row) => {
            const statusConfig = getUserStatusConfig(row.status)
            return h(ElTag, { type: statusConfig.type }, () => statusConfig.text)
          }
        },
        {
          prop: 'createTime',
          label: '创建日期',
          width: 190,
          sortable: true,
          showOverflowTooltip: true,
          formatter: (row) => h('span', { class: 'table-nowrap-cell' }, row.createTime || '-')
        },
        {
          prop: 'operation',
          label: '操作',
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
    ElMessageBox.confirm('确定要删除该用户吗？该操作会逻辑删除用户；如需临时停用账号，请使用禁用按钮。', '删除用户', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    }).then(async () => {
      await fetchDeleteUser(row.id)
      ElMessage.success('删除成功')
      refreshData()
    })
  }

  const toggleUserStatus = (row: UserListItem): void => {
    const disabled = row.status === '4'
    const nextStatus = disabled ? '1' : '4'
    const actionText = disabled ? '启用' : '禁用'
    ElMessageBox.confirm(`确定要${actionText}该用户吗？`, `${actionText}用户`, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: disabled ? 'success' : 'warning'
    }).then(async () => {
      await fetchUpdateUserStatus(row.id, { status: nextStatus })
      ElMessage.success(`${actionText}成功`)
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
