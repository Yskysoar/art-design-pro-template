package com.template.system.user.vo;

import java.util.List;

/**
 * 用户列表行数据。
 *
 * @param id         用户 ID
 * @param avatar     头像地址
 * @param status     前端用户状态编码：1在线，2离线，3异常，4注销
 * @param userName   用户名
 * @param userGender 性别
 * @param nickName   昵称
 * @param userPhone  手机号
 * @param userEmail  邮箱
 * @param userRoles  用户角色编码集合
 * @param createBy   创建人
 * @param createTime 创建时间
 * @param updateBy   更新人
 * @param updateTime 更新时间
 */
public record UserListItemVo(
        Long id,
        String avatar,
        String status,
        String userName,
        String userGender,
        String nickName,
        String userPhone,
        String userEmail,
        List<String> userRoles,
        List<Long> orgIds,
        String createBy,
        String createTime,
        String updateBy,
        String updateTime
) {
}
