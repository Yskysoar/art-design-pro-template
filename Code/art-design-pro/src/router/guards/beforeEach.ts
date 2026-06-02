/**
 * 路由全局前置守卫模块
 *
 * 提供完整的路由导航守卫功能
 *
 * ## 主要功能
 *
 * - 登录状态验证和重定向
 * - 动态路由注册和权限控制
 * - 菜单数据获取和处理（前端/后端模式）
 * - 用户信息获取和缓存
 * - 页面标题设置
 * - 工作标签页管理
 * - 进度条和加载动画控制
 * - 静态路由识别和处理
 * - 错误处理和异常跳转
 *
 * ## 使用场景
 *
 * - 路由跳转前的权限验证
 * - 动态菜单加载和路由注册
 * - 用户登录状态管理
 * - 页面访问控制
 * - 路由级别的加载状态管理
 *
 * ## 工作流程
 *
 * 1. 检查登录状态，未登录跳转到登录页
 * 2. 首次访问时获取用户信息和菜单数据
 * 3. 根据权限动态注册路由
 * 4. 设置页面标题和工作标签页
 * 5. 处理根路径重定向到首页
 * 6. 未匹配路由跳转到 404 页面
 *
 * @module router/guards/beforeEach
 */
import type { Router, RouteLocationNormalized, NavigationGuardNext } from 'vue-router'
import { nextTick } from 'vue'
import NProgress from 'nprogress'
import { useSettingStore } from '@/store/modules/setting'
import { useUserStore } from '@/store/modules/user'
import { useMenuStore } from '@/store/modules/menu'
import { setWorktab } from '@/utils/navigation'
import { setPageTitle } from '@/utils/router'
import { RoutesAlias } from '../routesAlias'
import { staticRoutes } from '../routes/staticRoutes'
import { loadingService } from '@/utils/ui'
import { useCommon } from '@/hooks/core/useCommon'
import { useWorktabStore } from '@/store/modules/worktab'
import { fetchGetUserInfo } from '@/api/auth'
import { ApiStatus } from '@/utils/http/status'
import { isHttpError } from '@/utils/http/error'
import { useAppMode } from '@/hooks/core/useAppMode'
import type { AppRouteRecord } from '@/types/router'
import { RouteRegistry, MenuProcessor, IframeRouteManager, RoutePermissionValidator } from '../core'

// 路由注册器实例
let routeRegistry: RouteRegistry | null = null

// 菜单处理器实例
const menuProcessor = new MenuProcessor()

class RouteInitCancelledError extends Error {}

// 跟踪是否需要关闭 loading
let pendingLoading = false

// Route init failure flag for status inspection only.
let routeInitFailed = false

// 路由初始化进行中标记，防止并发请求
let routeInitInProgress = false

// 复用动态路由初始化任务，避免快速切换页面时重复请求用户信息和菜单。
let routeInitPromise: Promise<AppRouteRecord[]> | null = null

// 跟踪最新导航，防止旧导航在初始化完成后反抢新页面。
let latestNavigationId = 0

// 跟踪初始化代际，防止登出或重新登录后旧任务继续注册旧路由。
let routeInitGeneration = 0

/**
 * 获取 pendingLoading 状态
 */
export function getPendingLoading(): boolean {
  return pendingLoading
}

/**
 * 重置 pendingLoading 状态
 */
export function resetPendingLoading(): void {
  pendingLoading = false
}

/**
 * 获取路由初始化失败状态
 */
export function getRouteInitFailed(): boolean {
  return routeInitFailed
}

/**
 * 重置路由初始化状态（用于重新登录场景）
 */
export function resetRouteInitState(): void {
  routeInitFailed = false
  routeInitInProgress = false
  routeInitPromise = null
  latestNavigationId++
  routeInitGeneration++
}

/**
 * 设置路由全局前置守卫
 */
export function setupBeforeEachGuard(router: Router): void {
  // 初始化路由注册器
  routeRegistry = new RouteRegistry(router)

  router.beforeEach(
    async (
      to: RouteLocationNormalized,
      from: RouteLocationNormalized,
      next: NavigationGuardNext
    ) => {
      try {
        await handleRouteGuard(to, from, next, router)
      } catch (error) {
        console.error('[RouteGuard] 路由守卫处理失败:', error)
        closeLoading()
        next({ name: 'Exception500' })
      }
    }
  )
}

/**
 * 关闭 loading 效果
 */
function closeLoading(): void {
  if (pendingLoading) {
    nextTick(() => {
      loadingService.hideLoading()
      pendingLoading = false
    })
  }
}

/**
 * 处理路由守卫逻辑
 */
async function handleRouteGuard(
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext,
  router: Router
): Promise<void> {
  const settingStore = useSettingStore()
  const userStore = useUserStore()
  const navigationId = ++latestNavigationId

  // 启动进度条
  if (settingStore.showNprogress) {
    NProgress.start()
  }

  // 1. 检查登录状态
  if (!handleLoginStatus(to, userStore, next)) {
    return
  }

  // 静态路由不依赖动态菜单。登录页、注册页等必须直接放行，
  // 避免 token 残留或菜单加载失败时把认证页带进 500/loading 状态。
  if (isStaticRoute(to.path)) {
    setPageTitle(to)
    next()
    return
  }

  // 2. Register dynamic routes.
  if (!routeRegistry?.isRegistered() && userStore.isLogin) {
    await handleDynamicRoutes(to, next, router, navigationId)
    return
  }

  // 3. 处理根路径重定向
  if (handleRootPathRedirect(to, next)) {
    return
  }

  // 4. 处理已匹配的路由
  if (to.matched.length > 0) {
    setWorktab(to)
    setPageTitle(to)
    next()
    return
  }

  // 5. 未匹配到路由，跳转到 404
  next({ name: 'Exception404' })
}

/**
 * 处理登录状态
 * @returns true 表示可以继续，false 表示已处理跳转
 */
function handleLoginStatus(
  to: RouteLocationNormalized,
  userStore: ReturnType<typeof useUserStore>,
  next: NavigationGuardNext
): boolean {
  if (userStore.isLogin && to.path === RoutesAlias.Login) {
    next({ path: '/', replace: true })
    return false
  }

  // 已登录或访问登录页或静态路由，直接放行
  if (userStore.isLogin || to.path === RoutesAlias.Login || isStaticRoute(to.path)) {
    return true
  }

  // 未登录且访问需要权限的页面，跳转到登录页并携带 redirect 参数
  userStore.logOut()
  next({
    name: 'Login',
    query: { redirect: to.fullPath }
  })
  return false
}

/**
 * 检查路由是否为静态路由
 */
function isStaticRoute(path: string): boolean {
  const checkRoute = (routes: any[], targetPath: string): boolean => {
    return routes.some((route) => {
      // 404 catch-all 路由不应视为可匿名访问的静态页，
      // 否则未登录时手动输入任意地址会直接落到 404，无法跳转登录页。
      if (route.name === 'Exception404') {
        return false
      }

      // 处理动态路由参数匹配
      const routePath = route.path
      const pattern = routePath.replace(/:[^/]+/g, '[^/]+').replace(/\*/g, '.*')
      const regex = new RegExp(`^${pattern}$`)

      if (regex.test(targetPath)) {
        return true
      }
      if (route.children && route.children.length > 0) {
        return checkRoute(route.children, targetPath)
      }
      return false
    })
  }

  return checkRoute(staticRoutes, path)
}

/**
 * 处理动态路由注册
 */
async function handleDynamicRoutes(
  to: RouteLocationNormalized,
  next: NavigationGuardNext,
  router: Router,
  navigationId: number
): Promise<void> {
  try {
    const menuList = await ensureDynamicRoutesInitialized(router)

    const userStore = useUserStore()
    if (!userStore.isLogin || !userStore.accessToken) {
      next(false)
      return
    }

    if (navigationId !== latestNavigationId) {
      next(false)
      return
    }

    // 静态路由不依赖菜单权限，初始化后直接恢复目标地址。
    if (isStaticRoute(to.path)) {
      closeLoading()
      next({
        path: to.path,
        query: to.query,
        hash: to.hash,
        replace: true
      })
      return
    }

    // 验证目标路径权限
    const { homePath } = useCommon()
    const { path: validatedPath, hasPermission } = RoutePermissionValidator.validatePath(
      to.path,
      menuList,
      homePath.value || '/'
    )

    // 重新导航到目标路由
    if (!hasPermission) {
      // 无权限访问，跳转到首页
      closeLoading()

      // 输出警告信息
      console.warn(`[RouteGuard] 用户无权限访问路径: ${to.path}，已跳转到首页`)

      // 直接跳转到首页
      next({
        path: validatedPath,
        replace: true
      })
    } else {
      // 有权限，正常导航
      closeLoading()
      next({
        path: to.path,
        query: to.query,
        hash: to.hash,
        replace: true
      })
    }
  } catch (error) {
    // 关闭 loading
    closeLoading()

    // 401 错误：axios 拦截器已处理退出登录，取消当前导航
    if (isUnauthorizedError(error)) {
      next(false)
      return
    }

    if (error instanceof RouteInitCancelledError) {
      next(false)
      return
    }

    // Mark current init as failed and allow future navigation to retry.
    routeInitFailed = true
    routeInitPromise = null
    routeInitInProgress = false
    // 输出详细错误信息，便于排查
    if (isHttpError(error)) {
      console.error(`[RouteGuard] 错误码: ${error.code}, 消息: ${error.message}`)
    }

    // 跳转到 500 页面，使用 replace 避免产生历史记录
    next({ name: 'Exception500', replace: true })
  }
}

/**
 * 确保动态路由只初始化一次。
 */
async function ensureDynamicRoutesInitialized(router: Router): Promise<AppRouteRecord[]> {
  if (routeInitPromise) {
    return routeInitPromise
  }

  routeInitInProgress = true
  pendingLoading = true
  loadingService.showLoading()

  const generation = routeInitGeneration
  const startToken = useUserStore().accessToken
  routeInitPromise = initializeDynamicRoutes(router, generation, startToken)

  try {
    return await routeInitPromise
  } finally {
    routeInitInProgress = false
    routeInitPromise = null
  }
}

/**
 * 初始化动态路由、菜单和工作标签页。
 */
async function initializeDynamicRoutes(
  router: Router,
  generation: number,
  startToken: string
): Promise<AppRouteRecord[]> {
  const menuList = await loadUserInfoAndMenus()

  assertRouteInitStillValid(generation, startToken)

  if (!menuProcessor.validateMenuList(menuList)) {
    throw new Error('获取菜单列表失败，请重新登录')
  }

  normalizeBackendMenus(menuList)

  routeRegistry?.register(menuList)

  const menuStore = useMenuStore()
  menuStore.setMenuList(menuList)
  menuStore.addRemoveRouteFns(routeRegistry?.getRemoveRouteFns() || [])

  IframeRouteManager.getInstance().save()
  useWorktabStore().validateWorktabs(router)

  return menuList
}

/**
 * 兼容旧版后端菜单数据，并稳定系统菜单展示顺序。
 *
 * 当前标准数据已经维护在 mock SQL 中；本函数用于本地数据库尚未更新时，
 * 将旧的“文章管理 -> 评论敏感词”映射为“系统管理 -> 敏感词库”。
 */
function normalizeBackendMenus(items: AppRouteRecord[]): void {
  const dashboard = items.find((item) => item.name === 'Dashboard' || item.path === '/dashboard')
  const article = items.find((item) => item.name === 'Article' || item.path === '/article')
  const system = items.find((item) => item.name === 'System' || item.path === '/system')

  if (article?.children && system) {
    const sensitiveIndex = article.children.findIndex(
      (child) => child.name === 'ArticleComment' || child.name === 'SensitiveWord'
    )
    if (sensitiveIndex >= 0) {
      const [sensitiveRoute] = article.children.splice(sensitiveIndex, 1)
      system.children = system.children || []
      if (!system.children.some((child) => child.name === 'SensitiveWord')) {
        system.children.push({
          ...sensitiveRoute,
          path: 'sensitive-word',
          name: 'SensitiveWord',
          component: '/article/comment',
          meta: {
            ...sensitiveRoute.meta,
            title: '敏感词库',
            icon: 'ri:shield-keyhole-line',
            keepAlive: false,
            authList: [{ title: '管理敏感词', authMark: 'system:sensitive-word' }]
          }
        })
      }
    }
  }

  if (system) {
    system.children = system.children || []
    system.children = system.children.filter((child) => child.name !== 'SystemSettings')
    system.children.sort((left, right) => systemMenuOrder(left) - systemMenuOrder(right))
  }

  items.sort((left, right) => topMenuOrder(left, dashboard, article, system) - topMenuOrder(right, dashboard, article, system))
}

function topMenuOrder(
  route: AppRouteRecord,
  dashboard?: AppRouteRecord,
  article?: AppRouteRecord,
  system?: AppRouteRecord
): number {
  if (route === dashboard || route.name === 'Dashboard' || route.path === '/dashboard') return 1
  if (route === article || route.name === 'Article' || route.path === '/article') return 2
  if (route === system || route.name === 'System' || route.path === '/system') return 3
  return 99
}

function systemMenuOrder(route: AppRouteRecord): number {
  const order: Record<string, number> = {
    User: 1,
    Role: 2,
    Org: 3,
    Config: 4,
    Menus: 5,
    SensitiveWord: 6,
    UserCenter: 99
  }
  return order[String(route.name)] ?? 90
}

/**
 * 确认初始化任务仍属于当前登录态。
 */
function assertRouteInitStillValid(generation: number, startToken: string): void {
  const userStore = useUserStore()
  if (
    generation !== routeInitGeneration ||
    !userStore.isLogin ||
    !userStore.accessToken ||
    userStore.accessToken !== startToken
  ) {
    throw new RouteInitCancelledError('路由初始化已被新的登录态取代')
  }
}

/**
 * 按权限模式加载用户信息和菜单。
 */
async function loadUserInfoAndMenus(): Promise<AppRouteRecord[]> {
  const { isBackendMode } = useAppMode()

  if (isBackendMode.value) {
    const [, menuList] = await Promise.all([fetchUserInfo(), menuProcessor.getMenuList()])
    return menuList
  }

  await fetchUserInfo()
  return menuProcessor.getMenuList()
}

/**
 * 获取用户信息
 */
async function fetchUserInfo(): Promise<void> {
  const userStore = useUserStore()
  const data = await fetchGetUserInfo()
  userStore.setUserInfo(data)
  // 检查并清理工作台标签页（如果是不同用户登录）
  userStore.checkAndClearWorktabs()
}

/**
 * 重置路由相关状态
 */
export function resetRouterState(_delay = 0): void {
  routeRegistry?.unregister()
  IframeRouteManager.getInstance().clear()

  const menuStore = useMenuStore()
  menuStore.removeAllDynamicRoutes()
  menuStore.setMenuList([])

  // 重置路由初始化状态，允许重新登录后再次初始化
  resetRouteInitState()
}

/**
 * 处理根路径重定向到首页
 * @returns true 表示已处理跳转，false 表示无需跳转
 */
function handleRootPathRedirect(to: RouteLocationNormalized, next: NavigationGuardNext): boolean {
  if (to.path !== '/') {
    return false
  }

  const { homePath } = useCommon()
  if (homePath.value && homePath.value !== '/') {
    next({ path: homePath.value, replace: true })
    return true
  }

  return false
}

/**
 * 判断是否为未授权错误（401）
 */
function isUnauthorizedError(error: unknown): boolean {
  return isHttpError(error) && error.code === ApiStatus.unauthorized
}
