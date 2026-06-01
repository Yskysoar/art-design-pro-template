<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogType === 'add' ? '添加用户' : '编辑用户'"
    width="680px"
    align-center
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="92px">
      <ElFormItem label="用户名" prop="userName">
        <ElInput v-model="formData.userName" placeholder="请输入用户名" />
      </ElFormItem>
      <ElFormItem label="手机号" prop="userPhone">
        <ElInput v-model="formData.userPhone" placeholder="请输入手机号" />
      </ElFormItem>
      <ElFormItem label="性别" prop="userGender">
        <ElSelect v-model="formData.userGender" style="width: 100%">
          <ElOption label="男" value="男" />
          <ElOption label="女" value="女" />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="角色" prop="roleCodes">
        <ElSelect v-model="formData.roleCodes" multiple style="width: 100%">
          <ElOption v-for="role in roleList" :key="role.roleCode" :value="role.roleCode" :label="role.roleName" />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="组织" prop="orgIds">
        <ElTreeSelect
          v-model="formData.orgIds"
          :data="orgTree"
          :props="{ label: 'orgName', value: 'id', children: 'children' }"
          multiple
          check-strictly
          filterable
          placeholder="请选择组织"
          style="width: 100%"
        />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="dialogVisible = false">取消</ElButton>
      <ElButton type="primary" @click="handleSubmit">提交</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { fetchCreateUser, fetchGetOrgTree, fetchGetRoleList, fetchUpdateUser } from '@/api/system-manage'
  import type { FormInstance, FormRules } from 'element-plus'

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
    userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    userPhone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
    userGender: [{ required: true, message: '请选择性别', trigger: 'change' }],
    roleCodes: [{ required: true, message: '请选择角色', trigger: 'change' }],
    orgIds: [{ required: true, message: '请选择组织', trigger: 'change' }]
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

    ElMessage.success(dialogType.value === 'add' ? '添加成功' : '更新成功')
    dialogVisible.value = false
    emit('submit')
  }
</script>
