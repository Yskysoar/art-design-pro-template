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
            <ElButton v-auth="'system:org:manage'" @click="showDialog('add')" v-ripple>新增组织</ElButton>
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
      :title="dialogType === 'add' ? '新增组织' : '编辑组织'"
      width="560px"
      align-center
    >
      <ElForm ref="formRef" :model="form" :rules="rules" label-width="100px">
        <ElFormItem label="上级组织" prop="parentId">
          <ElTreeSelect
            v-model="form.parentId"
            :data="parentOptions"
            :props="{ label: 'orgName', children: 'children' }"
            node-key="id"
            value-key="id"
            check-strictly
            clearable
            filterable
            placeholder="不选择则作为根组织"
            style="width: 100%"
          />
        </ElFormItem>
        <ElFormItem label="组织名称" prop="orgName">
          <ElInput v-model="form.orgName" maxlength="100" show-word-limit />
        </ElFormItem>
        <ElFormItem label="组织编码" prop="orgCode">
          <ElInput v-model="form.orgCode" maxlength="100" show-word-limit />
        </ElFormItem>
        <ElFormItem label="组织类型" prop="orgType">
          <ElSelect v-model="form.orgType" style="width: 100%">
            <ElOption label="分组" value="GROUP" />
            <ElOption label="部门" value="DEPT" />
            <ElOption label="社团" value="CLUB" />
            <ElOption label="商家" value="MERCHANT" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="排序" prop="sort">
          <ElInputNumber v-model="form.sort" :min="0" controls-position="right" style="width: 100%" />
        </ElFormItem>
        <ElFormItem label="是否启用">
          <ElSwitch v-model="form.enabled" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="submitForm">保存</ElButton>
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

  defineOptions({ name: 'Org' })

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
    { label: '关键词', key: 'keyword', type: 'input', props: { clearable: true } },
    {
      label: '状态',
      key: 'enabled',
      type: 'select',
      props: {
        clearable: true,
        options: [
          { label: '启用', value: true },
          { label: '停用', value: false }
        ]
      }
    }
  ])

  const rules: FormRules = {
    orgName: [{ required: true, message: '请输入组织名称', trigger: 'blur' }],
    orgCode: [{ required: true, message: '请输入组织编码', trigger: 'blur' }]
  }

  const parentOptions = computed(() => [
    {
      id: 0,
      parentId: 0,
      ancestors: '0',
      orgName: '根组织',
      orgCode: 'ROOT',
      orgType: 'GROUP',
      sort: 0,
      enabled: true,
      children: filterSelfAndChildren(tableData.value, currentOrg.value?.id)
    }
  ])

  const { columnChecks, columns } = useTableColumns(() => [
    { prop: 'orgName', label: '组织名称', minWidth: 180 },
    { prop: 'orgCode', label: '组织编码', minWidth: 160 },
    {
      prop: 'orgType',
      label: '组织类型',
      width: 110,
      formatter: (row: OrgItem) => orgTypeText(row.orgType)
    },
    { prop: 'sort', label: '排序', width: 80 },
    {
      prop: 'enabled',
      label: '状态',
      width: 90,
      formatter: (row: OrgItem) =>
        h(ElTag, { type: row.enabled ? 'success' : 'info' }, () => (row.enabled ? '启用' : '停用'))
    },
    {
      prop: 'operation',
      label: '操作',
      width: 150,
      fixed: 'right',
      formatter: (row: OrgItem) =>
        h('div', [
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
    ElMessage.success(dialogType.value === 'add' ? '新增成功' : '更新成功')
    dialogVisible.value = false
    getOrgTree()
  }

  const deleteOrg = async (row: OrgItem) => {
    await ElMessageBox.confirm(`确定删除组织"${row.orgName}"吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await fetchDeleteOrg(row.id)
    ElMessage.success('删除成功')
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
      GROUP: '分组',
      DEPT: '部门',
      CLUB: '社团',
      MERCHANT: '商家'
    }
    return map[type] || type
  }
</script>
