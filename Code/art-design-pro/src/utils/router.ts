/**
 * 路由工具函数
 *
 * 提供路由相关的工具函数
 *
 * @module utils/router
 */
import { RouteLocationNormalized, RouteRecordRaw } from 'vue-router'
import AppConfig from '@/config'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import i18n, { $t } from '@/locales'

/** 扩展的路由配置类型 */
export type AppRouteRecordRaw = RouteRecordRaw & {
  hidden?: boolean
}

/** 兼容后端历史菜单标题，避免面包屑、标签页和双列菜单出现中英混排 */
const MENU_TITLE_ALIASES: Record<string, string> = {
  Dashboard: 'menus.dashboard.title',
  Console: 'menus.dashboard.console',
  Article: 'menus.article.title',
  ArticleList: 'menus.article.articleList',
  ArticlePublish: 'menus.article.articlePublish',
  ArticleDetail: 'menus.article.articleDetail',
  'System Settings': 'menus.system.title',
  System: 'menus.system.title',
  'User Manage': 'menus.system.user',
  User: 'menus.system.user',
  'Role Manage': 'menus.system.role',
  Role: 'menus.system.role',
  'Menu Manage': 'menus.system.menu',
  Menus: 'menus.system.menu',
  'Org Manage': 'menus.system.org',
  Org: 'menus.system.org',
  'Config Manage': 'menus.system.config',
  Config: 'menus.system.config',
  'Sensitive Words': 'menus.system.sensitiveWord',
  SensitiveWord: 'menus.system.sensitiveWord',
  组织管理: 'menus.system.org',
  配置管理: 'menus.system.config',
  敏感词库: 'menus.system.sensitiveWord'
}

/** 顶部进度条配置 */
export const configureNProgress = () => {
  NProgress.configure({
    easing: 'ease',
    speed: 600,
    showSpinner: false,
    parent: 'body'
  })
}

/**
 * 设置页面标题，根据路由元信息和系统信息拼接标题
 * @param to 当前路由对象
 */
export const setPageTitle = (to: RouteLocationNormalized): void => {
  const { title } = to.meta
  if (title) {
    setTimeout(() => {
      document.title = `${formatMenuTitle(String(title))} - ${AppConfig.systemInfo.name}`
    }, 150)
  }
}

/**
 * 格式化菜单标题
 * @param title 菜单标题，可以是 i18n 的 key，也可以是字符串
 * @returns 格式化后的菜单标题
 */
export const formatMenuTitle = (title: string): string => {
  if (title) {
    const i18nKey = MENU_TITLE_ALIASES[title] || title

    if (i18nKey.startsWith('menus.')) {
      // 使用 te() 方法检查翻译键值是否存在，避免控制台警告
      if (i18n.global.te(i18nKey)) {
        return $t(i18nKey)
      } else {
        // 如果翻译不存在，返回键值的最后部分作为fallback
        return i18nKey.split('.').pop() || i18nKey
      }
    }
    return i18nKey
  }
  return ''
}
