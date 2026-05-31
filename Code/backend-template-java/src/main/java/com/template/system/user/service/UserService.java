package com.template.system.user.service;

import com.template.common.pagination.PageResult;
import com.template.system.user.dto.UserListQuery;
import com.template.system.user.vo.UserListItemVo;

/**
 * 用户业务服务。
 */
public interface UserService {

    /**
     * 分页查询用户列表。
     *
     * @param query 查询参数
     * @return 用户分页数据
     */
    PageResult<UserListItemVo> pageUsers(UserListQuery query);
}
