package com.template.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.template.common.exception.BusinessException;
import com.template.common.response.ApiCode;
import com.template.file.entity.FileResource;
import com.template.file.service.FileAccessService;
import com.template.security.auth.AppUserPrincipal;
import com.template.social.entity.SocialMessage;
import com.template.social.mapper.SocialMessageMapper;
import org.springframework.stereotype.Service;

/**
 * 文件访问权限服务实现。
 */
@Service
public class FileAccessServiceImpl implements FileAccessService {

    private static final long NOT_DELETED = 0L;

    private final SocialMessageMapper socialMessageMapper;

    public FileAccessServiceImpl(SocialMessageMapper socialMessageMapper) {
        this.socialMessageMapper = socialMessageMapper;
    }

    @Override
    public void requireReadable(FileResource file, AppUserPrincipal principal) {
        if (file == null || file.getId() == null) {
            throw new BusinessException(ApiCode.NOT_FOUND, "文件不存在");
        }
        Long privateMessageCount = socialMessageMapper.selectCount(new LambdaQueryWrapper<SocialMessage>()
                .eq(SocialMessage::getFileId, file.getId())
                .eq(SocialMessage::getDeleted, NOT_DELETED));
        if (privateMessageCount == null || privateMessageCount == 0) {
            return;
        }
        if (principal == null) {
            throw new BusinessException(ApiCode.UNAUTHORIZED, "请先登录");
        }
        Long participantCount = socialMessageMapper.selectCount(new LambdaQueryWrapper<SocialMessage>()
                .eq(SocialMessage::getFileId, file.getId())
                .eq(SocialMessage::getDeleted, NOT_DELETED)
                .and(wrapper -> wrapper
                        .eq(SocialMessage::getSenderId, principal.userId())
                        .or()
                        .eq(SocialMessage::getReceiverId, principal.userId())));
        if (participantCount == null || participantCount == 0) {
            throw new BusinessException(ApiCode.FORBIDDEN, "无权限访问该文件");
        }
    }
}
