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
                placeholder="请输入账号"
                v-model.trim="formData.userName"
              />
            </ElFormItem>

            <ElFormItem prop="newPassword">
              <ElInput
                class="custom-height"
                placeholder="请输入新密码"
                type="password"
                autocomplete="new-password"
                show-password
                v-model.trim="formData.newPassword"
              />
            </ElFormItem>

            <ElFormItem prop="confirmPassword">
              <ElInput
                class="custom-height"
                placeholder="请再次输入新密码"
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
                  placeholder="请输入验证码"
                  maxlength="4"
                  v-model.trim="formData.captchaCode"
                  @keyup.enter="resetPassword"
                />
                <button class="captcha-image" type="button" @click="loadCaptcha">
                  <img v-if="captchaImage" :src="captchaImage" alt="captcha" />
                  <span v-else>刷新</span>
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

  defineOptions({ name: 'ForgetPassword' })

  const router = useRouter()
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
      callback(new Error('请再次输入新密码'))
      return
    }
    if (value !== formData.newPassword) {
      callback(new Error('两次输入的密码不一致'))
      return
    }
    callback()
  }

  const rules: FormRules = {
    userName: [{ required: true, message: '请输入账号', trigger: 'blur' }],
    newPassword: [
      { required: true, message: '请输入新密码', trigger: 'blur' },
      { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
    ],
    confirmPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }],
    captchaCode: [
      { required: true, message: '请输入验证码', trigger: 'blur' },
      { min: 4, max: 4, message: '验证码为4位', trigger: 'blur' }
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
      ElMessage.success('密码已重置，请使用新密码登录')
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
