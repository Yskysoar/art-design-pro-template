import request from '@/utils/http'
import { AppRouteRecord } from '@/types/router'

// 获取用户列表
export function fetchGetUserList(params: Api.SystemManage.UserSearchParams) {
  return request.get<Api.SystemManage.UserList>({
    url: '/api/user/list',
    params
  })
}

// 新增用户
export function fetchCreateUser(data: Api.SystemManage.UserSaveParams) {
  return request.post<void>({
    url: '/api/user',
    data
  })
}

// 更新用户
export function fetchUpdateUser(id: number, data: Api.SystemManage.UserSaveParams) {
  return request.put<void>({
    url: `/api/user/${id}`,
    data
  })
}

// 删除用户
export function fetchDeleteUser(id: number) {
  return request.del<void>({
    url: `/api/user/${id}`
  })
}

// 获取角色列表
export function fetchGetRoleList(params: Api.SystemManage.RoleSearchParams) {
  return request.get<Api.SystemManage.RoleList>({
    url: '/api/role/list',
    params
  })
}

// 新增角色
export function fetchCreateRole(data: Api.SystemManage.RoleSaveParams) {
  return request.post<void>({
    url: '/api/role',
    data
  })
}

// 更新角色
export function fetchUpdateRole(id: number, data: Api.SystemManage.RoleSaveParams) {
  return request.put<void>({
    url: `/api/role/${id}`,
    data
  })
}

// 删除角色
export function fetchDeleteRole(id: number) {
  return request.del<void>({
    url: `/api/role/${id}`
  })
}

// 查询角色菜单权限
export function fetchGetRolePermissions(id: number) {
  return request.get<Api.SystemManage.RolePermission>({
    url: `/api/role/${id}/permissions`
  })
}

// 保存角色菜单权限
export function fetchSaveRolePermissions(id: number, data: Api.SystemManage.RolePermissionSaveParams) {
  return request.put<void>({
    url: `/api/role/${id}/permissions`,
    data
  })
}

// 查询角色数据权限
export function fetchGetRoleDataScope(id: number) {
  return request.get<Api.SystemManage.RoleDataScope>({
    url: `/api/role/${id}/data-scope`
  })
}

// 保存角色数据权限
export function fetchSaveRoleDataScope(id: number, data: Api.SystemManage.RoleDataScopeSaveParams) {
  return request.put<void>({
    url: `/api/role/${id}/data-scope`,
    data
  })
}

// 获取菜单列表
export function fetchGetMenuList() {
  return request.get<AppRouteRecord[]>({
    url: '/api/v3/system/menus'
  })
}

// 获取菜单管理树
export function fetchGetManageMenuList() {
  return request.get<AppRouteRecord[]>({
    url: '/api/v3/system/menus/manage'
  })
}

// 新增菜单
export function fetchCreateMenu(data: Api.SystemManage.MenuSaveParams) {
  return request.post<void>({
    url: '/api/v3/system/menus',
    data
  })
}

// 更新菜单
export function fetchUpdateMenu(id: number, data: Api.SystemManage.MenuSaveParams) {
  return request.put<void>({
    url: `/api/v3/system/menus/${id}`,
    data
  })
}

// 删除菜单
export function fetchDeleteMenu(id: number) {
  return request.del<void>({
    url: `/api/v3/system/menus/${id}`
  })
}

// 获取配置项列表
export function fetchGetConfigList(params: Api.SystemManage.ConfigSearchParams) {
  return request.get<Api.SystemManage.ConfigList>({
    url: '/api/config/list',
    params
  })
}

// 新增配置项
export function fetchCreateConfig(data: Api.SystemManage.ConfigSaveParams) {
  return request.post<void>({
    url: '/api/config',
    data
  })
}

// 更新配置项
export function fetchUpdateConfig(id: number, data: Api.SystemManage.ConfigSaveParams) {
  return request.put<void>({
    url: `/api/config/${id}`,
    data
  })
}

// 删除配置项
export function fetchDeleteConfig(id: number) {
  return request.del<void>({
    url: `/api/config/${id}`
  })
}

// 获取组织树
export function fetchGetOrgTree() {
  return request.get<Api.SystemManage.OrgTreeItem[]>({
    url: '/api/org/tree'
  })
}

// 新增组织
export function fetchCreateOrg(data: Api.SystemManage.OrgSaveParams) {
  return request.post<void>({
    url: '/api/org',
    data
  })
}

// 更新组织
export function fetchUpdateOrg(id: number, data: Api.SystemManage.OrgSaveParams) {
  return request.put<void>({
    url: `/api/org/${id}`,
    data
  })
}

// 删除组织
export function fetchDeleteOrg(id: number) {
  return request.del<void>({
    url: `/api/org/${id}`
  })
}
