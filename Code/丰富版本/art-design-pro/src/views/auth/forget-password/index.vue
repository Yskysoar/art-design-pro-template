<template>
  <div class="flex w-full h-screen">
    <LoginLeftView />

    <div class="relative flex-1">
      <AuthTopBar />

      <div class="auth-right-wrap">
        <div class="form">
          <h3 class="title">{{ $t('forgetPassword.title') }}</h3>
          <p class="sub-title">{{ $t('forgetPassword.subTitle') }}</p>
          <ElForm ref="formRef" class="mt-7.5" :model="formData" :rules="rules" label-position="top">
            <ElFormItem prop="userName">
              <ElInput
                class="custom-height"
                :placeholder="$t('forgetPassword.placeholders.userName')"
                v-model.trim="formData.userName"
              />
            </ElFormItem>

            <ElFormItem prop="newPassword">
              <ElInput
                class="custom-height"
                :placeholder="$t('forgetPassword.placeholders.newPassword')"
                type="password"
                autocomplete="new-password"
                show-password
                v-model.trim="formData.newPassword"
              />
            </ElFormItem>

            <ElFormItem prop="confirmPassword">
              <ElInput
                class="custom-height"
                :placeholder="$t('forgetPassword.placeholders.confirmPassword')"
                type="password"
                autocomplete="new-password"
                show-password
                v-model.trim="formData.confirmPassword"
                @keyup.enter="resetPassword"
              />
            </ElFormItem>

            <ElFormItem prop="captchaCode">
              <div class="captcha-row">
                <ElInput
                  class="custom-height"
                  :placeholder="$t('forgetPassword.placeholders.captcha')"
                  maxlength="4"
                  v-model.trim="formData.captchaCode"
                  @keyup.enter="resetPassword"
                />
                <button class="captcha-image" type="button" @click="loadCaptcha">
                  <img v-if="captchaImage" :src="captchaImage" alt="captcha" />
                  <span v-else>{{ $t('forgetPassword.captcha.refresh') }}</span>
                </button>
              </div>
            </ElFormItem>
          </ElForm>

          <div style="margin-top: 15px">
            <ElButton
              class="w-full custom-height"
              type="primary"
              @click="resetPassword"
              :loading="loading"
              v-ripple
            >
              {{ $t('forgetPassword.submitBtnText') }}
            </ElButton>
          </div>

          <div style="margin-top: 15px">
            <ElButton class="w-full custom-height" plain @click="toLogin">
              {{ $t('forgetPassword.backBtnText') }}
            </ElButton>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { fetchCaptcha, fetchResetPassword } from '@/api/auth'
  import type { FormInstance, FormRules } from 'element-plus'
  import { useI18n } from 'vue-i18n'

  defineOptions({ name: 'ForgetPassword' })

  const router = useRouter()
  const { t } = useI18n()
  const formRef = ref<FormInstance>()

  const loading = ref(false)
  const captchaId = ref('')
  const captchaImage = ref('')
  const formData = reactive({
    userName: '',
    newPassword: '',
    confirmPassword: '',
    captchaCode: ''
  })

  const validateConfirmPassword = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
    if (!value) {
      callback(new Error(t('forgetPassword.rule.confirmPasswordRequired')))
      return
    }
    if (value !== formData.newPassword) {
      callback(new Error(t('forgetPassword.rule.passwordMismatch')))
      return
    }
    callback()
  }

  const rules: FormRules = {
    userName: [{ required: true, message: t('forgetPassword.placeholders.userName'), trigger: 'blur' }],
    newPassword: [
      { required: true, message: t('forgetPassword.placeholders.newPassword'), trigger: 'blur' },
      { min: 6, message: t('forgetPassword.rule.passwordLength'), trigger: 'blur' }
    ],
    confirmPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }],
    captchaCode: [
      { required: true, message: t('forgetPassword.placeholders.captcha'), trigger: 'blur' },
      { min: 4, max: 4, message: t('forgetPassword.rule.captchaLength'), trigger: 'blur' }
    ]
  }

  const loadCaptcha = async () => {
    try {
      const captcha = await fetchCaptcha()
      captchaId.value = captcha.captchaId
      captchaImage.value = captcha.imageBase64
      formData.captchaCode = ''
    } catch {
      console.warn('[ForgetPassword] 验证码加载失败')
      captchaId.value = ''
      captchaImage.value = ''
    }
  }

  const resetPassword = async () => {
    if (!formRef.value) return
    await formRef.value.validate()
    loading.value = true
    try {
      await fetchResetPassword({
        userName: formData.userName,
        newPassword: formData.newPassword,
        captchaId: captchaId.value,
        captchaCode: formData.captchaCode
      })
      ElMessage.success(t('forgetPassword.success'))
      toLogin()
    } finally {
      if (loading.value) {
        await loadCaptcha()
      }
      loading.value = false
    }
  }

  const toLogin = () => {
    router.push({ name: 'Login' })
  }

  onMounted(loadCaptcha)
</script>

<style scoped>
  @import '../login/style.css';

  .captcha-row {
    display: flex;
    gap: 12px;
    width: 100%;
  }

  .captcha-image {
    width: 112px;
    height: 40px;
    padding: 0;
    overflow: hidden;
    cursor: pointer;
    background: var(--el-fill-color-light);
    border: 1px solid var(--el-border-color);
    border-radius: 6px;
  }

  .captcha-image img {
    display: block;
    width: 100%;
    height: 100%;
  }
</style>
