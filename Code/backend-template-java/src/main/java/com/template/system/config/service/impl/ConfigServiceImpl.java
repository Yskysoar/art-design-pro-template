package com.template.system.config.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.template.common.exception.BusinessException;
import com.template.common.pagination.PageResult;
import com.template.common.response.ApiCode;
import com.template.security.auth.AppUserPrincipal;
import com.template.system.config.dto.ConfigListQuery;
import com.template.system.config.dto.ConfigSaveRequest;
import com.template.system.config.entity.SysConfig;
import com.template.system.config.mapper.SysConfigMapper;
import com.template.system.config.service.ConfigService;
import com.template.system.config.vo.ConfigItemVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 系统配置项业务服务实现。
 */
@Service
public class ConfigServiceImpl implements ConfigService {

    private static final int EDITABLE = 1;
    private static final int NOT_DELETED = 0;
    private static final long DEFAULT_CURRENT = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysConfigMapper configMapper;

    public ConfigServiceImpl(SysConfigMapper configMapper) {
        this.configMapper = configMapper;
    }

    @Override
    public PageResult<ConfigItemVo> pageConfigs(ConfigListQuery query) {
        long current = query.current() == null || query.current() < 1 ? DEFAULT_CURRENT : query.current();
        long size = query.size() == null || query.size() < 1 ? DEFAULT_SIZE : Math.min(query.size(), MAX_SIZE);

        IPage<SysConfig> page = configMapper.selectPage(Page.of(current, size), buildQueryWrapper(query));
        return new PageResult<>(
                page.getRecords().stream().map(this::toVo).toList(),
                page.getCurrent(),
                page.getSize(),
                page.getTotal()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createConfig(ConfigSaveRequest request, AppUserPrincipal principal) {
        assertConfigKeyUnique(request.configKey(), null);

        SysConfig config = new SysConfig();
        config.setConfigKey(request.configKey());
        config.setConfigValue(request.configValue());
        config.setDescription(request.description());
        config.setEditable(toEditableValue(request.editable()));
        config.setCreateBy(principal.userName());
        config.setUpdateBy(principal.userName());
        config.setDeleted(NOT_DELETED);

        configMapper.insert(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(Long id, ConfigSaveRequest request, AppUserPrincipal principal) {
        SysConfig config = getExistingConfig(id);
        assertEditable(config);
        assertConfigKeyUnique(request.configKey(), id);

        config.setConfigKey(request.configKey());
        config.setConfigValue(request.configValue());
        config.setDescription(request.description());
        config.setEditable(toEditableValue(request.editable()));
        config.setUpdateBy(principal.userName());

        configMapper.updateById(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(Long id) {
        SysConfig config = getExistingConfig(id);
        assertEditable(config);
        configMapper.deleteById(id);
    }

    private LambdaQueryWrapper<SysConfig> buildQueryWrapper(ConfigListQuery query) {
        return new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getDeleted, NOT_DELETED)
                .like(StringUtils.hasText(query.configKey()), SysConfig::getConfigKey, query.configKey())
                .like(StringUtils.hasText(query.description()), SysConfig::getDescription, query.description())
                .eq(query.editable() != null, SysConfig::getEditable, Boolean.TRUE.equals(query.editable()) ? EDITABLE : 0)
                .orderByDesc(SysConfig::getCreateTime);
    }

    private SysConfig getExistingConfig(Long id) {
        if (id == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "配置ID不能为空");
        }

        SysConfig config = configMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getId, id)
                .eq(SysConfig::getDeleted, NOT_DELETED));
        if (config == null) {
            throw new BusinessException(ApiCode.NOT_FOUND, "配置项不存在");
        }
        return config;
    }

    private void assertConfigKeyUnique(String configKey, Long excludeId) {
        Long count = configMapper.selectCount(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, configKey)
                .eq(SysConfig::getDeleted, NOT_DELETED)
                .ne(excludeId != null, SysConfig::getId, excludeId));
        if (count != null && count > 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "配置键已存在");
        }
    }

    private void assertEditable(SysConfig config) {
        if (!Integer.valueOf(EDITABLE).equals(config.getEditable())) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "该配置项不可编辑");
        }
    }

    private ConfigItemVo toVo(SysConfig config) {
        return new ConfigItemVo(
                config.getId(),
                config.getConfigKey(),
                config.getConfigValue(),
                config.getDescription(),
                Integer.valueOf(EDITABLE).equals(config.getEditable()),
                config.getCreateBy(),
                formatDateTime(config.getCreateTime()),
                config.getUpdateBy(),
                formatDateTime(config.getUpdateTime())
        );
    }

    private int toEditableValue(Boolean editable) {
        return Boolean.FALSE.equals(editable) ? 0 : EDITABLE;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : DATE_TIME_FORMATTER.format(dateTime);
    }
}
