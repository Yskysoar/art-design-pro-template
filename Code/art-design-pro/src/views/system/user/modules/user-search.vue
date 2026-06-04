<template>
  <ArtSearchBar
    ref="searchBarRef"
    v-model="formData"
    :items="formItems"
    :rules="rules"
    @reset="handleReset"
    @search="handleSearch"
  >
  </ArtSearchBar>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n'

  const { t } = useI18n()
  interface Props {
    modelValue: Api.SystemManage.UserSearchParams
  }
  interface Emits {
    (e: 'update:modelValue', value: Api.SystemManage.UserSearchParams): void
    (e: 'search', params: Api.SystemManage.UserSearchParams): void
    (e: 'reset'): void
  }
  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  // 表单数据双向绑定
  const searchBarRef = ref()
  const formData = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
  })

  // 校验规则
  const rules = {
    // userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }]
  }

  // 动态 options
  const statusOptions = [
    { label: t('common.enabled'), value: '1' },
    { label: t('common.disabled'), value: '4' }
  ]

  // 表单配置
  const formItems = computed(() => [
    {
      label: t('systemManage.user.userName'),
      key: 'userName',
      type: 'input',
      placeholder: t('systemManage.user.placeholders.userName'),
      clearable: true
    },
    {
      label: t('systemManage.user.phone'),
      key: 'userPhone',
      type: 'input',
      props: { placeholder: t('systemManage.user.placeholders.phone'), maxlength: '11' }
    },
    {
      label: t('systemManage.user.email'),
      key: 'userEmail',
      type: 'input',
      props: { placeholder: t('systemManage.user.placeholders.email') }
    },
    {
      label: t('common.status'),
      key: 'status',
      type: 'select',
      props: {
        placeholder: t('systemManage.common.selectStatus'),
        clearable: true,
        options: statusOptions
      }
    },
    {
      label: t('systemManage.user.gender'),
      key: 'userGender',
      type: 'radiogroup',
      props: {
        options: [
          { label: t('systemManage.user.male'), value: '1' },
          { label: t('systemManage.user.female'), value: '2' }
        ]
      }
    }
  ])

  // 事件
  function handleReset() {
    emit('reset')
  }

  async function handleSearch(params: Api.SystemManage.UserSearchParams) {
    await searchBarRef.value.validate()
    emit('search', params)
  }
</script>
