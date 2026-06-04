package com.template.system.user.dto;

/**
 * 用户列表查询参数。
 *
 * @param current    当前页码，从 1 开始
 * @param size       每页条数
 * @param id         用户 ID
 * @param userName   用户名，支持模糊查询
 * @param userGender 性别
 * @param userPhone  手机号，支持模糊查询
 * @param userEmail  邮箱，支持模糊查询
 * @param status     前端用户状态编码
 */
public record UserListQuery(
        Long current,
        Long size,
        Long id,
        String userName,
        String userGender,
        String userPhone,
        String userEmail,
        String status
) {
}
