<!-- 个人中心页面 -->
<template>
  <div class="w-full h-full p-0 bg-transparent border-none shadow-none">
    <div class="relative flex-b mt-2.5 max-md:block max-md:mt-1">
      <div class="w-112 mr-5 max-md:w-full max-md:mr-0">
        <div class="art-card-sm relative p-9 pb-6 overflow-hidden text-center">
          <img class="absolute top-0 left-0 w-full h-50 object-cover" src="@imgs/user/bg.webp" />
          <img
            class="relative z-10 w-20 h-20 mt-30 mx-auto object-cover border-2 border-white rounded-full"
            :src="userAvatar"
            alt="avatar"
          />
          <h2 class="mt-5 text-xl font-normal">{{ userInfo.userName || '未命名用户' }}</h2>
          <p class="mt-5 text-sm">个人资料与账号信息</p>

          <div class="w-75 mx-auto mt-7.5 text-left">
            <div class="mt-2.5">
              <ArtSvgIcon icon="ri:mail-line" class="text-g-700" />
              <span class="ml-2 text-sm">{{ userInfo.email || '未填写邮箱' }}</span>
            </div>
            <div class="mt-2.5">
              <ArtSvgIcon icon="ri:user-3-line" class="text-g-700" />
              <span class="ml-2 text-sm">{{ roleText }}</span>
            </div>
          </div>

          <div class="mt-10">
            <h3 class="text-sm font-medium">角色</h3>
            <div class="flex flex-wrap justify-center mt-3.5">
              <div
                v-for="item in userInfo.roles || []"
                :key="item"
                class="py-1 px-1.5 mr-2.5 mb-2.5 text-xs border border-g-300 rounded"
              >
                {{ item }}
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="flex-1 overflow-hidden max-md:w-full max-md:mt-3.5">
        <div class="art-card-sm">
          <div class="flex items-center justify-between p-4 border-b border-g-300">
            <h1 class="text-xl font-normal">基本设置</h1>
            <ElButton size="small" :type="editing ? 'primary' : 'default'" @click="toggleEdit">
              {{ editing ? '取消' : '编辑' }}
            </ElButton>
          </div>

          <ElForm
            ref="ruleFormRef"
            :model="form"
            :rules="rules"
            class="box-border p-5 [&>.el-row_.el-form-item]:w-[calc(50%-10px)] [&>.el-row_.el-input]:w-full [&>.el-row_.el-select]:w-full"
            label-width="86px"
            label-position="top"
          >
            <ElRow>
              <ElFormItem label="用户名" prop="userName">
                <ElInput :model-value="userInfo.userName || ''" disabled />
              </ElFormItem>
              <ElFormItem label="用户 ID" prop="userId" class="ml-5">
                <ElInput :model-value="String(userInfo.userId || '')" disabled />
              </ElFormItem>
            </ElRow>

            <ElRow>
              <ElFormItem label="邮箱" prop="email">
                <ElInput v-model="form.email" :disabled="!editing" :placeholder="editing ? '请输入邮箱' : userInfo.email || '未填写'" />
              </ElFormItem>
              <ElFormItem label="头像地址" prop="avatar" class="ml-5">
                <ElInput v-model="form.avatar" :disabled="!editing" :placeholder="editing ? '请输入头像 URL' : userInfo.avatar || '未填写'" />
              </ElFormItem>
            </ElRow>

            <ElRow>
              <ElFormItem label="昵称" prop="nickName">
                <ElInput v-model="form.nickName" :disabled="!editing" :placeholder="editing ? '请输入昵称' : userInfo.userName || '未填写'" />
              </ElFormItem>
            </ElRow>

            <ElFormItem v-if="editing" label="操作">
              <ElButton type="primary" :loading="saving" @click="saveProfile">保存修改</ElButton>
              <ElButton :disabled="saving" @click="toggleEdit">取消</ElButton>
            </ElFormItem>
          </ElForm>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { useUserStore } from '@/store/modules/user'
  import type { FormInstance, FormRules } from 'element-plus'
  import defaultAvatar from '@/assets/images/user/avatar.webp'
  import { fetchUpdateProfile } from '@/api/system-manage'

  defineOptions({ name: 'UserCenter' })

  const userStore = useUserStore()
  const userInfo = computed(() => userStore.getUserInfo)
  const userAvatar = computed(() => form.avatar || userInfo.value.avatar || defaultAvatar)
  const roleText = computed(() => userInfo.value.roles?.join('、') || '暂无角色')
  const ruleFormRef = ref<FormInstance>()
  const editing = ref(false)
  const saving = ref(false)

  const form = reactive({
    email: '',
    avatar: '',
    nickName: ''
  })

  const rules = reactive<FormRules>({})

  const toggleEdit = () => {
    editing.value = !editing.value
    if (editing.value) {
      // 切换到编辑模式时，填充当前数据
      form.email = userInfo.value.email || ''
      form.avatar = userInfo.value.avatar || ''
      form.nickName = userInfo.value.userName || ''
    } else {
      // 取消编辑时清除验证
      ruleFormRef.value?.clearValidate()
    }
  }

  const saveProfile = async () => {
    if (!ruleFormRef.value) return
    await ruleFormRef.value.validate()
    saving.value = true
    try {
      await fetchUpdateProfile({
        nickName: form.nickName || undefined,
        userEmail: form.email || undefined,
        avatar: form.avatar || undefined
      })
      ElMessage.success('个人资料已更新')
      editing.value = false
      // 刷新用户信息
      const { fetchGetUserInfo } = await import('@/api/auth')
      const newInfo = await fetchGetUserInfo()
      userStore.setUserInfo(newInfo)
    } catch {
      // 错误已由 HTTP 拦截器处理
    } finally {
      saving.value = false
    }
  }
</script>