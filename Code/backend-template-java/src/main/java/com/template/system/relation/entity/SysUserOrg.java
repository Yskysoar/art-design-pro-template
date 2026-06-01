package com.template.system.relation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 用户组织关联实体。
 */
@TableName("sys_user_org")
public class SysUserOrg {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long orgId;
    private Integer primaryOrg;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public Integer getPrimaryOrg() {
        return primaryOrg;
    }

    public void setPrimaryOrg(Integer primaryOrg) {
        this.primaryOrg = primaryOrg;
    }
}
