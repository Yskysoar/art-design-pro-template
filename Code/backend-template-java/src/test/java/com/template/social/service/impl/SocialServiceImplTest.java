package com.template.social.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.template.common.exception.BusinessException;
import com.template.common.security.SensitiveWordGuard;
import com.template.file.service.FileStorageService;
import com.template.security.auth.AppUserPrincipal;
import com.template.social.convert.SocialStructMapper;
import com.template.social.dto.SocialMessageSendRequest;
import com.template.social.entity.SocialBlock;
import com.template.social.entity.SocialConversation;
import com.template.social.entity.SocialFollow;
import com.template.social.entity.SocialMessage;
import com.template.social.mapper.SocialBlockMapper;
import com.template.social.mapper.SocialConversationMapper;
import com.template.social.mapper.SocialFollowMapper;
import com.template.social.mapper.SocialMessageMapper;
import com.template.social.vo.SocialMessageVo;
import com.template.system.user.entity.SysUser;
import com.template.system.user.mapper.SysUserMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 社交关注与聊天业务服务单元测试。
 */
@ExtendWith(MockitoExtension.class)
class SocialServiceImplTest {

    private static final AppUserPrincipal ADMIN = new AppUserPrincipal(1L, "admin", List.of("R_SUPER"));
    private static final Long TARGET_ID = 2L;

    @Mock
    private SysUserMapper userMapper;
    @Mock
    private SocialFollowMapper followMapper;
    @Mock
    private SocialBlockMapper blockMapper;
    @Mock
    private SocialConversationMapper conversationMapper;
    @Mock
    private SocialMessageMapper messageMapper;
    @Mock
    private SocialStructMapper socialStructMapper;
    @Mock
    private SensitiveWordGuard sensitiveWordGuard;
    @Mock
    private FileStorageService fileStorageService;

    private SocialServiceImpl socialService;

    @BeforeEach
    void setUp() {
        initTableInfo(SocialFollow.class);
        initTableInfo(SocialMessage.class);
        socialService = new SocialServiceImpl(userMapper, followMapper, blockMapper, conversationMapper, messageMapper, socialStructMapper, sensitiveWordGuard, fileStorageService);
    }

    @Test
    @DisplayName("不能关注自己")
    void followShouldRejectSelf() {
        assertThatThrownBy(() -> socialService.follow(ADMIN.userId(), ADMIN))
                .isInstanceOf(BusinessException.class)
                .hasMessage("不能关注自己");

        verify(followMapper, never()).insert(any(SocialFollow.class));
    }

    @Test
    @DisplayName("关注已拉黑用户应被拒绝")
    void followShouldRejectBlockedUser() {
        when(userMapper.selectOne(anyWrapper())).thenReturn(user(TARGET_ID, "designer"));
        when(blockMapper.selectCount(anyWrapper())).thenReturn(1L);

        assertThatThrownBy(() -> socialService.follow(TARGET_ID, ADMIN))
                .isInstanceOf(BusinessException.class)
                .hasMessage("已拉黑或被对方拉黑，不能关注");

        verify(followMapper, never()).insert(any(SocialFollow.class));
    }

    @Test
    @DisplayName("拉黑用户应取消双方关注关系")
    void blockShouldCancelBothFollows() {
        when(userMapper.selectOne(anyWrapper())).thenReturn(user(TARGET_ID, "designer"));
        when(blockMapper.selectCount(anyWrapper())).thenReturn(0L);

        socialService.block(TARGET_ID, ADMIN);

        ArgumentCaptor<SocialBlock> blockCaptor = ArgumentCaptor.forClass(SocialBlock.class);
        verify(blockMapper).insert(blockCaptor.capture());
        assertThat(blockCaptor.getValue().getBlockerId()).isEqualTo(ADMIN.userId());
        assertThat(blockCaptor.getValue().getBlockedId()).isEqualTo(TARGET_ID);
        verify(followMapper, times(2)).update(eq(null), anyWrapper());
    }

    @Test
    @DisplayName("未互关且对方未回复时最多发送三条私信")
    void sendMessageShouldRejectWhenQuotaUsed() {
        when(userMapper.selectOne(anyWrapper())).thenReturn(user(TARGET_ID, "designer"));
        when(blockMapper.selectCount(anyWrapper())).thenReturn(0L, 0L);
        when(followMapper.selectCount(anyWrapper())).thenReturn(0L, 0L);
        when(messageMapper.selectOne(anyWrapper())).thenReturn(null);
        when(messageMapper.selectCount(anyWrapper())).thenReturn(3L);

        assertThatThrownBy(() -> socialService.sendMessage(new SocialMessageSendRequest(TARGET_ID, "TEXT", "第四条消息", null), ADMIN))
                .isInstanceOf(BusinessException.class)
                .hasMessage("对方回复前最多发送3条私信")
                .extracting(error -> ((BusinessException) error).getData())
                .satisfies(data -> assertThat(data).hasFieldOrPropertyWithValue("remainingQuota", 0));

        verify(messageMapper, never()).insert(any(SocialMessage.class));
    }

    @Test
    @DisplayName("互相关注后发送私信不受三条额度限制")
    void sendMessageShouldAllowMutualFollow() {
        when(userMapper.selectOne(anyWrapper())).thenReturn(user(TARGET_ID, "designer"), user(ADMIN.userId(), "admin"));
        when(blockMapper.selectCount(anyWrapper())).thenReturn(0L, 0L);
        when(followMapper.selectCount(anyWrapper())).thenReturn(1L, 1L);
        when(conversationMapper.selectOne(anyWrapper())).thenReturn(conversation(10L));
        when(messageMapper.insert(any(SocialMessage.class))).thenAnswer(invocation -> {
            SocialMessage message = invocation.getArgument(0);
            message.setId(99L);
            return 1;
        });
        when(socialStructMapper.toMessageVo(any(SocialMessage.class), eq("admin"), eq(null), eq(true), eq(false), any()))
                .thenAnswer(invocation -> {
                    SocialMessage message = invocation.getArgument(0);
                    String createTime = invocation.getArgument(5);
                    return new SocialMessageVo(
                            message.getId(),
                            message.getConversationId(),
                            message.getSenderId(),
                            message.getReceiverId(),
                            "admin",
                            null,
                            message.getContent(),
                            message.getMessageType(),
                            message.getFileId(),
                            message.getFileUrl(),
                            message.getFileName(),
                            message.getFileSize(),
                            message.getFileContentType(),
                            true,
                            false,
                            createTime
                    );
                });

        var result = socialService.sendMessage(new SocialMessageSendRequest(TARGET_ID, "TEXT", "你好，项目已更新", null), ADMIN);

        assertThat(result.id()).isEqualTo(99L);
        assertThat(result.content()).isEqualTo("你好，项目已更新");
        assertThat(result.mine()).isTrue();
        assertThat(result.createTime()).isNotBlank();
        verify(messageMapper).insert(any(SocialMessage.class));
        verify(conversationMapper).updateById(any(SocialConversation.class));
    }

    @Test
    @DisplayName("私信内容应经过敏感词校验")
    void sendMessageShouldValidateSensitiveWords() {
        when(userMapper.selectOne(anyWrapper())).thenReturn(user(TARGET_ID, "designer"));
        when(blockMapper.selectCount(anyWrapper())).thenReturn(0L, 0L);
        doThrow(new BusinessException(com.template.common.response.ApiCode.BAD_REQUEST, "私信内容包含敏感词"))
                .when(sensitiveWordGuard)
                .validate(eq("私信内容"), any());

        assertThatThrownBy(() -> socialService.sendMessage(new SocialMessageSendRequest(TARGET_ID, "TEXT", "敏感内容", null), ADMIN))
                .isInstanceOf(BusinessException.class)
                .hasMessage("私信内容包含敏感词");

        verify(messageMapper, never()).insert(any(SocialMessage.class));
    }

    @Test
    @DisplayName("标记已读只更新当前用户收到的未读消息")
    void markConversationReadShouldOnlyUpdateReceiverUnreadMessages() {
        when(conversationMapper.selectOne(anyWrapper())).thenReturn(conversation(10L));

        socialService.markConversationRead(10L, ADMIN);

        verify(messageMapper).update(eq(null), anyWrapper());
    }

    private SocialConversation conversation(Long id) {
        SocialConversation conversation = new SocialConversation();
        conversation.setId(id);
        conversation.setUserAId(1L);
        conversation.setUserBId(2L);
        conversation.setCreateTime(LocalDateTime.of(2026, 6, 1, 10, 0));
        conversation.setDeleted(0L);
        return conversation;
    }

    private SysUser user(Long id, String userName) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUserName(userName);
        user.setNickName(userName);
        user.setStatus("NORMAL");
        user.setDeleted(0);
        return user;
    }

    @SuppressWarnings("unchecked")
    private <T> Wrapper<T> anyWrapper() {
        return any(Wrapper.class);
    }

    private void initTableInfo(Class<?> entityType) {
        if (TableInfoHelper.getTableInfo(entityType) == null) {
            TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), entityType);
        }
    }
}
