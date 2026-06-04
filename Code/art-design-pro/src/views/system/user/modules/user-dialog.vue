<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogType === 'add' ? $t('systemManage.user.addDialog') : $t('systemManage.user.editDialog')"
    width="680px"
    align-center
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="92px">
      <ElFormItem :label="$t('systemManage.user.userName')" prop="userName">
        <ElInput v-model="formData.userName" :placeholder="$t('systemManage.user.placeholders.userName')" />
      </ElFormItem>
      <ElFormItem :label="$t('systemManage.user.phone')" prop="userPhone">
        <ElInput v-model="formData.userPhone" :placeholder="$t('systemManage.user.placeholders.phone')" />
      </ElFormItem>
      <ElFormItem :label="$t('systemManage.user.gender')" prop="userGender">
        <ElSelect v-model="formData.userGender" style="width: 100%">
          <ElOption :label="$t('systemManage.user.male')" value="男" />
          <ElOption :label="$t('systemManage.user.female')" value="女" />
        </ElSelect>
      </ElFormItem>
      <ElFormItem :label="$t('systemManage.user.role')" prop="roleCodes">
        <ElSelect v-model="formData.roleCodes" multiple style="width: 100%">
          <ElOption v-for="role in roleList" :key="role.roleCode" :value="role.roleCode" :label="role.roleName" />
        </ElSelect>
      </ElFormItem>
      <ElFormItem :label="$t('systemManage.user.org')" prop="orgIds">
        <ElTreeSelect
          v-model="formData.orgIds"
          :data="orgTree"
          :props="{ label: 'orgName', children: 'children' }"
          node-key="id"
          value-key="id"
          multiple
          check-strictly
          filterable
          :placeholder="$t('systemManage.user.placeholders.org')"
          style="width: 100%"
        />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="dialogVisible = false">{{ $t('common.cancel') }}</ElButton>
      <ElButton type="primary" @click="handleSubmit">{{ $t('common.submit') }}</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { fetchCreateUser, fetchGetOrgTree, fetchGetRoleList, fetchUpdateUser } from '@/api/system-manage'
  import type { FormInstance, FormRules } from 'element-plus'
  import { useI18n } from 'vue-i18n'

  interface Props {
    visible: boolean
    type: string
    userData?: Partial<Api.SystemManage.UserListItem>
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()
  const { t } = useI18n()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const dialogType = computed(() => props.type)
  const formRef = ref<FormInstance>()
  const roleList = ref<Api.SystemManage.RoleListItem[]>([])
  const orgTree = ref<Api.SystemManage.OrgTreeItem[]>([])
  const formData = reactive({
    userName: '',
    userPhone: '',
    userGender: '男',
    roleCodes: [] as string[],
    orgIds: [] as number[]
  })

  const rules: FormRules = {
    userName: [{ required: true, message: t('systemManage.user.placeholders.userName'), trigger: 'blur' }],
    userPhone: [{ required: true, message: t('systemManage.user.placeholders.phone'), trigger: 'blur' }],
    userGender: [{ required: true, message: t('systemManage.user.placeholders.gender'), trigger: 'change' }],
    roleCodes: [{ required: true, message: t('systemManage.user.placeholders.role'), trigger: 'change' }],
    orgIds: [{ required: true, message: t('systemManage.user.placeholders.org'), trigger: 'change' }]
  }

  const loadOptions = async () => {
    const [roles, orgs] = await Promise.all([
      fetchGetRoleList({ current: 1, size: 100 }),
      fetchGetOrgTree()
    ])
    roleList.value = roles.records
    orgTree.value = orgs
  }

  const initFormData = async () => {
    const row = props.userData
    Object.assign(formData, {
      userName: row?.userName || '',
      userPhone: row?.userPhone || '',
      userGender: row?.userGender || '男',
      roleCodes: Array.isArray(row?.userRoles) ? row.userRoles : [],
      orgIds: Array.isArray(row?.orgIds) ? row.orgIds : []
    })
  }

  watch(
    () => [props.visible, props.type, props.userData],
    async ([visible]) => {
      if (!visible) {
        return
      }
      await loadOptions()
      await initFormData()
      nextTick(() => formRef.value?.clearValidate())
    },
    { immediate: true }
  )

  const handleSubmit = async () => {
    await formRef.value?.validate()

    const payload: Api.SystemManage.UserSaveParams = {
      userName: formData.userName,
      nickName: formData.userName,
      userGender: formData.userGender,
      userPhone: formData.userPhone,
      status: '1',
      roleCodes: formData.roleCodes,
      orgIds: formData.orgIds
    }

    if (dialogType.value === 'edit' && props.userData?.id) {
      await fetchUpdateUser(props.userData.id, payload)
    } else {
      await fetchCreateUser({ ...payload, password: 'admin123' })
    }

    ElMessage.success(dialogType.value === 'add' ? t('common.success.add') : t('common.success.update'))
    dialogVisible.value = false
    emit('submit')
  }
</script>
