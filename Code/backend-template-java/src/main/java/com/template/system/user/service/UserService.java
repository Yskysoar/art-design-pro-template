package com.template.system.user.service;

import com.template.common.pagination.PageResult;
import com.template.security.auth.AppUserPrincipal;
import com.template.system.user.dto.UserCreateRequest;
import com.template.system.user.dto.UserListQuery;
import com.template.system.user.dto.UserPasswordChangeRequest;
import com.template.system.user.dto.UserStatusRequest;
import com.template.system.user.dto.UserUpdateRequest;
import com.template.system.user.vo.UserListItemVo;
import com.template.system.user.vo.UserOrgVo;

import java.util.List;

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

    /**
     * 新增用户。
     *
     * @param request   新增用户请求
     * @param principal 当前登录用户
     */
    void createUser(UserCreateRequest request, AppUserPrincipal principal);

    /**
     * 更新用户基础资料和角色。
     *
     * @param id        用户 ID
     * @param request   更新请求
     * @param principal 当前登录用户
     */
    void updateUser(Long id, UserUpdateRequest request, AppUserPrincipal principal);

    /**
     * 修改用户状态。
     *
     * @param id        用户 ID
     * @param request   状态请求
     * @param principal 当前登录用户
     */
    void updateStatus(Long id, UserStatusRequest request, AppUserPrincipal principal);

    /**
     * 逻辑删除用户。
     *
     * @param id        用户 ID
     * @param principal 当前登录用户
     */
    void deleteUser(Long id, AppUserPrincipal principal);

    /**
     * 查询用户组织关系。
     *
     * @param userId 用户 ID
     * @return 用户组织集合
     */
    UserOrgVo getUserOrgs(Long userId);

    /**
     * 保存用户组织关系。
     *
     * @param userId    用户 ID
     * @param orgIds    组织 ID 集合
     * @param principal 当前登录用户
     */
    void saveUserOrgs(Long userId, List<Long> orgIds, AppUserPrincipal principal);

    /**
     * 修改当前登录用户密码。
     *
     * @param request   修改密码请求
     * @param principal 当前登录用户
     */
    void changeCurrentUserPassword(UserPasswordChangeRequest request, AppUserPrincipal principal);
}
