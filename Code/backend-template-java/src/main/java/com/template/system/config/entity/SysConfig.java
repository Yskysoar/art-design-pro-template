package com.template.system.config.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 系统配置项实体。
 *
 * <p>用于保存用户组织关系模式、角色层级开关、匿名访问策略等后台可配置基础参数。</p>
 */
@TableName("sys_config")
public class SysConfig {

    /** 配置项主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 配置键，要求全局唯一，例如 user_org_relation_mode。 */
    private String configKey;

    /** 配置值，按业务约定保存字符串、布尔值或枚举值。 */
    private String configValue;

    /** 配置说明，用于后台列表展示该配置项的业务含义。 */
    private String description;

    /** 是否允许后台编辑，1 表示可编辑，0 表示只读保护。 */
    private Integer editable;

    /** 创建人账号。 */
    private String createBy;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 最后更新人账号。 */
    private String updateBy;

    /** 最后更新时间。 */
    private LocalDateTime updateTime;

    /** 逻辑删除标记，0 表示正常，1 表示已删除。 */
    private Integer deleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getEditable() {
        return editable;
    }

    public void setEditable(Integer editable) {
        this.editable = editable;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}
