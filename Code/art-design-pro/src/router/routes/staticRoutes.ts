import { AppRouteRecordRaw } from '@/utils/router'

/**
 * 静态路由配置。
 *
 * 静态路由不依赖后端菜单注册，主要用于登录、异常页、外链容器和需要主布局承载但不显示在侧边栏的页面。
 * 404 通配符必须放在最后，避免提前拦截后续静态路由。
 */
export const staticRoutes: AppRouteRecordRaw[] = [
  {
    path: '/auth/login',
    name: 'Login',
    component: () => import('@views/auth/login/index.vue'),
    meta: { title: 'menus.login.title', isHideTab: true }
  },
  {
    path: '/auth/register',
    name: 'Register',
    component: () => import('@views/auth/register/index.vue'),
    meta: { title: 'menus.register.title', isHideTab: true }
  },
  {
    path: '/auth/forget-password',
    name: 'ForgetPassword',
    component: () => import('@views/auth/forget-password/index.vue'),
    meta: { title: 'menus.forgetPassword.title', isHideTab: true }
  },
  {
    path: '/403',
    name: 'Exception403',
    component: () => import('@views/exception/403/index.vue'),
    meta: { title: '403', isHideTab: true }
  },
  {
    path: '/outside',
    component: () => import('@views/index/index.vue'),
    name: 'Outside',
    meta: { title: 'menus.outside.title' },
    children: [
      {
        path: '/outside/iframe/:path',
        name: 'Iframe',
        component: () => import('@/views/outside/Iframe.vue'),
        meta: { title: 'iframe' }
      }
    ]
  },
  {
    path: '/social',
    component: () => import('@views/index/index.vue'),
    name: 'Social',
    meta: { title: '社交', isHide: true },
    children: [
      {
        path: 'chat',
        name: 'SocialChat',
        component: () => import('@views/social/chat/index.vue'),
        meta: { title: '聊天', keepAlive: true, isHide: true }
      }
    ]
  },
  {
    path: '/500',
    name: 'Exception500',
    component: () => import('@views/exception/500/index.vue'),
    meta: { title: '500', isHideTab: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'Exception404',
    component: () => import('@views/exception/404/index.vue'),
    meta: { title: '404', isHideTab: true }
  }
]
