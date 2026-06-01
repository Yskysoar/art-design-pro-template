package com.template.system.org.service;

import com.template.security.auth.AppUserPrincipal;
import com.template.system.org.dto.OrgSaveRequest;
import com.template.system.org.vo.OrgTreeVo;

import java.util.List;

/**
 * 组织业务服务。
 */
public interface OrgService {

    /**
     * 查询组织树。
     *
     * @return 组织树
     */
    List<OrgTreeVo> getOrgTree();

    /**
     * 新增组织。
     *
     * @param request   组织保存请求
     * @param principal 当前登录用户
     */
    void createOrg(OrgSaveRequest request, AppUserPrincipal principal);

    /**
     * 更新组织。
     *
     * @param id        组织 ID
     * @param request   组织保存请求
     * @param principal 当前登录用户
     */
    void updateOrg(Long id, OrgSaveRequest request, AppUserPrincipal principal);

    /**
     * 删除组织。
     *
     * @param id 组织 ID
     */
    void deleteOrg(Long id);
}
