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
          <ElSpace wrap>
            <ElButton @click="showDialog('add')" v-ripple>新增配置</ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <ArtTable :loading="loading" :data="data" :columns="columns" :pagination="pagination" />
    </ElCard>

    <ElDialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增配置' : '编辑配置'"
      width="520px"
      align-center
    >
      <ElForm ref="formRef" :model="form" :rules="rules" label-width="100px">
        <ElFormItem label="配置键" prop="configKey">
          <ElInput v-model="form.configKey" :disabled="dialogType === 'edit'" />
        </ElFormItem>
        <ElFormItem label="配置值" prop="configValue">
          <ElInput v-model="form.configValue" />
        </ElFormItem>
        <ElFormItem label="说明" prop="description">
          <ElInput v-model="form.description" type="textarea" :rows="3" />
        </ElFormItem>
        <ElFormItem label="可编辑">
          <ElSwitch v-model="form.editable" />
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
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchCreateConfig,
    fetchDeleteConfig,
    fetchGetConfigList,
    fetchUpdateConfig
  } from '@/api/system-manage'
  import { ElMessageBox, ElSwitch, ElTag, type FormInstance, type FormRules } from 'element-plus'

  defineOptions({ name: 'Config' })

  type ConfigItem = Api.SystemManage.ConfigListItem

  const searchForm = reactive<Api.SystemManage.ConfigSearchParams>({
    configKey: undefined,
    description: undefined,
    editable: undefined
  })

  const formItems = computed(() => [
    { label: '配置键', key: 'configKey', type: 'input', props: { clearable: true } },
    { label: '说明', key: 'description', type: 'input', props: { clearable: true } },
    {
      label: '可编辑',
      key: 'editable',
      type: 'select',
      props: {
        clearable: true,
        options: [
          { label: '是', value: true },
          { label: '否', value: false }
        ]
      }
    }
  ])

  const dialogVisible = ref(false)
  const dialogType = ref<'add' | 'edit'>('add')
  const currentConfig = ref<ConfigItem | null>(null)
  const formRef = ref<FormInstance>()

  const form = reactive<Api.SystemManage.ConfigSaveParams>({
    configKey: '',
    configValue: '',
    description: '',
    editable: true
  })

  const rules: FormRules = {
    configKey: [{ required: true, message: '请输入配置键', trigger: 'blur' }],
    configValue: [{ required: true, message: '请输入配置值', trigger: 'blur' }]
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
      apiFn: fetchGetConfigList,
      apiParams: {
        current: 1,
        size: 20
      },
      columnsFactory: () => [
        { prop: 'configKey', label: '配置键', minWidth: 180 },
        { prop: 'configValue', label: '配置值', minWidth: 160 },
        { prop: 'description', label: '说明', minWidth: 220, showOverflowTooltip: true },
        {
          prop: 'editable',
          label: '可编辑',
          width: 100,
          formatter: (row) =>
            h(ElTag, { type: row.editable ? 'success' : 'info' }, () =>
              row.editable ? '是' : '否'
            )
        },
        { prop: 'updateTime', label: '更新时间', width: 180 },
        {
          prop: 'operation',
          label: '操作',
          width: 130,
          fixed: 'right',
          formatter: (row) =>
            h('div', [
              row.editable
                ? h(ArtButtonTable, { type: 'edit', onClick: () => showDialog('edit', row) })
                : null,
              row.editable ? h(ArtButtonTable, { type: 'delete', onClick: () => deleteConfig(row) }) : null
            ])
        }
      ]
    }
  })

  const handleSearch = (params: Api.SystemManage.ConfigSearchParams) => {
    replaceSearchParams(params)
    getData()
  }

  const handleReset = () => {
    resetSearchParams()
    getData()
  }

  const showDialog = (type: 'add' | 'edit', row?: ConfigItem) => {
    dialogType.value = type
    currentConfig.value = row || null
    Object.assign(form, {
      configKey: row?.configKey || '',
      configValue: row?.configValue || '',
      description: row?.description || '',
      editable: row?.editable ?? true
    })
    dialogVisible.value = true
    nextTick(() => formRef.value?.clearValidate())
  }

  const submitForm = async () => {
    await formRef.value?.validate()
    if (dialogType.value === 'edit' && currentConfig.value) {
      await fetchUpdateConfig(currentConfig.value.id, form)
    } else {
      await fetchCreateConfig(form)
    }
    ElMessage.success(dialogType.value === 'add' ? '新增成功' : '更新成功')
    dialogVisible.value = false
    refreshData()
  }

  const deleteConfig = async (row: ConfigItem) => {
    await ElMessageBox.confirm(`确定删除配置"${row.configKey}"吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await fetchDeleteConfig(row.id)
    ElMessage.success('删除成功')
    refreshData()
  }
</script>
