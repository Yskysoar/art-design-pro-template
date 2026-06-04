package com.template.system.user.vo;

import java.util.List;

/**
 * User-org relation response.
 *
 * @param userId user id
 * @param orgIds org id list
 */
public record UserOrgVo(Long userId, List<Long> orgIds) {
}
