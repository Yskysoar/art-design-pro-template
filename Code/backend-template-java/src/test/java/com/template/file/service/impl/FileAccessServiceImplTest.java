package com.template.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.template.common.exception.BusinessException;
import com.template.file.entity.FileResource;
import com.template.security.auth.AppUserPrincipal;
import com.template.social.entity.SocialMessage;
import com.template.social.mapper.SocialMessageMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 文件访问权限服务单元测试。
 */
class FileAccessServiceImplTest {

    private static final AppUserPrincipal SENDER = new AppUserPrincipal(1L, "sender", List.of("R_USER"));
    private static final AppUserPrincipal STRANGER = new AppUserPrincipal(9L, "stranger", List.of("R_USER"));

    private final SocialMessageMapper socialMessageMapper = mock(SocialMessageMapper.class);
    private final FileAccessServiceImpl fileAccessService = new FileAccessServiceImpl(socialMessageMapper);

    @Test
    @DisplayName("未被私信引用的文件允许匿名读取")
    void publicFileShouldAllowAnonymousRead() {
        when(socialMessageMapper.selectCount(anyWrapper())).thenReturn(0L);

        fileAccessService.requireReadable(file(8L), null);
    }

    @Test
    @DisplayName("私信文件匿名读取应拒绝")
    void privateMessageFileShouldRejectAnonymousRead() {
        when(socialMessageMapper.selectCount(anyWrapper())).thenReturn(1L);

        assertThatThrownBy(() -> fileAccessService.requireReadable(file(8L), null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("请先登录");
    }

    @Test
    @DisplayName("私信文件非参与者读取应拒绝")
    void privateMessageFileShouldRejectNonParticipant() {
        when(socialMessageMapper.selectCount(anyWrapper())).thenReturn(1L, 0L);

        assertThatThrownBy(() -> fileAccessService.requireReadable(file(8L), STRANGER))
                .isInstanceOf(BusinessException.class)
                .hasMessage("无权限访问该文件");
    }

    @Test
    @DisplayName("私信文件参与者允许读取")
    void privateMessageFileShouldAllowParticipant() {
        when(socialMessageMapper.selectCount(anyWrapper())).thenReturn(1L, 1L);

        fileAccessService.requireReadable(file(8L), SENDER);
    }

    private FileResource file(Long id) {
        FileResource file = new FileResource();
        file.setId(id);
        return file;
    }

    @SuppressWarnings("unchecked")
    private Wrapper<SocialMessage> anyWrapper() {
        return any(Wrapper.class);
    }
}
