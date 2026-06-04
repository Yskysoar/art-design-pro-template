<template>
  <ElDialog
    :title="dialogTitle"
    :model-value="visible"
    @update:model-value="handleCancel"
    width="860px"
    align-center
    class="menu-dialog"
    @closed="handleClosed"
  >
    <ArtForm
      ref="formRef"
      v-model="form"
      :items="formItems"
      :rules="rules"
      :span="width > 640 ? 12 : 24"
      :gutter="20"
      label-width="100px"
      :show-reset="false"
      :show-submit="false"
    >
      <template #menuType>
        <ElRadioGroup v-model="form.menuType" :disabled="disableMenuType">
          <ElRadioButton value="menu" label="menu">{{ $t('systemManage.menuDialog.menu') }}</ElRadioButton>
          <ElRadioButton value="button" label="button">{{ $t('systemManage.menuDialog.button') }}</ElRadioButton>
        </ElRadioGroup>
      </template>
    </ArtForm>

    <template #footer>
      <span class="dialog-footer">
        <ElButton @click="handleCancel">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" @click="handleSubmit">{{ $t('common.confirm') }}</ElButton>
      </span>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import type { FormRules } from 'element-plus'
  import { ElIcon, ElTooltip } from 'element-plus'
  import { QuestionFilled } from '@element-plus/icons-vue'
  import { formatMenuTitle } from '@/utils/router'
  import type { AppRouteRecord } from '@/types/router'
  import type { FormItem } from '@/components/core/forms/art-form/index.vue'
  import ArtForm from '@/components/core/forms/art-form/index.vue'
  import { useWindowSize } from '@vueuse/core'
  import { useI18n } from 'vue-i18n'

  const { width } = useWindowSize()
  const { t } = useI18n()

  /**
   * 创建带 tooltip 的表单标签
   * @param label 标签文本
   * @param tooltip 提示文本
   * @returns 渲染函数
   */
  const createLabelTooltip = (label: string, tooltip: string) => {
    return () =>
      h('span', { class: 'flex items-center' }, [
        h('span', label),
        h(
          ElTooltip,
          {
            content: tooltip,
            placement: 'top'
          },
          () => h(ElIcon, { class: 'ml-0.5 cursor-help' }, () => h(QuestionFilled))
        )
      ])
  }

  interface MenuFormData {
    id: number
    name: string
    path: string
    label: string
    component: string
    icon: string
    isEnable: boolean
    sort: number
    isMenu: boolean
    keepAlive: boolean
    isHide: boolean
    isHideTab: boolean
    link: string
    isIframe: boolean
    showBadge: boolean
    showTextBadge: string
    fixedTab: boolean
    activePath: string
    roles: string[]
    isFullPage: boolean
    authName: string
    authLabel: string
    authIcon: string
    authSort: number
  }

  interface Props {
    visible: boolean
    editData?: AppRouteRecord | any
    type?: 'menu' | 'button'
    lockType?: boolean
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit', data: MenuFormData): void
  }

  const props = withDefaults(defineProps<Props>(), {
    visible: false,
    type: 'menu',
    lockType: false
  })

  const emit = defineEmits<Emits>()

  const formRef = ref()
  const isEdit = ref(false)

  const form = reactive<MenuFormData & { menuType: 'menu' | 'button' }>({
    menuType: 'menu',
    id: 0,
    name: '',
    path: '',
    label: '',
    component: '',
    icon: '',
    isEnable: true,
    sort: 1,
    isMenu: true,
    keepAlive: true,
    isHide: false,
    isHideTab: false,
    link: '',
    isIframe: false,
    showBadge: false,
    showTextBadge: '',
    fixedTab: false,
    activePath: '',
    roles: [],
    isFullPage: false,
    authName: '',
    authLabel: '',
    authIcon: '',
    authSort: 1
  })

  const rules = reactive<FormRules>({
    name: [
      { required: true, message: t('systemManage.menuDialog.menuNameRequired'), trigger: 'blur' },
      { min: 2, max: 20, message: t('systemManage.menuDialog.nameLength'), trigger: 'blur' }
    ],
    path: [{ required: true, message: t('systemManage.menuDialog.routeRequired'), trigger: 'blur' }],
    label: [{ required: true, message: t('systemManage.menuDialog.permissionCodeRequired'), trigger: 'blur' }],
    authName: [{ required: true, message: t('systemManage.menuDialog.permissionNameRequired'), trigger: 'blur' }],
    authLabel: [{ required: true, message: t('systemManage.menuDialog.permissionCodeRequired'), trigger: 'blur' }]
  })

  /**
   * 表单项配置
   */
  const formItems = computed<FormItem[]>(() => {
    const baseItems: FormItem[] = [{ label: t('systemManage.menuDialog.menuType'), key: 'menuType', span: 24 }]

    // Switch 组件的 span：小屏幕 12，大屏幕 6
    const switchSpan = width.value < 640 ? 12 : 6

    if (form.menuType === 'menu') {
      return [
        ...baseItems,
        { label: t('systemManage.menuDialog.menuName'), key: 'name', type: 'input', props: { placeholder: t('systemManage.menuDialog.menuName') } },
        {
          label: createLabelTooltip(
            t('systemManage.menuDialog.routePath'),
            t('systemManage.menuDialog.routeTooltip')
          ),
          key: 'path',
          type: 'input',
          props: { placeholder: t('systemManage.menuDialog.routePlaceholder') }
        },
        { label: t('systemManage.menuDialog.permissionCode'), key: 'label', type: 'input', props: { placeholder: t('systemManage.menuDialog.permissionCodePlaceholder') } },
        {
          label: createLabelTooltip(
            t('systemManage.menuDialog.componentPath'),
            t('systemManage.menuDialog.componentTooltip')
          ),
          key: 'component',
          type: 'input',
          props: { placeholder: t('systemManage.menuDialog.componentPlaceholder') }
        },
        { label: t('systemManage.menuDialog.icon'), key: 'icon', type: 'input', props: { placeholder: t('systemManage.menuDialog.iconPlaceholder') } },
        {
          label: createLabelTooltip(
            t('systemManage.menuDialog.rolePermission'),
            t('systemManage.menuDialog.rolePermissionTooltip')
          ),
          key: 'roles',
          type: 'inputtag',
          props: { placeholder: t('systemManage.menuDialog.rolePermissionPlaceholder') }
        },
        {
          label: t('systemManage.menuDialog.menuSort'),
          key: 'sort',
          type: 'number',
          props: { min: 1, controlsPosition: 'right', style: { width: '100%' } }
        },
        {
          label: t('systemManage.menuDialog.externalLink'),
          key: 'link',
          type: 'input',
          props: { placeholder: '如：https://www.example.com' }
        },
        {
          label: t('systemManage.menuDialog.badge'),
          key: 'showTextBadge',
          type: 'input',
          props: { placeholder: 'New / Hot' }
        },
        {
          label: createLabelTooltip(
            t('systemManage.menuDialog.activePath'),
            t('systemManage.menuDialog.activePathTooltip')
          ),
          key: 'activePath',
          type: 'input',
          props: { placeholder: t('systemManage.menuDialog.activePathPlaceholder') }
        },
        { label: t('systemManage.menuDialog.enabled'), key: 'isEnable', type: 'switch', span: switchSpan },
        { label: t('systemManage.menuDialog.keepAlive'), key: 'keepAlive', type: 'switch', span: switchSpan },
        { label: t('systemManage.menuDialog.hideMenu'), key: 'isHide', type: 'switch', span: switchSpan },
        { label: t('systemManage.menuDialog.iframe'), key: 'isIframe', type: 'switch', span: switchSpan },
        { label: t('systemManage.menuDialog.showBadge'), key: 'showBadge', type: 'switch', span: switchSpan },
        { label: t('systemManage.menuDialog.fixedTab'), key: 'fixedTab', type: 'switch', span: switchSpan },
        { label: t('systemManage.menuDialog.hideTab'), key: 'isHideTab', type: 'switch', span: switchSpan },
        { label: t('systemManage.menuDialog.fullPage'), key: 'isFullPage', type: 'switch', span: switchSpan }
      ]
    } else {
      return [
        ...baseItems,
        {
          label: t('systemManage.menuDialog.permissionName'),
          key: 'authName',
          type: 'input',
          props: { placeholder: t('systemManage.menuDialog.permissionNamePlaceholder') }
        },
        {
          label: t('systemManage.menuDialog.permissionCode'),
          key: 'authLabel',
          type: 'input',
          props: { placeholder: 'add / edit / delete' }
        },
        {
          label: t('systemManage.menuDialog.permissionSort'),
          key: 'authSort',
          type: 'number',
          props: { min: 1, controlsPosition: 'right', style: { width: '100%' } }
        }
      ]
    }
  })

  const dialogTitle = computed(() => {
    if (form.menuType === 'menu') {
      return isEdit.value ? t('systemManage.menuDialog.editMenu') : t('systemManage.menuDialog.newMenu')
    }
    return isEdit.value ? t('systemManage.menuDialog.editButton') : t('systemManage.menuDialog.newButton')
  })

  /**
   * 是否禁用菜单类型切换
   */
  const disableMenuType = computed(() => {
    if (isEdit.value) return true
    if (!isEdit.value && form.menuType === 'menu' && props.lockType) return true
    return false
  })

  /**
   * 重置表单数据
   */
  const resetForm = (): void => {
    formRef.value?.reset()
    form.menuType = 'menu'
  }

  /**
   * 加载表单数据（编辑模式）
   */
  const loadFormData = (): void => {
    if (!props.editData) return

    isEdit.value = true

    if (form.menuType === 'menu') {
      const row = props.editData
      form.id = row.id || 0
      form.name = formatMenuTitle(row.meta?.title || '')
      form.path = row.path || ''
      form.label = row.name || ''
      form.component = row.component || ''
      form.icon = row.meta?.icon || ''
      form.sort = row.meta?.sort || 1
      form.isMenu = row.meta?.isMenu ?? true
      form.keepAlive = row.meta?.keepAlive ?? false
      form.isHide = row.meta?.isHide ?? false
      form.isHideTab = row.meta?.isHideTab ?? false
      form.isEnable = row.meta?.isEnable ?? true
      form.link = row.meta?.link || ''
      form.isIframe = row.meta?.isIframe ?? false
      form.showBadge = row.meta?.showBadge ?? false
      form.showTextBadge = row.meta?.showTextBadge || ''
      form.fixedTab = row.meta?.fixedTab ?? false
      form.activePath = row.meta?.activePath || ''
      form.roles = row.meta?.roles || []
      form.isFullPage = row.meta?.isFullPage ?? false
    } else {
      const row = props.editData
      form.id = row.id || 0
      form.authName = row.title || ''
      form.authLabel = row.authMark || ''
      form.authIcon = row.icon || ''
      form.authSort = row.sort || 1
    }
  }

  /**
   * 提交表单
   */
  const handleSubmit = async (): Promise<void> => {
    if (!formRef.value) return

    try {
      await formRef.value.validate()
      emit('submit', { ...form })
      ElMessage.success(t('systemManage.menuDialog.submitSuccess', { action: isEdit.value ? t('common.edit') : t('common.add') }))
      handleCancel()
    } catch {
      ElMessage.error(t('systemManage.menuDialog.validateFailed'))
    }
  }

  /**
   * 取消操作
   */
  const handleCancel = (): void => {
    emit('update:visible', false)
  }

  /**
   * 对话框关闭后的回调
   */
  const handleClosed = (): void => {
    resetForm()
    isEdit.value = false
  }

  /**
   * 监听对话框显示状态
   */
  watch(
    () => props.visible,
    (newVal) => {
      if (newVal) {
        form.menuType = props.type
        nextTick(() => {
          if (props.editData) {
            loadFormData()
          }
        })
      }
    }
  )

  /**
   * 监听菜单类型变化
   */
  watch(
    () => props.type,
    (newType) => {
      if (props.visible) {
        form.menuType = newType
      }
    }
  )
</script>
