<template>
  <ElDialog v-model="visible" :title="$t('systemManage.dataScope.title')" width="560px" align-center @closed="handleClosed">
    <ElForm label-width="110px">
      <ElFormItem :label="$t('systemManage.dataScope.scope')">
        <ElSelect v-model="dataScope" style="width: 100%">
          <ElOption :label="$t('systemManage.dataScope.all')" value="ALL" />
          <ElOption :label="$t('systemManage.dataScope.self')" value="SELF" />
          <ElOption :label="$t('systemManage.dataScope.currentOrg')" value="CURRENT_ORG" />
          <ElOption :label="$t('systemManage.dataScope.currentOrgAndSub')" value="CURRENT_ORG_AND_SUB" />
          <ElOption :label="$t('systemManage.dataScope.customOrg')" value="CUSTOM_ORG" />
        </ElSelect>
      </ElFormItem>
      <ElFormItem v-if="dataScope === 'CUSTOM_ORG'" :label="$t('systemManage.dataScope.orgScope')">
        <ElTree
          ref="treeRef"
          node-key="id"
          show-checkbox
          default-expand-all
          :data="orgTree"
          :props="{ label: 'orgName', children: 'children' }"
          class="org-tree"
        />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="handleClose">{{ $t('common.cancel') }}</ElButton>
      <ElButton type="primary" @click="saveDataScope">{{ $t('common.save') }}</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import {
    fetchGetOrgTree,
    fetchGetRoleDataScope,
    fetchSaveRoleDataScope
  } from '@/api/system-manage'
  import type { ElTree } from 'element-plus'
  import { useI18n } from 'vue-i18n'

  interface Props {
    modelValue: boolean
    roleData?: Api.SystemManage.RoleListItem
  }

  interface Emits {
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    modelValue: false,
    roleData: undefined
  })
  const emit = defineEmits<Emits>()
  const { t } = useI18n()

  const visible = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
  })

  const treeRef = ref<InstanceType<typeof ElTree>>()
  const orgTree = ref<Api.SystemManage.OrgTreeItem[]>([])
  const dataScope = ref('SELF')

  watch(
    () => props.modelValue,
    async (opened) => {
      if (!opened || !props.roleData) return
      const [scope, tree] = await Promise.all([
        fetchGetRoleDataScope(props.roleData.roleId),
        fetchGetOrgTree()
      ])
      dataScope.value = scope.dataScope || 'SELF'
      orgTree.value = tree
      nextTick(() => {
        treeRef.value?.setCheckedKeys(scope.orgIds || [])
      })
    }
  )

  const saveDataScope = async () => {
    if (!props.roleData) return
    const checkedKeys = dataScope.value === 'CUSTOM_ORG' ? treeRef.value?.getCheckedKeys() || [] : []
    const halfCheckedKeys =
      dataScope.value === 'CUSTOM_ORG' ? treeRef.value?.getHalfCheckedKeys() || [] : []
    const orgIds = [...checkedKeys, ...halfCheckedKeys].filter(
      (id: unknown): id is number => typeof id === 'number'
    )

    await fetchSaveRoleDataScope(props.roleData.roleId, {
      dataScope: dataScope.value,
      orgIds
    })
    ElMessage.success(t('systemManage.dataScope.success'))
    emit('success')
    handleClose()
  }

  const handleClose = () => {
    visible.value = false
  }

  const handleClosed = () => {
    dataScope.value = 'SELF'
    treeRef.value?.setCheckedKeys([])
  }
</script>

<style scoped>
  .org-tree {
    width: 100%;
    max-height: 320px;
    overflow: auto;
    border: 1px solid var(--el-border-color-light);
    border-radius: 6px;
    padding: 8px;
  }
</style>
