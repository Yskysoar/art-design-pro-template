<template>
  <ElDialog
    v-model="visible"
    :title="dialogType === 'add' ? $t('systemManage.roleDialog.add') : $t('systemManage.roleDialog.edit')"
    width="30%"
    align-center
    @close="handleClose"
  >
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="120px">
      <ElFormItem :label="$t('systemManage.role.roleName')" prop="roleName">
        <ElInput v-model="form.roleName" :placeholder="$t('systemManage.role.placeholders.roleName')" />
      </ElFormItem>
      <ElFormItem :label="$t('systemManage.role.roleCode')" prop="roleCode">
        <ElInput v-model="form.roleCode" :placeholder="$t('systemManage.role.placeholders.roleCode')" />
      </ElFormItem>
      <ElFormItem :label="$t('systemManage.roleDialog.description')" prop="description">
        <ElInput
          v-model="form.description"
          type="textarea"
          :rows="3"
          :placeholder="$t('systemManage.role.placeholders.description')"
        />
      </ElFormItem>
      <ElFormItem :label="$t('systemManage.roleDialog.enabled')">
        <ElSwitch v-model="form.enabled" />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="handleClose">{{ $t('common.cancel') }}</ElButton>
      <ElButton type="primary" @click="handleSubmit">{{ $t('common.submit') }}</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import { fetchCreateRole, fetchUpdateRole } from '@/api/system-manage'
  import { useI18n } from 'vue-i18n'

  type RoleListItem = Api.SystemManage.RoleListItem

  interface Props {
    modelValue: boolean
    dialogType: 'add' | 'edit'
    roleData?: RoleListItem
  }

  interface Emits {
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    modelValue: false,
    dialogType: 'add',
    roleData: undefined
  })

  const emit = defineEmits<Emits>()
  const { t } = useI18n()

  const formRef = ref<FormInstance>()

  /**
   * 弹窗显示状态双向绑定
   */
  const visible = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
  })

  /**
   * 表单验证规则
   */
  const rules = reactive<FormRules>({
    roleName: [
      { required: true, message: t('systemManage.role.placeholders.roleName'), trigger: 'blur' },
      { min: 2, max: 20, message: t('systemManage.roleDialog.nameLength'), trigger: 'blur' }
    ],
    roleCode: [
      { required: true, message: t('systemManage.role.placeholders.roleCode'), trigger: 'blur' },
      { min: 2, max: 50, message: t('systemManage.roleDialog.codeLength'), trigger: 'blur' }
    ],
    description: [{ required: true, message: t('systemManage.role.placeholders.description'), trigger: 'blur' }]
  })

  /**
   * 表单数据
   */
  const form = reactive<RoleListItem>({
    roleId: 0,
    roleName: '',
    roleCode: '',
    description: '',
    createTime: '',
    enabled: true
  })

  /**
   * 监听弹窗打开，初始化表单数据
   */
  watch(
    () => props.modelValue,
    (newVal) => {
      if (newVal) initForm()
    }
  )

  /**
   * 监听角色数据变化，更新表单
   */
  watch(
    () => props.roleData,
    (newData) => {
      if (newData && props.modelValue) initForm()
    },
    { deep: true }
  )

  /**
   * 初始化表单数据
   * 根据弹窗类型填充表单或重置表单
   */
  const initForm = () => {
    if (props.dialogType === 'edit' && props.roleData) {
      Object.assign(form, props.roleData)
    } else {
      Object.assign(form, {
        roleId: 0,
        roleName: '',
        roleCode: '',
        description: '',
        createTime: '',
        enabled: true
      })
    }
  }

  /**
   * 关闭弹窗并重置表单
   */
  const handleClose = () => {
    visible.value = false
    formRef.value?.resetFields()
  }

  /**
   * 提交表单
   * 验证通过后调用接口保存数据
   */
  const handleSubmit = async () => {
    if (!formRef.value) return

    try {
      await formRef.value.validate()
      const payload: Api.SystemManage.RoleSaveParams = {
        roleName: form.roleName,
        roleCode: form.roleCode,
        description: form.description,
        enabled: form.enabled
      }

      if (props.dialogType === 'edit' && form.roleId) {
        await fetchUpdateRole(form.roleId, payload)
      } else {
        await fetchCreateRole(payload)
      }

      const message = props.dialogType === 'add' ? t('common.success.add') : t('systemManage.roleDialog.updateSuccess')
      ElMessage.success(message)
      emit('success')
      handleClose()
    } catch (error) {
      console.log(t('systemManage.roleDialog.validateFailed'), error)
    }
  }
</script>
