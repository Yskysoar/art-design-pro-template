package com.template.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.template.common.exception.BusinessException;
import com.template.notification.dto.NotificationListQuery;
import com.template.notification.entity.NotificationMessage;
import com.template.notification.mapper.NotificationMessageMapper;
import com.template.security.auth.AppUserPrincipal;
import com.template.system.user.entity.SysUser;
import com.template.system.user.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.ibatis.builder.MapperBuilderAssistant;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 用户通知消息服务单元测试。
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    private static final AppUserPrincipal ADMIN = new AppUserPrincipal(1L, "admin", List.of("R_SUPER"));

    @Mock
    private NotificationMessageMapper notificationMapper;
    @Mock
    private SysUserMapper userMapper;

    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        initTableInfo(NotificationMessage.class);
        notificationService = new NotificationServiceImpl(notificationMapper, userMapper);
    }

    @Test
    @DisplayName("分页查询通知时应映射触发人和已读状态")
    void pageNotificationsShouldMapActorAndReadState() {
        NotificationMessage message = notification(10L, "PRIVATE_MESSAGE");
        message.setReadTime(LocalDateTime.of(2026, 6, 4, 11, 0));
        Page<NotificationMessage> page = Page.of(1, 20);
        page.setRecords(List.of(message));
        page.setTotal(1);
        SysUser actor = user(2L, "designer", "设计同学");

        when(notificationMapper.selectPage(anyPage(), anyWrapper())).thenReturn(page);
        when(userMapper.selectList(anyUserWrapper())).thenReturn(List.of(actor));

        var result = notificationService.pageNotifications(new NotificationListQuery(1L, 20L, "private_message", false), ADMIN);

        assertThat(result.records()).hasSize(1);
        assertThat(result.records().get(0).actorName()).isEqualTo("设计同学");
        assertThat(result.records().get(0).read()).isTrue();
        assertThat(result.records().get(0).createTime()).isEqualTo("2026-06-04 10:30:00");
    }

    @Test
    @DisplayName("创建私信通知时应写入目标会话和摘要")
    void createPrivateMessageNotificationShouldInsertMessage() {
        when(userMapper.selectById(2L)).thenReturn(user(2L, "designer", "设计同学"));

        notificationService.createPrivateMessageNotification(2L, 1L, 99L, "你好，资料已更新");

        ArgumentCaptor<NotificationMessage> captor = ArgumentCaptor.forClass(NotificationMessage.class);
        verify(notificationMapper).insert(captor.capture());
        NotificationMessage inserted = captor.getValue();
        assertThat(inserted.getRecipientId()).isEqualTo(1L);
        assertThat(inserted.getActorId()).isEqualTo(2L);
        assertThat(inserted.getNoticeType()).isEqualTo("PRIVATE_MESSAGE");
        assertThat(inserted.getTitle()).isEqualTo("设计同学给你发来一条私信");
        assertThat(inserted.getTargetType()).isEqualTo("CONVERSATION");
        assertThat(inserted.getTargetId()).isEqualTo(99L);
        assertThat(inserted.getTargetUrl()).isEqualTo("/social/chat");
    }

    @Test
    @DisplayName("创建通知时不应给自己发送")
    void createNotificationShouldIgnoreSelf() {
        notificationService.createFollowNotification(1L, 1L);

        verify(notificationMapper, never()).insert(any(NotificationMessage.class));
    }

    @Test
    @DisplayName("标记不存在通知已读时应抛出业务异常")
    void markReadShouldRejectMissingNotification() {
        when(notificationMapper.update(eq(null), any())).thenReturn(0);
        when(notificationMapper.selectCount(anyWrapper())).thenReturn(0L);

        assertThatThrownBy(() -> notificationService.markRead(404L, ADMIN))
                .isInstanceOf(BusinessException.class)
                .hasMessage("通知不存在");
    }

    private NotificationMessage notification(Long id, String noticeType) {
        NotificationMessage message = new NotificationMessage();
        message.setId(id);
        message.setRecipientId(1L);
        message.setActorId(2L);
        message.setNoticeType(noticeType);
        message.setTitle("设计同学给你发来一条私信");
        message.setContent("你好");
        message.setTargetType("CONVERSATION");
        message.setTargetId(99L);
        message.setTargetUrl("/social/chat");
        message.setCreateTime(LocalDateTime.of(2026, 6, 4, 10, 30));
        message.setDeleted(0L);
        return message;
    }

    private SysUser user(Long id, String userName, String nickName) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUserName(userName);
        user.setNickName(nickName);
        return user;
    }

    private void initTableInfo(Class<?> entityClass) {
        if (TableInfoHelper.getTableInfo(entityClass) != null) {
            return;
        }
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, entityClass);
    }

    @SuppressWarnings("unchecked")
    private Page<NotificationMessage> anyPage() {
        return any(Page.class);
    }

    @SuppressWarnings("unchecked")
    private Wrapper<NotificationMessage> anyWrapper() {
        return any(Wrapper.class);
    }

    @SuppressWarnings("unchecked")
    private Wrapper<SysUser> anyUserWrapper() {
        return any(Wrapper.class);
    }
}
