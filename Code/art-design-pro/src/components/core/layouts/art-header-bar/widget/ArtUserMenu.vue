<!-- 用户菜单 -->
<template>
  <ElPopover
    ref="userMenuPopover"
    placement="bottom-end"
    :width="240"
    :hide-after="0"
    :offset="10"
    trigger="hover"
    :show-arrow="false"
    popper-class="user-menu-popover"
    popper-style="padding: 5px 16px;"
  >
    <template #reference>
      <img
        class="size-8.5 mr-5 c-p rounded-full max-sm:w-6.5 max-sm:h-6.5 max-sm:mr-[16px]"
        :src="userAvatar"
        alt="avatar"
      />
    </template>
    <template #default>
      <div class="pt-3">
        <div class="flex-c pb-1 px-0">
          <img
            class="w-10 h-10 mr-3 ml-0 overflow-hidden rounded-full float-left"
            :src="userAvatar"
            alt="avatar"
          />
          <div class="w-[calc(100%-60px)] h-full">
            <span class="block text-sm font-medium text-g-800 truncate">{{
              userInfo.userName || '未命名用户'
            }}</span>
            <span class="block mt-0.5 text-xs text-g-500 truncate">{{
              userInfo.email || '未填写邮箱'
            }}</span>
          </div>
        </div>
        <ul class="py-4 mt-3 border-t border-g-300/80">
          <li class="btn-item" @click="goUserCenter">
            <ArtSvgIcon icon="ri:user-3-line" />
            <span>{{ $t('topBar.user.userCenter') }}</span>
          </li>
          <li class="btn-item" @click="openPasswordDialog">
            <ArtSvgIcon icon="ri:key-2-line" />
            <span>修改密码</span>
          </li>
          <li class="btn-item" @click="lockScreen">
            <ArtSvgIcon icon="ri:lock-line" />
            <span>{{ $t('topBar.user.lockScreen') }}</span>
          </li>
          <div class="w-full h-px my-2 bg-g-300/80"></div>
          <div class="log-out c-p" @click="loginOut">
            {{ $t('topBar.user.logout') }}
          </div>
        </ul>
      </div>
    </template>
  </ElPopover>

  <ElDialog
    v-model="passwordDialogVisible"
    title="修改密码"
    width="420px"
    align-center
    @closed="resetPasswordForm"
  >
    <ElForm
      ref="passwordFormRef"
      :model="passwordForm"
      :rules="passwordRules"
      label-width="92px"
      label-position="top"
    >
      <ElFormItem label="当前密码" prop="oldPassword">
        <ElInput
          v-model.trim="passwordForm.oldPassword"
          type="password"
          show-password
          autocomplete="current-password"
        />
      </ElFormItem>
      <ElFormItem label="新密码" prop="newPassword">
        <ElInput
          v-model.trim="passwordForm.newPassword"
          type="password"
          show-password
          autocomplete="new-password"
        />
      </ElFormItem>
      <ElFormItem label="确认新密码" prop="confirmPassword">
        <ElInput
          v-model.trim="passwordForm.confirmPassword"
          type="password"
          show-password
          autocomplete="new-password"
          @keyup.enter="submitPasswordChange"
        />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="passwordDialogVisible = false">取消</ElButton>
      <ElButton type="primary" :loading="passwordSubmitting" @click="submitPasswordChange">
        确认修改
      </ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n'
  import { useRouter } from 'vue-router'
  import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
  import { useUserStore } from '@/store/modules/user'
  import { mittBus } from '@/utils/sys'
  import defaultAvatar from '@/assets/images/user/avatar.webp'
  import { fetchChangeCurrentUserPassword } from '@/api/system-manage'

  defineOptions({ name: 'ArtUserMenu' })

  const router = useRouter()
  const { t } = useI18n()
  const userStore = useUserStore()

  const { getUserInfo: userInfo } = storeToRefs(userStore)
  const userMenuPopover = ref()
  const passwordDialogVisible = ref(false)
  const passwordSubmitting = ref(false)
  const passwordFormRef = ref<FormInstance>()
  const passwordForm = reactive({
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  })
  const userAvatar = computed(() => userInfo.value.avatar || defaultAvatar)

  const validateConfirmPassword = (
    _rule: unknown,
    value: string,
    callback: (error?: Error) => void
  ) => {
    if (value !== passwordForm.newPassword) {
      callback(new Error('两次输入的新密码不一致'))
      return
    }
    callback()
  }

  const passwordRules: FormRules = {
    oldPassword: [
      { required: true, message: '请输入当前密码', trigger: 'blur' },
      { min: 6, max: 100, message: '密码长度为 6 到 100 位', trigger: 'blur' }
    ],
    newPassword: [
      { required: true, message: '请输入新密码', trigger: 'blur' },
      { min: 6, max: 100, message: '密码长度为 6 到 100 位', trigger: 'blur' }
    ],
    confirmPassword: [
      { required: true, message: '请再次输入新密码', trigger: 'blur' },
      { validator: validateConfirmPassword, trigger: 'blur' }
    ]
  }

  const goUserCenter = (): void => {
    closeUserMenu()
    router.push({ name: 'UserCenter' }).catch(() => {
      router.push('/system/user-center')
    })
  }

  const openPasswordDialog = (): void => {
    closeUserMenu()
    passwordDialogVisible.value = true
  }

  const lockScreen = (): void => {
    closeUserMenu()
    mittBus.emit('openLockScreen')
  }

  const resetPasswordForm = (): void => {
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    passwordFormRef.value?.clearValidate()
  }

  const submitPasswordChange = async (): Promise<void> => {
    if (!passwordFormRef.value) return
    await passwordFormRef.value.validate()
    passwordSubmitting.value = true
    try {
      await fetchChangeCurrentUserPassword({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })
      passwordDialogVisible.value = false
      ElMessage.success('密码已修改，请使用新密码重新登录')
      userStore.logOut()
    } finally {
      passwordSubmitting.value = false
    }
  }

  const loginOut = (): void => {
    closeUserMenu()
    setTimeout(() => {
      ElMessageBox.confirm(t('common.logOutTips'), t('common.tips'), {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        customClass: 'login-out-dialog'
      }).then(() => {
        userStore.logOut()
      })
    }, 200)
  }

  const closeUserMenu = (): void => {
    setTimeout(() => {
      userMenuPopover.value?.hide()
    }, 100)
  }
</script>

<style scoped>
  @reference '@styles/core/tailwind.css';

  @layer components {
    .btn-item {
      @apply flex items-center p-2 mb-3 select-none rounded-md cursor-pointer last:mb-0;

      span {
        @apply text-sm;
      }

      .art-svg-icon {
        @apply mr-2 text-base;
      }

      &:hover {
        background-color: var(--art-gray-200);
      }
    }
  }

  .log-out {
    @apply py-1.5
    mt-5
    text-xs
    text-center
    border
    border-g-400
    rounded-md
    transition-all
    duration-200
    hover:shadow-xl;
  }
</style>
