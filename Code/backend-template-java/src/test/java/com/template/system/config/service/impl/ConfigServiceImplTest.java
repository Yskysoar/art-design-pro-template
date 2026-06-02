package com.template.system.config.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.template.common.exception.BusinessException;
import com.template.common.response.ApiCode;
import com.template.common.pagination.PageResult;
import com.template.common.security.SensitiveWordGuard;
import com.template.security.auth.AppUserPrincipal;
import com.template.security.permission.PermissionService;
import com.template.system.config.dto.ConfigListQuery;
import com.template.system.config.dto.ConfigSaveRequest;
import com.template.system.config.entity.SysConfig;
import com.template.system.config.mapper.SysConfigMapper;
import com.template.system.config.vo.ConfigItemVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 配置项业务服务单元测试。
 */
@ExtendWith(MockitoExtension.class)
class ConfigServiceImplTest {

    private static final AppUserPrincipal ADMIN =
            new AppUserPrincipal(1L, "admin", List.of("R_SUPER"));

    @Mock
    private SysConfigMapper configMapper;
    @Mock
    private PermissionService permissionService;
    @Mock
    private SensitiveWordGuard sensitiveWordGuard;

    private ConfigServiceImpl configService;

    @BeforeEach
    void setUp() {
        configService = new ConfigServiceImpl(configMapper, permissionService, sensitiveWordGuard);
    }

    @Test
    @DisplayName("分页查询配置项时应修正非法分页参数并转换响应字段")
    void pageConfigsShouldNormalizePageAndMapResult() {
        SysConfig config = editableConfig(1L, "role_level_enabled", "true");
        config.setDescription("启用角色层级管理");
        config.setCreateTime(LocalDateTime.of(2026, 6, 1, 9, 0));
        config.setUpdateTime(LocalDateTime.of(2026, 6, 1, 10, 30));

        Page<SysConfig> page = Page.of(1, 100);
        page.setRecords(List.of(config));
        page.setTotal(1);
        when(configMapper.selectPage(anyPage(), anyWrapper())).thenReturn(page);

        PageResult<ConfigItemVo> result = configService.pageConfigs(
                new ConfigListQuery(0L, 999L, "role", "层级", true)
        );

        assertThat(result.current()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(100);
        assertThat(result.total()).isEqualTo(1);
        assertThat(result.records()).hasSize(1);
        assertThat(result.records().get(0).configKey()).isEqualTo("role_level_enabled");
        assertThat(result.records().get(0).editable()).isTrue();
        assertThat(result.records().get(0).updateTime()).isEqualTo("2026-06-01 10:30:00");
    }

    @Test
    @DisplayName("新增配置项时应校验配置键唯一并写入审计字段")
    void createConfigShouldInsertWhenKeyIsUnique() {
        when(configMapper.selectCount(anyWrapper())).thenReturn(0L);

        configService.createConfig(
                new ConfigSaveRequest("anonymous_portal_access", "true", "允许匿名访问前台公开内容", true),
                ADMIN
        );

        ArgumentCaptor<SysConfig> captor = ArgumentCaptor.forClass(SysConfig.class);
        verify(configMapper).insert(captor.capture());

        SysConfig inserted = captor.getValue();
        assertThat(inserted.getConfigKey()).isEqualTo("anonymous_portal_access");
        assertThat(inserted.getConfigValue()).isEqualTo("true");
        assertThat(inserted.getEditable()).isEqualTo(1);
        assertThat(inserted.getDeleted()).isEqualTo(0L);
        assertThat(inserted.getCreateBy()).isEqualTo("admin");
        assertThat(inserted.getUpdateBy()).isEqualTo("admin");
    }

    @Test
    @DisplayName("新增配置项时配置键重复应抛出业务异常")
    void createConfigShouldRejectDuplicateKey() {
        when(configMapper.selectCount(anyWrapper())).thenReturn(1L);

        assertThatThrownBy(() -> configService.createConfig(
                new ConfigSaveRequest("role_level_enabled", "true", "启用角色层级管理", true),
                ADMIN
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("配置键已存在");

        verify(configMapper, never()).insert(any(SysConfig.class));
    }

    @Test
    @DisplayName("删除后的同名配置键不应阻止重新新增配置项")
    void createConfigShouldAllowSameKeyAfterDeletedConfig() {
        when(configMapper.selectCount(anyWrapper())).thenReturn(0L);

        configService.createConfig(
                new ConfigSaveRequest("temporary_feature", "true", "临时功能开关", true),
                ADMIN
        );

        ArgumentCaptor<SysConfig> captor = ArgumentCaptor.forClass(SysConfig.class);
        verify(configMapper).insert(captor.capture());
        assertThat(captor.getValue().getConfigKey()).isEqualTo("temporary_feature");
        assertThat(captor.getValue().getDeleted()).isEqualTo(0L);
    }

    @Test
    @DisplayName("新增内置配置项时应校验配置值枚举")
    void createConfigShouldRejectUnsupportedBuiltInValue() {
        when(configMapper.selectCount(anyWrapper())).thenReturn(0L);

        assertThatThrownBy(() -> configService.createConfig(
                new ConfigSaveRequest("user_org_relation_mode", "INVALID", "用户组织关系模式", true),
                ADMIN
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("配置值不支持：INVALID");

        verify(configMapper, never()).insert(any(SysConfig.class));
    }

    @Test
    @DisplayName("更新可编辑配置项时应保留主键并更新业务字段")
    void updateConfigShouldUpdateEditableConfig() {
        SysConfig existing = editableConfig(2L, "guest_admin_access", "false");
        when(configMapper.selectOne(anyWrapper())).thenReturn(existing);
        when(configMapper.selectCount(anyWrapper())).thenReturn(0L);

        configService.updateConfig(
                2L,
                new ConfigSaveRequest("guest_admin_access", "false", "禁止游客访问后台", true),
                ADMIN
        );

        ArgumentCaptor<SysConfig> captor = ArgumentCaptor.forClass(SysConfig.class);
        verify(configMapper).updateById(captor.capture());

        SysConfig updated = captor.getValue();
        assertThat(updated.getId()).isEqualTo(2L);
        assertThat(updated.getConfigKey()).isEqualTo("guest_admin_access");
        assertThat(updated.getDescription()).isEqualTo("禁止游客访问后台");
        assertThat(updated.getUpdateBy()).isEqualTo("admin");
    }

    @Test
    @DisplayName("更新只读配置项时应拒绝修改")
    void updateConfigShouldRejectReadonlyConfig() {
        SysConfig existing = editableConfig(3L, "system_locked", "true");
        existing.setEditable(0);
        when(configMapper.selectOne(anyWrapper())).thenReturn(existing);

        assertThatThrownBy(() -> configService.updateConfig(
                3L,
                new ConfigSaveRequest("system_locked", "false", "系统锁定状态", false),
                ADMIN
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("该配置项不可编辑");

        verify(configMapper, never()).updateById(any(SysConfig.class));
    }

    @Test
    @DisplayName("更新配置项时不允许修改配置键")
    void updateConfigShouldRejectChangedConfigKey() {
        SysConfig existing = editableConfig(3L, "guest_admin_access", "false");
        when(configMapper.selectOne(anyWrapper())).thenReturn(existing);

        assertThatThrownBy(() -> configService.updateConfig(
                3L,
                new ConfigSaveRequest("anonymous_portal_access", "false", "禁止游客访问后台", true),
                ADMIN
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("配置键不允许修改");

        verify(configMapper, never()).updateById(any(SysConfig.class));
    }

    @Test
    @DisplayName("文章评论隐藏配置只能由超级管理员修改")
    void updateConfigShouldRequireSuperAdminForCommentHideSwitch() {
        AppUserPrincipal manager = new AppUserPrincipal(2L, "manager", List.of("R_MODERATOR"));
        SysConfig existing = editableConfig(5L, "article_comment_hide_enabled", "true");
        when(configMapper.selectOne(anyWrapper())).thenReturn(existing);
        when(configMapper.selectCount(anyWrapper())).thenReturn(0L);
        doThrow(new BusinessException(ApiCode.FORBIDDEN, "仅超级管理员可操作"))
                .when(permissionService)
                .requireSuperAdmin(manager);

        assertThatThrownBy(() -> configService.updateConfig(
                5L,
                new ConfigSaveRequest("article_comment_hide_enabled", "false", "是否开启文章评论隐藏和恢复功能", true),
                manager
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("仅超级管理员可操作");

        verify(configMapper, never()).updateById(any(SysConfig.class));
    }

    @Test
    @DisplayName("删除可编辑配置项时应执行逻辑删除")
    void deleteConfigShouldMarkDeleted() {
        SysConfig existing = editableConfig(4L, "temporary_feature", "true");
        when(configMapper.selectOne(anyWrapper())).thenReturn(existing);

        configService.deleteConfig(4L);

        verify(configMapper).deleteById(4L);
    }

    @Test
    @DisplayName("删除不存在的配置项时应抛出业务异常")
    void deleteConfigShouldRejectMissingConfig() {
        when(configMapper.selectOne(anyWrapper())).thenReturn(null);

        assertThatThrownBy(() -> configService.deleteConfig(404L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("配置项不存在");

        verify(configMapper, never()).updateById(any(SysConfig.class));
    }

    @Test
    @DisplayName("删除系统内置配置项时应拒绝")
    void deleteConfigShouldRejectBuiltInConfig() {
        SysConfig existing = editableConfig(1L, "user_org_relation_mode", "ONE_TO_MANY");
        when(configMapper.selectOne(anyWrapper())).thenReturn(existing);

        assertThatThrownBy(() -> configService.deleteConfig(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("系统内置配置不允许删除");

        verify(configMapper, never()).deleteById(anyLong());
    }

    private SysConfig editableConfig(Long id, String configKey, String configValue) {
        SysConfig config = new SysConfig();
        config.setId(id);
        config.setConfigKey(configKey);
        config.setConfigValue(configValue);
        config.setEditable(1);
        config.setDeleted(0);
        config.setCreateBy("system");
        config.setUpdateBy("system");
        return config;
    }

    @SuppressWarnings("unchecked")
    private Page<SysConfig> anyPage() {
        return any(Page.class);
    }

    @SuppressWarnings("unchecked")
    private Wrapper<SysConfig> anyWrapper() {
        return any(Wrapper.class);
    }
}
