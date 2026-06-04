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
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="getOrgTree">
        <template #left>
          <ElSpace wrap>
            <ElButton v-auth="'system:org:manage'" @click="showDialog('add')" v-ripple>{{ $t('systemManage.org.add') }}</ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <ArtTable
        rowKey="id"
        :loading="loading"
        :data="filteredData"
        :columns="columns"
        :stripe="false"
        :tree-props="{ children: 'children' }"
        :default-expand-all="true"
      />
    </ElCard>

    <ElDialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? $t('systemManage.org.add') : $t('systemManage.org.edit')"
      width="560px"
      align-center
    >
      <ElForm ref="formRef" :model="form" :rules="rules" label-width="100px">
        <ElFormItem :label="$t('systemManage.org.parent')" prop="parentId">
          <ElTreeSelect
            v-model="form.parentId"
            :data="parentOptions"
            :props="{ label: 'orgName', children: 'children' }"
            node-key="id"
            value-key="id"
            check-strictly
            clearable
            filterable
            :placeholder="$t('systemManage.org.rootPlaceholder')"
            style="width: 100%"
          />
        </ElFormItem>
        <ElFormItem :label="$t('systemManage.org.orgName')" prop="orgName">
          <ElInput v-model="form.orgName" maxlength="100" show-word-limit />
        </ElFormItem>
        <ElFormItem :label="$t('systemManage.org.orgCode')" prop="orgCode">
          <ElInput v-model="form.orgCode" maxlength="100" show-word-limit />
        </ElFormItem>
        <ElFormItem :label="$t('systemManage.org.orgType')" prop="orgType">
          <ElSelect v-model="form.orgType" style="width: 100%">
            <ElOption :label="$t('systemManage.org.group')" value="GROUP" />
            <ElOption :label="$t('systemManage.org.dept')" value="DEPT" />
            <ElOption :label="$t('systemManage.org.club')" value="CLUB" />
            <ElOption :label="$t('systemManage.org.merchant')" value="MERCHANT" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem :label="$t('systemManage.org.sort')" prop="sort">
          <ElInputNumber v-model="form.sort" :min="0" controls-position="right" style="width: 100%" />
        </ElFormItem>
        <ElFormItem :label="$t('systemManage.org.enabled')">
          <ElSwitch v-model="form.enabled" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" @click="submitForm">{{ $t('common.save') }}</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTableColumns } from '@/hooks/core/useTableColumns'
  import {
    fetchCreateOrg,
    fetchDeleteOrg,
    fetchGetOrgTree,
    fetchUpdateOrg
  } from '@/api/system-manage'
  import { ElMessageBox, ElTag, type FormInstance, type FormRules } from 'element-plus'
  import { useI18n } from 'vue-i18n'

  defineOptions({ name: 'Org' })
  const { t } = useI18n()

  type OrgItem = Api.SystemManage.OrgTreeItem

  const loading = ref(false)
  const tableData = ref<OrgItem[]>([])
  const searchForm = reactive({
    keyword: '',
    enabled: undefined as boolean | undefined
  })
  const appliedFilters = reactive({ ...searchForm })
  const dialogVisible = ref(false)
  const dialogType = ref<'add' | 'edit'>('add')
  const currentOrg = ref<OrgItem | null>(null)
  const formRef = ref<FormInstance>()

  const form = reactive<Api.SystemManage.OrgSaveParams>({
    parentId: 0,
    orgName: '',
    orgCode: '',
    orgType: 'GROUP',
    sort: 0,
    enabled: true
  })

  const formItems = computed(() => [
    { label: t('systemManage.org.keyword'), key: 'keyword', type: 'input', props: { clearable: true } },
    {
      label: t('common.status'),
      key: 'enabled',
      type: 'select',
      props: {
        clearable: true,
        options: [
          { label: t('common.enabled'), value: true },
          { label: t('systemManage.org.stopped'), value: false }
        ]
      }
    }
  ])

  const rules: FormRules = {
    orgName: [{ required: true, message: t('systemManage.org.placeholders.orgName'), trigger: 'blur' }],
    orgCode: [{ required: true, message: t('systemManage.org.placeholders.orgCode'), trigger: 'blur' }]
  }

  const parentOptions = computed(() => [
    {
      id: 0,
      parentId: 0,
      ancestors: '0',
      orgName: t('systemManage.org.root'),
      orgCode: 'ROOT',
      orgType: 'GROUP',
      sort: 0,
      enabled: true,
      children: filterSelfAndChildren(tableData.value, currentOrg.value?.id)
    }
  ])

  const { columnChecks, columns } = useTableColumns(() => [
    { prop: 'orgName', label: t('systemManage.org.orgName'), minWidth: 180 },
    { prop: 'orgCode', label: t('systemManage.org.orgCode'), minWidth: 160 },
    {
      prop: 'orgType',
      label: t('systemManage.org.orgType'),
      width: 110,
      formatter: (row: OrgItem) => orgTypeText(row.orgType)
    },
    { prop: 'sort', label: t('systemManage.org.sort'), width: 80 },
    {
      prop: 'enabled',
      label: t('common.status'),
      width: 90,
      formatter: (row: OrgItem) =>
        h(ElTag, { type: row.enabled ? 'success' : 'info' }, () => (row.enabled ? t('common.enabled') : t('systemManage.org.stopped')))
    },
    {
      prop: 'operation',
      label: t('common.operation'),
      width: 140,
      fixed: 'right',
      formatter: (row: OrgItem) =>
        h('div', { class: 'table-action-nowrap' }, [
          h(ArtButtonTable, { type: 'add', auth: 'system:org:manage', onClick: () => showDialog('add', row) }),
          h(ArtButtonTable, { type: 'edit', auth: 'system:org:manage', onClick: () => showDialog('edit', row) }),
          h(ArtButtonTable, { type: 'delete', auth: 'system:org:manage', onClick: () => deleteOrg(row) })
        ])
    }
  ])

  const filteredData = computed(() => filterOrgTree(tableData.value))

  onMounted(() => {
    getOrgTree()
  })

  const getOrgTree = async () => {
    loading.value = true
    try {
      tableData.value = await fetchGetOrgTree()
    } finally {
      loading.value = false
    }
  }

  const handleSearch = () => {
    Object.assign(appliedFilters, searchForm)
  }

  const handleReset = () => {
    Object.assign(searchForm, { keyword: '', enabled: undefined })
    Object.assign(appliedFilters, searchForm)
  }

  const showDialog = (type: 'add' | 'edit', row?: OrgItem) => {
    dialogType.value = type
    currentOrg.value = type === 'edit' ? row || null : null
    Object.assign(form, {
      parentId: type === 'add' ? row?.id || 0 : row?.parentId || 0,
      orgName: type === 'edit' ? row?.orgName || '' : '',
      orgCode: type === 'edit' ? row?.orgCode || '' : '',
      orgType: type === 'edit' ? row?.orgType || 'GROUP' : 'GROUP',
      sort: type === 'edit' ? row?.sort || 0 : 0,
      enabled: type === 'edit' ? row?.enabled ?? true : true
    })
    dialogVisible.value = true
    nextTick(() => formRef.value?.clearValidate())
  }

  const submitForm = async () => {
    await formRef.value?.validate()
    const payload = { ...form, parentId: form.parentId || 0 }
    if (dialogType.value === 'edit' && currentOrg.value) {
      await fetchUpdateOrg(currentOrg.value.id, payload)
    } else {
      await fetchCreateOrg(payload)
    }
    ElMessage.success(dialogType.value === 'add' ? t('common.success.add') : t('common.success.update'))
    dialogVisible.value = false
    getOrgTree()
  }

  const deleteOrg = async (row: OrgItem) => {
    await ElMessageBox.confirm(t('systemManage.org.deleteConfirm', { name: row.orgName }), t('common.confirmDeleteTitle'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    await fetchDeleteOrg(row.id)
    ElMessage.success(t('common.success.delete'))
    getOrgTree()
  }

  const filterOrgTree = (items: OrgItem[]): OrgItem[] => {
    const keyword = appliedFilters.keyword.trim().toLowerCase()
    const enabled = appliedFilters.enabled

    const result: OrgItem[] = []
    items.forEach((item) => {
      const children = item.children ? filterOrgTree(item.children) : []
      const text = `${item.orgName} ${item.orgCode} ${item.orgType}`.toLowerCase()
      const keywordMatched = !keyword || text.includes(keyword)
      const enabledMatched = enabled === undefined || item.enabled === enabled
      const selfMatched = keywordMatched && enabledMatched
      if (selfMatched || children.length > 0) {
        result.push({ ...item, children })
      }
    })
    return result
  }

  const filterSelfAndChildren = (items: OrgItem[], selfId?: number): OrgItem[] => {
    return items
      .filter((item) => item.id !== selfId)
      .map((item) => ({
        ...item,
        children: item.children ? filterSelfAndChildren(item.children, selfId) : undefined
      }))
  }

  const orgTypeText = (type: string) => {
    const map: Record<string, string> = {
      GROUP: t('systemManage.org.group'),
      DEPT: t('systemManage.org.dept'),
      CLUB: t('systemManage.org.club'),
      MERCHANT: t('systemManage.org.merchant')
    }
    return map[type] || type
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
