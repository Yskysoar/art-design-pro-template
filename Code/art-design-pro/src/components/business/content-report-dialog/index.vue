<template>
  <ElDialog
    v-model="visible"
    :title="$t('moderation.report.submitTitle')"
    width="520px"
    align-center
    @closed="resetForm"
  >
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="96px">
      <ElFormItem :label="$t('moderation.report.targetType')">
        <ElInput :model-value="targetLabel" disabled />
      </ElFormItem>
      <ElFormItem :label="$t('moderation.report.reasonType')" prop="reasonType">
        <ElSelect v-model="form.reasonType" class="w-full">
          <ElOption
            v-for="item in reasonOptions"
            :key="item"
            :label="$t(`moderation.report.reasons.${item}`)"
            :value="item"
          />
        </ElSelect>
      </ElFormItem>
      <ElFormItem :label="$t('moderation.report.description')" prop="description">
        <ElInput
          v-model="form.description"
          type="textarea"
          :rows="4"
          maxlength="500"
          show-word-limit
          :placeholder="$t('moderation.report.descriptionPlaceholder')"
        />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="visible = false">{{ $t('common.cancel') }}</ElButton>
      <ElButton type="primary" :loading="submitting" @click="submitReport">
        {{ $t('common.submit') }}
      </ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { createContentReport } from '@/api/moderation'
  import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
  import { useI18n } from 'vue-i18n'

  defineOptions({ name: 'ContentReportDialog' })

  type ReportTargetType = 'ARTICLE' | 'COMMENT' | 'PRIVATE_MESSAGE'

  const { t } = useI18n()
  const visible = defineModel<boolean>('visible', { default: false })
  const props = defineProps<{
    targetType: ReportTargetType
    targetId?: number
  }>()
  const emit = defineEmits<{
    success: []
  }>()

  const reasonOptions = [
    'SPAM',
    'HARASSMENT',
    'PERSONAL_INFO',
    'COPYRIGHT',
    'FRAUD',
    'MISLEADING',
    'FILE_RISK',
    'OUTDATED',
    'OFF_TOPIC',
    'LOW_QUALITY',
    'OTHER'
  ]

  const formRef = ref<FormInstance>()
  const submitting = ref(false)
  const form = reactive({
    reasonType: 'SPAM',
    description: ''
  })
  const rules = computed<FormRules>(() => ({
    reasonType: [{ required: true, message: t('moderation.report.reasonRequired'), trigger: 'change' }],
    description: [{ max: 500, message: t('moderation.report.descriptionMax'), trigger: 'blur' }]
  }))
  const targetLabel = computed(() => t(`moderation.report.targets.${props.targetType}`))

  const resetForm = () => {
    form.reasonType = 'SPAM'
    form.description = ''
    formRef.value?.clearValidate()
    submitting.value = false
  }

  const submitReport = async () => {
    if (!props.targetId) {
      ElMessage.error(t('moderation.report.targetRequired'))
      return
    }
    const valid = await formRef.value?.validate()
    if (!valid) return
    submitting.value = true
    try {
      await createContentReport({
        targetType: props.targetType,
        targetId: props.targetId,
        reasonType: form.reasonType,
        description: form.description.trim() || undefined
      })
      ElMessage.success(t('moderation.report.submitSuccess'))
      emit('success')
      visible.value = false
    } finally {
      submitting.value = false
    }
  }
</script>
