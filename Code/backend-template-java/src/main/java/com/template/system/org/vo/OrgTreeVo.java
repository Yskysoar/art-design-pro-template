package com.template.system.org.vo;

import java.util.List;

/**
 * 组织树节点。
 *
 * @param id        组织 ID
 * @param parentId  父组织 ID
 * @param ancestors 祖级列表
 * @param orgName   组织名称
 * @param orgCode   组织编码
 * @param orgType   组织类型
 * @param sort      排序
 * @param enabled   是否启用
 * @param children  子组织
 */
public record OrgTreeVo(
        Long id,
        Long parentId,
        String ancestors,
        String orgName,
        String orgCode,
        String orgType,
        Integer sort,
        Boolean enabled,
        List<OrgTreeVo> children
) {
}
