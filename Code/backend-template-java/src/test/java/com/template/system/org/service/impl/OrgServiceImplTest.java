package com.template.system.org.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.template.common.exception.BusinessException;
import com.template.security.auth.AppUserPrincipal;
import com.template.security.permission.PermissionService;
import com.template.system.org.entity.SysOrg;
import com.template.system.org.mapper.SysOrgMapper;
import com.template.system.org.vo.OrgTreeVo;
import com.template.system.relation.entity.SysRoleOrg;
import com.template.system.relation.entity.SysUserOrg;
import com.template.system.relation.mapper.SysRoleOrgMapper;
import com.template.system.relation.mapper.SysUserOrgMapper;
import com.template.system.role.entity.SysRole;
import com.template.system.role.mapper.SysRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 组织业务服务单元测试。
 */
@ExtendWith(MockitoExtension.class)
class OrgServiceImplTest {

    private static final AppUserPrincipal SUPER = new AppUserPrincipal(1L, "admin", List.of("R_SUPER"));
    private static final AppUserPrincipal MANAGER = new AppUserPrincipal(2L, "manager", List.of("R_MANAGER"));

    @Mock
    private SysOrgMapper orgMapper;
    @Mock
    private SysUserOrgMapper userOrgMapper;
    @Mock
    private SysRoleOrgMapper roleOrgMapper;
    @Mock
    private SysRoleMapper roleMapper;
    @Mock
    private PermissionService permissionService;

    private OrgServiceImpl orgService;

    @BeforeEach
    void setUp() {
        orgService = new OrgServiceImpl(orgMapper, userOrgMapper, roleOrgMapper, roleMapper, permissionService);
    }

    @Test
    @DisplayName("超级管理员查询组织树应返回全量组织")
    void getOrgTreeShouldReturnAllOrgsForSuperAdmin() {
        when(orgMapper.selectList(anyOrgWrapper())).thenReturn(orgs());
        when(permissionService.isSuperAdmin(SUPER)).thenReturn(true);

        List<OrgTreeVo> tree = orgService.getOrgTree(SUPER);

        assertThat(tree).hasSize(1);
        assertThat(flatNames(tree)).containsExactly("总部", "研发部", "前端组", "运营部");
        verify(roleMapper, never()).selectList(anyRoleWrapper());
    }

    @Test
    @DisplayName("CURRENT_ORG_AND_SUB 应返回当前用户组织及其下级")
    void getOrgTreeShouldReturnCurrentOrgAndChildren() {
        when(orgMapper.selectList(anyOrgWrapper())).thenReturn(orgs());
        when(permissionService.isSuperAdmin(MANAGER)).thenReturn(false);
        when(roleMapper.selectList(anyRoleWrapper())).thenReturn(List.of(role(2L, "CURRENT_ORG_AND_SUB")));
        when(userOrgMapper.selectList(anyUserOrgWrapper())).thenReturn(List.of(userOrg(2L, 2L)));

        List<OrgTreeVo> tree = orgService.getOrgTree(MANAGER);

        assertThat(flatNames(tree)).containsExactly("总部", "研发部", "前端组");
    }

    @Test
    @DisplayName("CURRENT_ORG 应只返回当前用户直接关联组织")
    void getOrgTreeShouldReturnCurrentOrgOnly() {
        when(orgMapper.selectList(anyOrgWrapper())).thenReturn(orgs());
        when(permissionService.isSuperAdmin(MANAGER)).thenReturn(false);
        when(roleMapper.selectList(anyRoleWrapper())).thenReturn(List.of(role(2L, "CURRENT_ORG")));
        when(userOrgMapper.selectList(anyUserOrgWrapper())).thenReturn(List.of(userOrg(2L, 2L)));

        List<OrgTreeVo> tree = orgService.getOrgTree(MANAGER);

        assertThat(flatNames(tree)).containsExactly("总部", "研发部");
    }

    @Test
    @DisplayName("SELF 在组织树场景应按当前用户直接关联组织处理")
    void getOrgTreeShouldTreatSelfAsCurrentOrg() {
        when(orgMapper.selectList(anyOrgWrapper())).thenReturn(orgs());
        when(permissionService.isSuperAdmin(MANAGER)).thenReturn(false);
        when(roleMapper.selectList(anyRoleWrapper())).thenReturn(List.of(role(2L, "SELF")));
        when(userOrgMapper.selectList(anyUserOrgWrapper())).thenReturn(List.of(userOrg(2L, 2L)));

        List<OrgTreeVo> tree = orgService.getOrgTree(MANAGER);

        assertThat(flatNames(tree)).containsExactly("总部", "研发部");
    }

    @Test
    @DisplayName("CUSTOM_ORG 应返回角色自定义组织及其下级")
    void getOrgTreeShouldReturnCustomOrgsAndChildren() {
        when(orgMapper.selectList(anyOrgWrapper())).thenReturn(orgs());
        when(permissionService.isSuperAdmin(MANAGER)).thenReturn(false);
        when(roleMapper.selectList(anyRoleWrapper())).thenReturn(List.of(role(3L, "CUSTOM_ORG")));
        when(userOrgMapper.selectList(anyUserOrgWrapper())).thenReturn(List.of());
        when(roleOrgMapper.selectList(anyRoleOrgWrapper())).thenReturn(List.of(roleOrg(3L, 4L)));

        List<OrgTreeVo> tree = orgService.getOrgTree(MANAGER);

        assertThat(flatNames(tree)).containsExactly("总部", "运营部");
    }

    @Test
    @DisplayName("普通用户没有组织范围时应返回空组织树")
    void getOrgTreeShouldReturnEmptyWhenNoVisibleOrg() {
        when(orgMapper.selectList(anyOrgWrapper())).thenReturn(orgs());
        when(permissionService.isSuperAdmin(MANAGER)).thenReturn(false);
        when(roleMapper.selectList(anyRoleWrapper())).thenReturn(List.of(role(2L, "CURRENT_ORG")));
        when(userOrgMapper.selectList(anyUserOrgWrapper())).thenReturn(List.of());

        List<OrgTreeVo> tree = orgService.getOrgTree(MANAGER);

        assertThat(tree).isEmpty();
    }

    @Test
    @DisplayName("角色数据权限范围非法时应返回业务错误")
    void getOrgTreeShouldRejectUnsupportedDataScope() {
        when(orgMapper.selectList(anyOrgWrapper())).thenReturn(orgs());
        when(permissionService.isSuperAdmin(MANAGER)).thenReturn(false);
        when(roleMapper.selectList(anyRoleWrapper())).thenReturn(List.of(role(2L, "INVALID_SCOPE")));

        assertThatThrownBy(() -> orgService.getOrgTree(MANAGER))
                .isInstanceOf(BusinessException.class)
                .hasMessage("数据权限范围不支持：INVALID_SCOPE");
    }

    private List<String> flatNames(List<OrgTreeVo> tree) {
        return tree.stream()
                .flatMap(node -> java.util.stream.Stream.concat(
                        java.util.stream.Stream.of(node.orgName()),
                        flatNames(node.children()).stream()
                ))
                .toList();
    }

    private List<SysOrg> orgs() {
        return List.of(
                org(1L, 0L, "0", "总部", 1),
                org(2L, 1L, "0,1", "研发部", 1),
                org(3L, 2L, "0,1,2", "前端组", 1),
                org(4L, 1L, "0,1", "运营部", 2)
        );
    }

    private SysOrg org(Long id, Long parentId, String ancestors, String orgName, Integer sort) {
        SysOrg org = new SysOrg();
        org.setId(id);
        org.setParentId(parentId);
        org.setAncestors(ancestors);
        org.setOrgName(orgName);
        org.setOrgCode("ORG_" + id);
        org.setOrgType("DEPT");
        org.setSort(sort);
        org.setEnabled(1);
        org.setDeleted(0);
        return org;
    }

    private SysRole role(Long id, String dataScope) {
        SysRole role = new SysRole();
        role.setId(id);
        role.setRoleCode("R_" + id);
        role.setDataScope(dataScope);
        role.setEnabled(1);
        role.setDeleted(0);
        return role;
    }

    private SysUserOrg userOrg(Long userId, Long orgId) {
        SysUserOrg userOrg = new SysUserOrg();
        userOrg.setUserId(userId);
        userOrg.setOrgId(orgId);
        return userOrg;
    }

    private SysRoleOrg roleOrg(Long roleId, Long orgId) {
        SysRoleOrg roleOrg = new SysRoleOrg();
        roleOrg.setRoleId(roleId);
        roleOrg.setOrgId(orgId);
        return roleOrg;
    }

    @SuppressWarnings("unchecked")
    private Wrapper<SysOrg> anyOrgWrapper() {
        return any(Wrapper.class);
    }

    @SuppressWarnings("unchecked")
    private Wrapper<SysRole> anyRoleWrapper() {
        return any(Wrapper.class);
    }

    @SuppressWarnings("unchecked")
    private Wrapper<SysUserOrg> anyUserOrgWrapper() {
        return any(Wrapper.class);
    }

    @SuppressWarnings("unchecked")
    private Wrapper<SysRoleOrg> anyRoleOrgWrapper() {
        return any(Wrapper.class);
    }
}
