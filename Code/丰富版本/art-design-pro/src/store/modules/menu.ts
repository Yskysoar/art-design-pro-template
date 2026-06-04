/**
 * 菜单状态管理模块
 *
 * 提供菜单数据和动态路由的状态管理
 *
 * ## 主要功能
 *
 * - 菜单列表存储和管理
 * - 首页路径配置
 * - 动态路由注册和移除
 * - 路由移除函数管理
 * - 菜单宽度配置
 * - 菜单数据缓存
 *
 * ## 使用场景
 *
 * - 动态菜单加载和渲染
 * - 路由权限控制
 * - 首页路径动态设置
 * - 登出时清理动态路由
 *
 * ## 工作流程
 *
 * 1. 获取菜单数据（前端/后端模式）
 * 2. 设置菜单列表和首页路径
 * 3. 注册动态路由并保存移除函数
 * 4. 登出时调用移除函数清理路由
 *
 * @module store/modules/menu
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { AppRouteRecord } from '@/types/router'
import { getFirstMenuPath } from '@/utils'
import { HOME_PAGE_PATH } from '@/router'

/**
 * 菜单状态管理
 * 管理应用的菜单列表、首页路径、菜单宽度和动态路由移除函数
 */
export const useMenuStore = defineStore('menuStore', () => {
  /** 首页路径 */
  const homePath = ref(HOME_PAGE_PATH)
  /** 菜单列表 */
  const menuList = ref<AppRouteRecord[]>([])
  /** 菜单宽度 */
  const menuWidth = ref('')
  /** 存储路由移除函数的数组 */
  const removeRouteFns = ref<(() => void)[]>([])
  /** 菜单缓存（用于加速登录） */
  const menuCache = ref<AppRouteRecord[] | null>(null)
  /** 缓存时间戳 */
  const cacheTimestamp = ref<number>(0)

  /** 缓存有效期（5分钟） */
  const CACHE_TTL = 5 * 60 * 1000

  /**
   * 设置菜单列表
   * @param list 菜单路由记录数组
   */
  const setMenuList = (list: AppRouteRecord[]) => {
    menuList.value = list
    setHomePath(HOME_PAGE_PATH || getFirstMenuPath(list))

    if (list.length > 0) {
      // 只缓存有效菜单，避免登出清空菜单时把空数组作为可用缓存。
      menuCache.value = list
      cacheTimestamp.value = Date.now()
    } else {
      clearMenuCache()
    }
  }

  /**
   * 获取缓存的菜单列表
   * @returns 缓存的菜单列表，如果缓存过期则返回 null
   */
  const getCachedMenuList = (): AppRouteRecord[] | null => {
    if (!menuCache.value || menuCache.value.length === 0) return null
    if (Date.now() - cacheTimestamp.value > CACHE_TTL) {
      // 缓存过期，清空
      menuCache.value = null
      return null
    }
    return menuCache.value
  }

  /**
   * 清空菜单缓存
   */
  const clearMenuCache = () => {
    menuCache.value = null
    cacheTimestamp.value = 0
  }

  /**
   * 获取首页路径
   * @returns 首页路径字符串
   */
  const getHomePath = () => homePath.value

  /**
   * 设置主页路径
   * @param path 主页路径
   */
  const setHomePath = (path: string) => {
    homePath.value = path
  }

  /**
   * 添加路由移除函数
   * @param fns 要添加的路由移除函数数组
   */
  const addRemoveRouteFns = (fns: (() => void)[]) => {
    removeRouteFns.value.push(...fns)
  }

  /**
   * 移除所有动态路由
   * 执行所有存储的路由移除函数并清空数组
   */
  const removeAllDynamicRoutes = () => {
    removeRouteFns.value.forEach((fn) => fn())
    removeRouteFns.value = []
  }

  /**
   * 清空路由移除函数数组
   */
  const clearRemoveRouteFns = () => {
    removeRouteFns.value = []
  }

  return {
    menuList,
    menuWidth,
    removeRouteFns,
    setMenuList,
    getHomePath,
    setHomePath,
    addRemoveRouteFns,
    removeAllDynamicRoutes,
    clearRemoveRouteFns,
    getCachedMenuList,
    clearMenuCache
  }
})
