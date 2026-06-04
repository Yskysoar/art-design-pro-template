<!-- 登录页面 -->
<template>
  <div class="flex w-full h-screen">
    <LoginLeftView />

    <div class="relative flex-1">
      <AuthTopBar />

      <div class="auth-right-wrap">
        <div class="form">
          <h3 class="title">{{ $t('login.title') }}</h3>
          <p class="sub-title">{{ $t('login.subTitle') }}</p>
          <ElForm
            ref="formRef"
            :model="formData"
            :rules="rules"
            :key="formKey"
            @keyup.enter="handleSubmit"
            style="margin-top: 25px"
          >
            <ElFormItem prop="username">
              <ElInput
                class="custom-height"
                :placeholder="$t('login.placeholder.username')"
                v-model.trim="formData.username"
              />
            </ElFormItem>
            <ElFormItem prop="password">
              <ElInput
                class="custom-height"
                :placeholder="$t('login.placeholder.password')"
                v-model.trim="formData.password"
                type="password"
                autocomplete="off"
                show-password
              />
            </ElFormItem>

            <ElFormItem prop="captchaCode">
              <div class="captcha-row">
                <ElInput
                  class="custom-height"
                  v-model.trim="formData.captchaCode"
                  :placeholder="$t('login.placeholder.captcha')"
                  maxlength="4"
                />
                <button class="captcha-image" type="button" @click="loadCaptcha">
                  <img v-if="captchaImage" :src="captchaImage" alt="captcha" />
                  <span v-else>{{ $t('login.captcha.refresh') }}</span>
                </button>
              </div>
            </ElFormItem>

            <div class="flex-cb mt-2 text-sm">
              <span />
              <RouterLink class="text-theme" :to="{ name: 'ForgetPassword' }">{{
                $t('login.forgetPwd')
              }}</RouterLink>
            </div>

            <div style="margin-top: 30px">
              <ElButton
                class="w-full custom-height"
                type="primary"
                @click="handleSubmit"
                :loading="loading"
                v-ripple
              >
                {{ $t('login.btnText') }}
              </ElButton>
            </div>

            <div class="mt-5 text-sm text-gray-600">
              <span>{{ $t('login.noAccount') }}</span>
              <RouterLink class="text-theme" :to="{ name: 'Register' }">{{
                $t('login.register')
              }}</RouterLink>
            </div>
          </ElForm>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import AppConfig from '@/config'
  import { useUserStore } from '@/store/modules/user'
  import { useWorktabStore } from '@/store/modules/worktab'
  import { useI18n } from 'vue-i18n'
  import { HttpError } from '@/utils/http/error'
  import { fetchCaptcha, fetchGetUserInfo, fetchLogin } from '@/api/auth'
  import { ElNotification, type FormInstance, type FormRules } from 'element-plus'
  defineOptions({ name: 'Login' })

  const { t, locale } = useI18n()
  const worktabStore = useWorktabStore()
  const formKey = ref(0)

  // 监听语言切换，重置表单
  watch(locale, () => {
    formKey.value++
  })

  const userStore = useUserStore()
  const router = useRouter()
  const route = useRoute()
  const systemName = AppConfig.systemInfo.name
  const formRef = ref<FormInstance>()

  const formData = reactive({
    username: '',
    password: '',
    captchaCode: ''
  })

  const rules = computed<FormRules>(() => ({
    username: [{ required: true, message: t('login.placeholder.username'), trigger: 'blur' }],
    password: [{ required: true, message: t('login.placeholder.password'), trigger: 'blur' }],
    captchaCode: [
      { required: true, message: t('login.placeholder.captcha'), trigger: 'blur' },
      { min: 4, max: 4, message: t('login.rule.captchaLength'), trigger: 'blur' }
    ]
  }))

  const loading = ref(false)
  const captchaId = ref('')
  const captchaImage = ref('')

  onMounted(() => {
    loadCaptcha()
  })

  const loadCaptcha = async () => {
    try {
      const captcha = await fetchCaptcha()
      captchaId.value = captcha.captchaId
      captchaImage.value = captcha.imageBase64
      formData.captchaCode = ''
    } catch {
      console.warn('[Login] 验证码加载失败')
      captchaId.value = ''
      captchaImage.value = ''
    }
  }

  // 登录
  const handleSubmit = async () => {
    if (!formRef.value) return

    try {
      // 表单验证
      const valid = await formRef.value.validate()
      if (!valid) return

      loading.value = true

      // 登录请求
      const { username, password } = formData

      const { token, refreshToken } = await fetchLogin({
        userName: username,
        password,
        captchaId: captchaId.value,
        captchaCode: formData.captchaCode
      })

      // 验证token
      if (!token) {
        throw new Error('Login failed - no token received')
      }

      // 存储 token 和登录状态
      userStore.setToken(token, refreshToken)
      userStore.setLoginStatus(true)

      // 清空工作台标签页，登录后只保留工作台
      worktabStore.clearAll()

      // 获取 redirect 参数，如果存在则跳转到指定页面，否则跳转到首页
      const redirect = getSafeRedirectPath(route.query.redirect)
      await router.push(redirect)
      // 导航完成后显示通知，避免通知弹窗先于页面跳转
      showLoginSuccessNotice()
    } catch (error) {
      // 处理 HttpError
      if (error instanceof HttpError) {
        // console.log(error.code)
      } else {
        // 处理非 HttpError
        // ElMessage.error('登录失败，请稍后重试')
        console.error('[Login] Unexpected error:', error)
      }
    } finally {
      loading.value = false
      await loadCaptcha()
    }
  }

  // 登录成功提示
  const showLoginSuccessNotice = () => {
    ElNotification({
        title: t('login.success.title'),
        type: 'success',
        duration: 2500,
        zIndex: 10000,
        message: `${t('login.success.message')}, ${systemName}!`
      })
  }

  const getSafeRedirectPath = (redirect: unknown) => {
    const path = Array.isArray(redirect) ? redirect[0] : redirect
    if (!path || typeof path !== 'string' || !path.startsWith('/')) {
      return '/'
    }

    if (
      path.startsWith('/auth/') ||
      path === '/403' ||
      path === '/404' ||
      path === '/500'
    ) {
      return '/'
    }

    return path
  }
</script>

<style scoped>
  @import './style.css';
</style>

<style lang="scss" scoped>
  :deep(.el-select__wrapper) {
    height: 40px !important;
  }

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
