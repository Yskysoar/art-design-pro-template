package com.template.system.config.service;

import com.template.common.pagination.PageResult;
import com.template.security.auth.AppUserPrincipal;
import com.template.system.config.dto.ConfigListQuery;
import com.template.system.config.dto.ConfigSaveRequest;
import com.template.system.config.vo.ConfigItemVo;

/**
 * 系统配置项业务服务。
 */
public interface ConfigService {

    /**
     * 分页查询配置项。
     *
     * @param query 查询参数
     * @return 配置项分页数据
     */
    PageResult<ConfigItemVo> pageConfigs(ConfigListQuery query);

    /**
     * 新增配置项。
     *
     * @param request   配置项保存请求
     * @param principal 当前登录用户
     */
    void createConfig(ConfigSaveRequest request, AppUserPrincipal principal);

    /**
     * 更新配置项。
     *
     * @param id        配置 ID
     * @param request   配置项保存请求
     * @param principal 当前登录用户
     */
    void updateConfig(Long id, ConfigSaveRequest request, AppUserPrincipal principal);

    /**
     * 删除配置项。
     *
     * @param id 配置 ID
     */
    void deleteConfig(Long id);
}
