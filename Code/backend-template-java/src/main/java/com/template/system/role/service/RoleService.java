package com.template.system.role.service;

import com.template.common.pagination.PageResult;
import com.template.system.role.dto.RoleListQuery;
import com.template.system.role.vo.RoleListItemVo;

/**
 * 角色业务服务。
 */
public interface RoleService {

    /**
     * 分页查询角色列表。
     *
     * @param query 查询参数
     * @return 角色分页数据
     */
    PageResult<RoleListItemVo> pageRoles(RoleListQuery query);
}
