package com.template.system.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.template.common.exception.BusinessException;
import com.template.security.auth.AppUserPrincipal;
import com.template.security.permission.PermissionService;
import com.template.system.auth.service.CaptchaService;
import com.template.system.config.entity.SysConfig;
import com.template.system.config.mapper.SysConfigMapper;
import com.template.system.org.entity.SysOrg;
import com.template.system.org.mapper.SysOrgMapper;
import com.template.system.relation.entity.SysUserOrg;
import com.template.system.relation.entity.SysUserRole;
import com.template.system.relation.mapper.SysUserOrgMapper;
import com.template.system.relation.mapper.SysUserRoleMapper;
import com.template.system.role.entity.SysRole;
import com.template.system.role.mapper.SysRoleMapper;
import com.template.system.user.dto.UserCreateRequest;
import com.template.system.user.dto.UserListQuery;
import com.template.system.user.dto.UserPasswordChangeRequest;
import com.template.system.user.dto.UserUpdateRequest;
import com.template.system.user.entity.SysUser;
import com.template.system.user.mapper.SysUserMapper;
import com.template.system.user.vo.UserListItemVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 用户业务服务单元测试。
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final AppUserPrincipal ADMIN = new AppUserPrincipal(1L, "admin", List.of("R_SUPER"));

    @Mock
    private SysUserMapper userMapper;
    @Mock
    private SysUserRoleMapper userRoleMapper;
    @Mock
    private SysUserOrgMapper userOrgMapper;
    @Mock
    private SysRoleMapper roleMapper;
    @Mock
    private SysOrgMapper orgMapper;
    @Mock
    private SysConfigMapper configMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PermissionService permissionService;
    @Mock
    private CaptchaService captchaService;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(
                userMapper,
                userRoleMapper,
                userOrgMapper,
                roleMapper,
                orgMapper,
                configMapper,
                passwordEncoder,
                permissionService,
                captchaService
        );
    }

    @Test
    @DisplayName("分页查询用户时应返回用户角色和组织 ID")
    void pageUsersShouldReturnRoleCodesAndOrgIds() {
        SysUser user = user(10L, "demo");
        Page<SysUser> page = Page.of(1, 20);
        page.setRecords(List.of(user));
        page.setTotal(1);

        when(userMapper.selectPage(anyPage(), anyWrapper())).thenReturn(page);
        when(userRoleMapper.selectList(anyUserRoleWrapper())).thenReturn(List.of(userRole(10L, 2L)));
        when(roleMapper.selectList(anyRoleWrapper())).thenReturn(List.of(role(2L, "R_USER")));
        when(userOrgMapper.selectList(anyUserOrgWrapper())).thenReturn(List.of(userOrg(10L, 2L, 1), userOrg(10L, 3L, 0)));

        List<UserListItemVo> records = userService.pageUsers(new UserListQuery(1L, 20L, null, null, null, null, null, null)).records();

        assertThat(records).hasSize(1);
        assertThat(records.get(0).userRoles()).containsExactly("R_USER");
        assertThat(records.get(0).orgIds()).containsExactly(2L, 3L);
    }

    @Test
    @DisplayName("新增用户时应写入角色和组织关系")
    void createUserShouldRewriteRolesAndOrgs() {
        when(userMapper.selectCount(anyWrapper())).thenReturn(0L);
        when(roleMapper.selectList(anyRoleWrapper())).thenReturn(List.of(role(2L, "R_USER")));
        when(permissionService.isSuperAdmin(ADMIN)).thenReturn(true);
        when(passwordEncoder.encode("admin123")).thenReturn("hashed");
        when(configMapper.selectList(anyConfigWrapper())).thenReturn(List.of(config("ONE_TO_MANY")));
        when(orgMapper.selectList(anyOrgWrapper())).thenReturn(List.of(org(2L), org(3L)));

        UserCreateRequest request = new UserCreateRequest(
                "demo",
                "admin123",
                "演示用户",
                "男",
                "13800000002",
                "demo@example.com",
                null,
                "1",
                List.of("R_USER"),
                List.of(2L, 3L)
        );

        userService.createUser(request, ADMIN);

        verify(userMapper).insert(any(SysUser.class));
        verify(userRoleMapper).insert(any(SysUserRole.class));
        ArgumentCaptor<SysUserOrg> orgCaptor = ArgumentCaptor.forClass(SysUserOrg.class);
        verify(userOrgMapper, org.mockito.Mockito.times(2)).insert(orgCaptor.capture());
        assertThat(orgCaptor.getAllValues()).extracting(SysUserOrg::getOrgId).containsExactly(2L, 3L);
        assertThat(orgCaptor.getAllValues()).extracting(SysUserOrg::getPrimaryOrg).containsExactly(1, 0);
    }

    @Test
    @DisplayName("一对一组织模式下保存多个组织应拒绝")
    void updateUserShouldRejectMultipleOrgsWhenModeIsOneToOne() {
        when(userMapper.selectOne(anyWrapper())).thenReturn(user(10L, "demo"));
        when(userMapper.selectCount(anyWrapper())).thenReturn(0L);
        when(roleMapper.selectList(anyRoleWrapper())).thenReturn(List.of(role(2L, "R_USER")));
        when(permissionService.isSuperAdmin(ADMIN)).thenReturn(true);
        when(configMapper.selectList(anyConfigWrapper())).thenReturn(List.of(config("ONE_TO_ONE")));

        UserUpdateRequest request = new UserUpdateRequest(
                "demo",
                "演示用户",
                "男",
                "13800000002",
                "demo@example.com",
                null,
                List.of("R_USER"),
                List.of(2L, 3L)
        );

        assertThatThrownBy(() -> userService.updateUser(10L, request, ADMIN))
                .isInstanceOf(BusinessException.class)
                .hasMessage("当前系统仅允许一个用户关联一个组织");

        verify(userOrgMapper, never()).insert(any(SysUserOrg.class));
    }

    @Test
    @DisplayName("配置值非法时应拒绝保存用户组织关系")
    void saveUserOrgsShouldRejectUnsupportedConfigValue() {
        when(userMapper.selectOne(anyWrapper())).thenReturn(user(10L, "demo"));
        when(configMapper.selectList(anyConfigWrapper())).thenReturn(List.of(config("INVALID")));

        assertThatThrownBy(() -> userService.saveUserOrgs(10L, List.of(2L), ADMIN))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户组织关系配置值不支持：INVALID");

        verify(userOrgMapper, never()).insert(any(SysUserOrg.class));
    }

    @Test
    @DisplayName("保存不存在的组织 ID 应拒绝")
    void saveUserOrgsShouldRejectMissingOrg() {
        when(userMapper.selectOne(anyWrapper())).thenReturn(user(10L, "demo"));
        when(configMapper.selectList(anyConfigWrapper())).thenReturn(List.of(config("ONE_TO_MANY")));
        when(orgMapper.selectList(anyOrgWrapper())).thenReturn(List.of(org(2L)));

        assertThatThrownBy(() -> userService.saveUserOrgs(10L, List.of(2L, 404L), ADMIN))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("组织不存在");

        verify(userOrgMapper, never()).insert(any(SysUserOrg.class));
    }

    @Test
    @DisplayName("修改当前用户密码时旧密码错误应拒绝")
    void changeCurrentUserPasswordShouldRejectWrongOldPassword() {
        SysUser user = user(1L, "admin");
        user.setPasswordHash("old-hash");
        when(userMapper.selectOne(anyWrapper())).thenReturn(user);
        when(passwordEncoder.matches("wrong-pass", "old-hash")).thenReturn(false);

        assertThatThrownBy(() -> userService.changeCurrentUserPassword(
                new UserPasswordChangeRequest("wrong-pass", "new-pass-123", "captcha-id", "a1B2"),
                ADMIN
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("当前密码不正确");

        verify(userMapper, never()).updateById(any(SysUser.class));
    }

    @Test
    @DisplayName("修改当前用户密码时应校验旧密码并写入新哈希")
    void changeCurrentUserPasswordShouldUpdatePasswordHash() {
        SysUser user = user(1L, "admin");
        user.setPasswordHash("old-hash");
        when(userMapper.selectOne(anyWrapper())).thenReturn(user);
        when(passwordEncoder.matches("old-pass-123", "old-hash")).thenReturn(true);
        when(passwordEncoder.matches("new-pass-123", "old-hash")).thenReturn(false);
        when(passwordEncoder.encode("new-pass-123")).thenReturn("new-hash");

        userService.changeCurrentUserPassword(
                new UserPasswordChangeRequest("old-pass-123", "new-pass-123", "captcha-id", "a1B2"),
                ADMIN
        );

        ArgumentCaptor<SysUser> userCaptor = ArgumentCaptor.forClass(SysUser.class);
        verify(userMapper).updateById(userCaptor.capture());
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("new-hash");
        assertThat(userCaptor.getValue().getUpdateBy()).isEqualTo("admin");
    }

    private SysUser user(Long id, String userName) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUserName(userName);
        user.setStatus("NORMAL");
        user.setDeleted(0);
        return user;
    }

    private SysRole role(Long id, String code) {
        SysRole role = new SysRole();
        role.setId(id);
        role.setRoleCode(code);
        role.setRoleName(code);
        role.setRoleLevel(10);
        role.setEnabled(1);
        role.setDeleted(0);
        return role;
    }

    private SysOrg org(Long id) {
        SysOrg org = new SysOrg();
        org.setId(id);
        org.setDeleted(0);
        return org;
    }

    private SysConfig config(String value) {
        SysConfig config = new SysConfig();
        config.setConfigKey("user_org_relation_mode");
        config.setConfigValue(value);
        config.setDeleted(0);
        return config;
    }

    private SysUserRole userRole(Long userId, Long roleId) {
        SysUserRole relation = new SysUserRole();
        relation.setUserId(userId);
        relation.setRoleId(roleId);
        return relation;
    }

    private SysUserOrg userOrg(Long userId, Long orgId, Integer primaryOrg) {
        SysUserOrg relation = new SysUserOrg();
        relation.setUserId(userId);
        relation.setOrgId(orgId);
        relation.setPrimaryOrg(primaryOrg);
        return relation;
    }

    @SuppressWarnings("unchecked")
    private Page<SysUser> anyPage() {
        return any(Page.class);
    }

    @SuppressWarnings("unchecked")
    private Wrapper<SysUser> anyWrapper() {
        return any(Wrapper.class);
    }

    @SuppressWarnings("unchecked")
    private Wrapper<SysUserRole> anyUserRoleWrapper() {
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
    private Wrapper<SysConfig> anyConfigWrapper() {
        return any(Wrapper.class);
    }

    @SuppressWarnings("unchecked")
    private Wrapper<SysOrg> anyOrgWrapper() {
        return any(Wrapper.class);
    }
}
