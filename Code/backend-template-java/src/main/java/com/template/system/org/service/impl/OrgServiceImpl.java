package com.template.system.org.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.template.common.exception.BusinessException;
import com.template.common.response.ApiCode;
import com.template.security.auth.AppUserPrincipal;
import com.template.system.org.dto.OrgSaveRequest;
import com.template.system.org.entity.SysOrg;
import com.template.system.org.mapper.SysOrgMapper;
import com.template.system.org.service.OrgService;
import com.template.system.org.vo.OrgTreeVo;
import com.template.system.relation.entity.SysRoleOrg;
import com.template.system.relation.entity.SysUserOrg;
import com.template.system.relation.mapper.SysRoleOrgMapper;
import com.template.system.relation.mapper.SysUserOrgMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 组织业务服务实现。
 */
@Service
public class OrgServiceImpl implements OrgService {

    private static final int ENABLED = 1;
    private static final int NOT_DELETED = 0;
    private static final long ROOT_PARENT_ID = 0L;
    private static final String ROOT_ANCESTORS = "0";
    private static final String DEFAULT_ORG_TYPE = "GROUP";

    private final SysOrgMapper orgMapper;
    private final SysUserOrgMapper userOrgMapper;
    private final SysRoleOrgMapper roleOrgMapper;

    public OrgServiceImpl(
            SysOrgMapper orgMapper,
            SysUserOrgMapper userOrgMapper,
            SysRoleOrgMapper roleOrgMapper
    ) {
        this.orgMapper = orgMapper;
        this.userOrgMapper = userOrgMapper;
        this.roleOrgMapper = roleOrgMapper;
    }

    @Override
    public List<OrgTreeVo> getOrgTree() {
        List<SysOrg> orgs = orgMapper.selectList(new LambdaQueryWrapper<SysOrg>()
                .eq(SysOrg::getDeleted, NOT_DELETED)
                .orderByAsc(SysOrg::getSort)
                .orderByAsc(SysOrg::getId));
        Map<Long, List<SysOrg>> childrenMap = orgs.stream()
                .sorted(Comparator.comparing(SysOrg::getSort, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.groupingBy(SysOrg::getParentId));
        return buildTree(ROOT_PARENT_ID, childrenMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrg(OrgSaveRequest request, AppUserPrincipal principal) {
        SysOrg parent = getParentOrg(request.parentId());
        assertOrgCodeUnique(request.orgCode(), null);

        SysOrg org = new SysOrg();
        applyRequest(org, request, principal.userName());
        org.setAncestors(buildAncestors(parent));
        org.setDeleted(NOT_DELETED);
        orgMapper.insert(org);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrg(Long id, OrgSaveRequest request, AppUserPrincipal principal) {
        SysOrg org = getExistingOrg(id);
        SysOrg parent = getParentOrg(request.parentId());
        assertNotMoveToSelfOrChild(id, request.parentId());
        assertOrgCodeUnique(request.orgCode(), id);

        applyRequest(org, request, principal.userName());
        org.setAncestors(buildAncestors(parent));
        orgMapper.updateById(org);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrg(Long id) {
        SysOrg org = getExistingOrg(id);
        Long childCount = orgMapper.selectCount(new LambdaQueryWrapper<SysOrg>()
                .eq(SysOrg::getParentId, id)
                .eq(SysOrg::getDeleted, NOT_DELETED));
        if (childCount != null && childCount > 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "存在子组织，不能删除");
        }

        Long userCount = userOrgMapper.selectCount(new LambdaQueryWrapper<SysUserOrg>()
                .eq(SysUserOrg::getOrgId, id));
        if (userCount != null && userCount > 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "组织已关联用户，不能删除");
        }

        Long roleCount = roleOrgMapper.selectCount(new LambdaQueryWrapper<SysRoleOrg>()
                .eq(SysRoleOrg::getOrgId, id));
        if (roleCount != null && roleCount > 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "组织已被角色数据权限引用，不能删除");
        }

        org.setDeleted(1);
        org.setEnabled(0);
        orgMapper.updateById(org);
    }

    private List<OrgTreeVo> buildTree(Long parentId, Map<Long, List<SysOrg>> childrenMap) {
        return childrenMap.getOrDefault(parentId, List.of()).stream()
                .map(org -> toVo(org, buildTree(org.getId(), childrenMap)))
                .toList();
    }

    private OrgTreeVo toVo(SysOrg org, List<OrgTreeVo> children) {
        return new OrgTreeVo(
                org.getId(),
                org.getParentId(),
                org.getAncestors(),
                org.getOrgName(),
                org.getOrgCode(),
                org.getOrgType(),
                org.getSort(),
                Integer.valueOf(ENABLED).equals(org.getEnabled()),
                children
        );
    }

    private SysOrg getExistingOrg(Long id) {
        if (id == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "组织ID不能为空");
        }

        SysOrg org = orgMapper.selectOne(new LambdaQueryWrapper<SysOrg>()
                .eq(SysOrg::getId, id)
                .eq(SysOrg::getDeleted, NOT_DELETED));
        if (org == null) {
            throw new BusinessException(ApiCode.NOT_FOUND, "组织不存在");
        }
        return org;
    }

    private SysOrg getParentOrg(Long parentId) {
        Long normalizedParentId = normalizeParentId(parentId);
        if (ROOT_PARENT_ID == normalizedParentId) {
            return null;
        }
        return getExistingOrg(normalizedParentId);
    }

    private void assertOrgCodeUnique(String orgCode, Long excludeOrgId) {
        Long count = orgMapper.selectCount(new LambdaQueryWrapper<SysOrg>()
                .eq(SysOrg::getOrgCode, orgCode)
                .eq(SysOrg::getDeleted, NOT_DELETED)
                .ne(excludeOrgId != null, SysOrg::getId, excludeOrgId));
        if (count != null && count > 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "组织编码已存在");
        }
    }

    private void assertNotMoveToSelfOrChild(Long id, Long parentId) {
        Long cursor = normalizeParentId(parentId);
        while (cursor != null && cursor != ROOT_PARENT_ID) {
            if (id.equals(cursor)) {
                throw new BusinessException(ApiCode.BAD_REQUEST, "父组织不能是当前组织或其子组织");
            }
            SysOrg parent = orgMapper.selectById(cursor);
            cursor = parent == null ? ROOT_PARENT_ID : parent.getParentId();
        }
    }

    private void applyRequest(SysOrg org, OrgSaveRequest request, String operator) {
        org.setParentId(normalizeParentId(request.parentId()));
        org.setOrgName(request.orgName());
        org.setOrgCode(request.orgCode());
        org.setOrgType(StringUtils.hasText(request.orgType()) ? request.orgType() : DEFAULT_ORG_TYPE);
        org.setSort(request.sort() == null ? 0 : request.sort());
        org.setEnabled(Boolean.FALSE.equals(request.enabled()) ? 0 : ENABLED);
        org.setUpdateBy(operator);
        if (!StringUtils.hasText(org.getCreateBy())) {
            org.setCreateBy(operator);
        }
    }

    private Long normalizeParentId(Long parentId) {
        return parentId == null || parentId < 0 ? ROOT_PARENT_ID : parentId;
    }

    private String buildAncestors(SysOrg parent) {
        if (parent == null) {
            return ROOT_ANCESTORS;
        }
        return parent.getAncestors() + "," + parent.getId();
    }
}
