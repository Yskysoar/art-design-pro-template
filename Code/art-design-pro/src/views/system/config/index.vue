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
            <ElButton v-auth="'system:config:manage'" @click="showDialog('add')" v-ripple>{{ $t('systemManage.config.add') }}</ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <ArtTable :loading="loading" :data="data" :columns="columns" :pagination="pagination" />
    </ElCard>

    <ElDialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? $t('systemManage.config.add') : $t('systemManage.config.edit')"
      width="520px"
      align-center
    >
      <ElForm ref="formRef" :model="form" :rules="rules" label-width="100px">
        <ElFormItem :label="$t('systemManage.config.configKey')" prop="configKey">
          <ElInput v-model="form.configKey" :disabled="dialogType === 'edit'" />
        </ElFormItem>
        <ElFormItem :label="$t('systemManage.config.configValue')" prop="configValue">
          <ElSelect v-if="configValueOptions.length" v-model="form.configValue" class="w-full">
            <ElOption
              v-for="item in configValueOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </ElSelect>
          <ElInput v-else v-model="form.configValue" />
        </ElFormItem>
        <ElFormItem :label="$t('systemManage.config.description')" prop="description">
          <ElInput v-model="form.description" type="textarea" :rows="3" />
        </ElFormItem>
        <ElFormItem :label="$t('systemManage.config.editable')">
          <ElSwitch v-model="form.editable" />
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
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchCreateConfig,
    fetchDeleteConfig,
    fetchGetConfigList,
    fetchUpdateConfig
  } from '@/api/system-manage'
  import { ElMessageBox, ElSwitch, ElTag, type FormInstance, type FormRules } from 'element-plus'
  import { useI18n } from 'vue-i18n'

  defineOptions({ name: 'Config' })
  const { t } = useI18n()

  type ConfigItem = Api.SystemManage.ConfigListItem
  type ConfigValueOption = {
    label: string
    value: string
  }

  const searchForm = reactive<Api.SystemManage.ConfigSearchParams>({
    configKey: undefined,
    description: undefined,
    editable: undefined
  })

  const formItems = computed(() => [
    { label: t('systemManage.config.configKey'), key: 'configKey', type: 'input', props: { clearable: true } },
    { label: t('systemManage.config.description'), key: 'description', type: 'input', props: { clearable: true } },
    {
      label: t('systemManage.config.editable'),
      key: 'editable',
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
    configKey: [{ required: true, message: t('systemManage.config.placeholders.configKey'), trigger: 'blur' }],
    configValue: [{ required: true, message: t('systemManage.config.placeholders.configValue'), trigger: 'blur' }]
  }

  const booleanOptions = computed<ConfigValueOption[]>(() => [
    { label: t('common.yes'), value: 'true' },
    { label: t('common.no'), value: 'false' }
  ])

  const configValueOptionMap = computed<Record<string, ConfigValueOption[]>>(() => ({
    user_org_relation_mode: [
      { label: t('systemManage.config.values.ONE_TO_ONE'), value: 'ONE_TO_ONE' },
      { label: t('systemManage.config.values.ONE_TO_MANY'), value: 'ONE_TO_MANY' }
    ],
    role_level_enabled: booleanOptions.value,
    anonymous_portal_access: booleanOptions.value,
    guest_admin_access: booleanOptions.value,
    article_comment_hide_enabled: booleanOptions.value,
    article_default_visible: booleanOptions.value,
    upload_max_size_mb: [
      { label: t('systemManage.config.values.sizeMb', { size: 5 }), value: '5' },
      { label: t('systemManage.config.values.sizeMb', { size: 10 }), value: '10' },
      { label: t('systemManage.config.values.sizeMb', { size: 20 }), value: '20' },
      { label: t('systemManage.config.values.sizeMb', { size: 50 }), value: '50' }
    ]
  }))

  const configValueOptions = computed(() => configValueOptionMap.value[form.configKey] || [])

  watch(
    () => form.configKey,
    () => {
      const options = configValueOptions.value
      if (!options.length || options.some((item) => item.value === form.configValue)) return
      form.configValue = options[0].value
    }
  )

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
        { prop: 'configKey', label: t('systemManage.config.configKey'), minWidth: 180 },
        { prop: 'configValue', label: t('systemManage.config.configValue'), minWidth: 160 },
        { prop: 'description', label: t('systemManage.config.description'), minWidth: 220, showOverflowTooltip: true },
        {
          prop: 'editable',
          label: t('systemManage.config.editable'),
          width: 100,
          formatter: (row) =>
            h(ElTag, { type: row.editable ? 'success' : 'info' }, () =>
              row.editable ? t('common.yes') : t('common.no')
            )
        },
        { prop: 'updateTime', label: t('systemManage.common.updateTime'), width: 180 },
        {
          prop: 'operation',
          label: t('common.operation'),
          width: 120,
          fixed: 'right',
          formatter: (row) =>
            h('div', { class: 'table-action-nowrap' }, [
              row.editable
                ? h(ArtButtonTable, { type: 'edit', auth: 'system:config:manage', onClick: () => showDialog('edit', row) })
                : null,
              row.editable ? h(ArtButtonTable, { type: 'delete', auth: 'system:config:manage', onClick: () => deleteConfig(row) }) : null
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
    ElMessage.success(dialogType.value === 'add' ? t('common.success.add') : t('common.success.update'))
    dialogVisible.value = false
    refreshData()
  }

  const deleteConfig = async (row: ConfigItem) => {
    await ElMessageBox.confirm(t('systemManage.config.deleteConfirm', { name: row.configKey }), t('common.confirmDeleteTitle'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    await fetchDeleteConfig(row.id)
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
