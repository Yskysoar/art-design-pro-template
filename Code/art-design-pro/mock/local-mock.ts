import type { Connect, Plugin, ViteDevServer } from 'vite'
import { parse as parseUrl } from 'node:url'

type JsonRecord = Record<string, unknown>

interface MockRequest {
  method: string
  pathname: string
  query: Record<string, string>
  body: JsonRecord
}

interface MockResponse<T = unknown> {
  code: number
  msg: string
  data: T
}

interface PageResult<T> {
  records: T[]
  current: number
  size: number
  total: number
}

interface UserListItem {
  id: number
  avatar: string
  status: string
  userName: string
  userGender: string
  nickName: string
  userPhone: string
  userEmail: string
  userRoles: string[]
  createBy: string
  createTime: string
  updateBy: string
  updateTime: string
}

interface RoleListItem {
  roleId: number
  roleName: string
  roleCode: string
  description: string
  enabled: boolean
  createTime: string
}

interface ArticleTypeItem {
  id: number
  name: string
}

interface ArticleDetail {
  id: number
  title: string
  categoryId: number
  categoryName: string
  blog_class: string
  coverUrl: string
  summary: string
  contentHtml: string
  html_content: string
  visible: boolean
  status: string
  viewCount: number
  createBy: string
  createTime: string
  attachments: unknown[]
}

interface AppRouteRecord {
  id?: number
  path: string
  name?: string
  component?: string
  redirect?: string
  meta: {
    title: string
    icon?: string
    roles?: string[]
    authList?: Array<{ title: string; authMark: string }>
    keepAlive?: boolean
    fixedTab?: boolean
    isHide?: boolean
    isHideTab?: boolean
    activePath?: string
    link?: string
    isIframe?: boolean
  }
  children?: AppRouteRecord[]
}

const TOKEN = 'local-mock-access-token'
const REFRESH_TOKEN = 'local-mock-refresh-token'

const users: UserListItem[] = [
  {
    id: 1,
    avatar: '/src/assets/images/avatar/avatar1.webp',
    status: '1',
    userName: 'admin',
    userGender: '男',
    nickName: '系统管理员',
    userPhone: '13800000001',
    userEmail: 'admin@example.com',
    userRoles: ['R_SUPER'],
    createBy: 'system',
    createTime: '2026-05-31 09:00:00',
    updateBy: 'system',
    updateTime: '2026-05-31 09:00:00'
  },
  {
    id: 2,
    avatar: '/src/assets/images/avatar/avatar2.webp',
    status: '1',
    userName: 'manager',
    userGender: '女',
    nickName: '业务管理员',
    userPhone: '13800000002',
    userEmail: 'manager@example.com',
    userRoles: ['R_ADMIN'],
    createBy: 'admin',
    createTime: '2026-05-31 10:00:00',
    updateBy: 'admin',
    updateTime: '2026-05-31 10:00:00'
  },
  {
    id: 3,
    avatar: '/src/assets/images/avatar/avatar3.webp',
    status: '2',
    userName: 'operator',
    userGender: '男',
    nickName: '运营用户',
    userPhone: '13800000003',
    userEmail: 'operator@example.com',
    userRoles: ['R_USER'],
    createBy: 'admin',
    createTime: '2026-05-31 11:00:00',
    updateBy: 'admin',
    updateTime: '2026-05-31 11:00:00'
  }
]

const roles: RoleListItem[] = [
  {
    roleId: 1,
    roleName: '超级管理员',
    roleCode: 'R_SUPER',
    description: '拥有系统全部管理权限',
    enabled: true,
    createTime: '2026-05-31 09:00:00'
  },
  {
    roleId: 2,
    roleName: '管理员',
    roleCode: 'R_ADMIN',
    description: '拥有常规业务管理权限',
    enabled: true,
    createTime: '2026-05-31 10:00:00'
  },
  {
    roleId: 3,
    roleName: '普通用户',
    roleCode: 'R_USER',
    description: '拥有基础浏览权限',
    enabled: true,
    createTime: '2026-05-31 11:00:00'
  }
]

const articleTypes: ArticleTypeItem[] = [
  { id: 1, name: '产品动态' },
  { id: 2, name: '技术文章' },
  { id: 3, name: '运营公告' }
]

const articleDetail: ArticleDetail = {
  id: 1,
  title: '本地 Mock 文章详情',
  categoryId: 2,
  categoryName: '技术文章',
  blog_class: '2',
  coverUrl: '/src/assets/images/cover/img1.webp',
  summary: '本地 Mock 文章详情摘要',
  contentHtml: `
    <h2>本地 Mock 文章</h2>
    <p>这是一份保存在本地 Vite middleware 中的文章详情数据，用于替代外部 Mock 服务。</p>
  `,
  html_content: `
    <h2>本地 Mock 文章</h2>
    <p>这是一份保存在本地 Vite middleware 中的文章详情数据，用于替代外部 Mock 服务。</p>
    <p>后续对接真实后端时，可将该接口替换为数据库返回内容。</p>
    <pre><code>GET /api/article/detail?id=1</code></pre>
  `,
  visible: true,
  status: 'PUBLISHED',
  viewCount: 12,
  createBy: 'admin',
  createTime: '2026-06-01 10:00:00',
  attachments: []
}

const menus: AppRouteRecord[] = [
  {
    id: 1,
    path: '/dashboard',
    name: 'Dashboard',
    component: '/index/index',
    meta: {
      title: 'menus.dashboard.title',
      icon: 'ri:pie-chart-line',
      roles: ['R_SUPER', 'R_ADMIN']
    },
    children: [
      {
        id: 11,
        path: 'console',
        name: 'Console',
        component: '/dashboard/console',
        meta: {
          title: 'menus.dashboard.console',
          icon: 'ri:home-smile-2-line',
          keepAlive: false,
          fixedTab: true
        }
      },
      {
        id: 12,
        path: 'analysis',
        name: 'Analysis',
        component: '/dashboard/analysis',
        meta: {
          title: 'menus.dashboard.analysis',
          icon: 'ri:align-item-bottom-line',
          keepAlive: false
        }
      }
    ]
  },
  {
    id: 2,
    path: '/system',
    name: 'System',
    component: '/index/index',
    meta: {
      title: 'menus.system.title',
      icon: 'ri:user-3-line',
      roles: ['R_SUPER', 'R_ADMIN']
    },
    children: [
      {
        id: 21,
        path: 'user',
        name: 'User',
        component: '/system/user',
        meta: {
          title: 'menus.system.user',
          icon: 'ri:user-line',
          keepAlive: true,
          roles: ['R_SUPER', 'R_ADMIN'],
          authList: [
            { title: '新增用户', authMark: 'system:user:add' },
            { title: '编辑用户', authMark: 'system:user:edit' },
            { title: '删除用户', authMark: 'system:user:delete' }
          ]
        }
      },
      {
        id: 22,
        path: 'role',
        name: 'Role',
        component: '/system/role',
        meta: {
          title: 'menus.system.role',
          icon: 'ri:user-settings-line',
          keepAlive: true,
          roles: ['R_SUPER'],
          authList: [
            { title: '新增角色', authMark: 'system:role:add' },
            { title: '编辑角色', authMark: 'system:role:edit' },
            { title: '分配角色权限', authMark: 'system:role:permission' }
          ]
        }
      },
      {
        id: 23,
        path: 'menu',
        name: 'Menus',
        component: '/system/menu',
        meta: {
          title: 'menus.system.menu',
          icon: 'ri:menu-line',
          keepAlive: true,
          roles: ['R_SUPER'],
          authList: [{ title: '管理菜单', authMark: 'system:menu:manage' }]
        }
      }
    ]
  },
  {
    id: 3,
    path: '/article',
    name: 'Article',
    component: '/index/index',
    meta: {
      title: 'menus.article.title',
      icon: 'ri:book-2-line',
      roles: ['R_SUPER', 'R_ADMIN']
    },
    children: [
      {
        id: 31,
        path: 'article-list',
        name: 'ArticleList',
        component: '/article/list',
        meta: {
          title: 'menus.article.articleList',
          icon: 'ri:article-line',
          keepAlive: true,
          authList: [
            { title: '新增', authMark: 'article:publish:add' },
            { title: '编辑', authMark: 'article:publish:edit' }
          ]
        }
      },
      {
        id: 32,
        path: 'comment',
        name: 'ArticleComment',
        component: '/article/comment',
        meta: {
          title: 'menus.article.comment',
          icon: 'ri:mail-line',
          keepAlive: true
        }
      },
      {
        id: 33,
        path: 'publish',
        name: 'ArticlePublish',
        component: '/article/publish',
        meta: {
          title: 'menus.article.articlePublish',
          icon: 'ri:telegram-2-line',
          keepAlive: true,
          authList: [{ title: '发布', authMark: 'article:publish:add' }]
        }
      }
    ]
  }
]

const jsonHeaders = {
  'Content-Type': 'application/json; charset=utf-8',
  'Cache-Control': 'no-store'
}

/**
 * 创建 Vite 本地 Mock 插件。
 *
 * 仅在开发环境且 `VITE_USE_LOCAL_MOCK=true` 时启用，生产构建不会注册这些接口。
 */
export function localMockPlugin(enabled: boolean): Plugin {
  return {
    name: 'local-mock-api',
    configureServer(server) {
      if (!enabled) return
      registerMockMiddleware(server)
    }
  }
}

function registerMockMiddleware(server: ViteDevServer): void {
  server.middlewares.use(async (req, res, next) => {
    const method = (req.method || 'GET').toUpperCase()
    const parsedUrl = parseUrl(req.url || '', true)
    const pathname = parsedUrl.pathname || ''

    if (!pathname.startsWith('/api/')) {
      next()
      return
    }

    const body = await readJsonBody(req)
    const mockRequest: MockRequest = {
      method,
      pathname,
      query: normalizeQuery(parsedUrl.query),
      body
    }

    const result = handleMockRequest(mockRequest)
    if (!result) {
      next()
      return
    }

    writeJson(res, result)
  })
}

function handleMockRequest(request: MockRequest): MockResponse | null {
  if (request.method === 'GET' && request.pathname === '/api/auth/captcha') {
    return ok({
      captchaId: `local-${Date.now()}`,
      imageBase64:
        'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTEyIiBoZWlnaHQ9IjQwIiB2aWV3Qm94PSIwIDAgMTEyIDQwIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPjxyZWN0IHdpZHRoPSIxMTIiIGhlaWdodD0iNDAiIGZpbGw9IiNmNWY4ZmMiLz48cGF0aCBkPSJNMCAxMEwxMTIgMzBNMCAzMEwxMTIgMTBNMTAgMEw5MCA0MCIgc3Ryb2tlPSIjOTRjM2U4IiBzdHJva2Utd2lkdGg9IjIiLz48dGV4dCB4PSIyMCIgeT0iMjgiIGZvbnQtc2l6ZT0iMjQiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC13ZWlnaHQ9IjcwMCIgZmlsbD0iIzI1NjNiZCI+QTFCMjwvdGV4dD48L3N2Zz4=',
      expiresIn: 300
    })
  }

  if (request.method === 'POST' && request.pathname === '/api/auth/login') {
    return ok({
      token: TOKEN,
      refreshToken: REFRESH_TOKEN
    })
  }

  if (request.method === 'GET' && request.pathname === '/api/user/info') {
    return ok({
      buttons: [
        'system:user:add',
        'system:user:edit',
        'system:user:delete',
        'system:role:add',
        'system:role:edit',
        'system:role:permission',
        'system:menu:manage',
        'system:org:manage',
        'system:config:manage',
        'article:publish:add',
        'article:publish:edit',
        'article:upload'
      ],
      roles: ['R_SUPER', 'R_ADMIN'],
      userId: 1,
      userName: 'admin',
      email: 'admin@example.com',
      avatar: '/src/assets/images/avatar/avatar.webp'
    })
  }

  if (request.method === 'GET' && request.pathname === '/api/user/list') {
    return ok(paginate(filterUsers(users, request.query), request.query))
  }

  if (request.method === 'GET' && request.pathname === '/api/role/list') {
    return ok(paginate(filterRoles(roles, request.query), request.query))
  }

  if (request.method === 'GET' && request.pathname === '/api/v3/system/menus') {
    return ok(menus)
  }

  if (request.method === 'GET' && request.pathname === '/api/article/types') {
    return ok(articleTypes)
  }

  if (request.method === 'GET' && request.pathname === '/api/article/detail') {
    return ok(articleDetail)
  }

  return null
}

function filterUsers(list: UserListItem[], query: Record<string, string>): UserListItem[] {
  return list.filter((item) => {
    if (query.userName && !item.userName.includes(query.userName)) return false
    if (query.userPhone && !item.userPhone.includes(query.userPhone)) return false
    if (query.userEmail && !item.userEmail.includes(query.userEmail)) return false
    if (query.status && item.status !== query.status) return false
    return true
  })
}

function filterRoles(list: RoleListItem[], query: Record<string, string>): RoleListItem[] {
  return list.filter((item) => {
    if (query.roleName && !item.roleName.includes(query.roleName)) return false
    if (query.roleCode && !item.roleCode.includes(query.roleCode)) return false
    if (query.description && !item.description.includes(query.description)) return false
    if (query.enabled && String(item.enabled) !== query.enabled) return false
    return true
  })
}

function paginate<T>(list: T[], query: Record<string, string>): PageResult<T> {
  const current = toPositiveNumber(query.current, 1)
  const size = toPositiveNumber(query.size, 10)
  const start = (current - 1) * size
  const end = start + size

  return {
    records: list.slice(start, end),
    current,
    size,
    total: list.length
  }
}

function ok<T>(data: T): MockResponse<T> {
  return {
    code: 200,
    msg: 'success',
    data
  }
}

function toPositiveNumber(value: string | undefined, fallback: number): number {
  const numberValue = Number(value)
  return Number.isFinite(numberValue) && numberValue > 0 ? numberValue : fallback
}

function normalizeQuery(query: NodeJS.Dict<string | string[]>): Record<string, string> {
  return Object.entries(query).reduce<Record<string, string>>((result, [key, value]) => {
    if (Array.isArray(value)) {
      result[key] = value[0] || ''
    } else if (typeof value === 'string') {
      result[key] = value
    }
    return result
  }, {})
}

function readJsonBody(req: Connect.IncomingMessage): Promise<JsonRecord> {
  return new Promise((resolve) => {
    const chunks: Buffer[] = []

    req.on('data', (chunk) => {
      chunks.push(Buffer.isBuffer(chunk) ? chunk : Buffer.from(chunk))
    })

    req.on('end', () => {
      if (chunks.length === 0) {
        resolve({})
        return
      }

      try {
        const text = Buffer.concat(chunks).toString('utf-8')
        resolve(JSON.parse(text) as JsonRecord)
      } catch {
        resolve({})
      }
    })

    req.on('error', () => {
      resolve({})
    })
  })
}

function writeJson(res: Connect.ServerResponse, response: MockResponse): void {
  res.statusCode = 200
  Object.entries(jsonHeaders).forEach(([key, value]) => {
    res.setHeader(key, value)
  })
  res.end(JSON.stringify(response))
}
