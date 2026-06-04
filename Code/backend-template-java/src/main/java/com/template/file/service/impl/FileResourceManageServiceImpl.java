package com.template.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.template.article.entity.Article;
import com.template.article.entity.ArticleAttachment;
import com.template.article.mapper.ArticleAttachmentMapper;
import com.template.article.mapper.ArticleMapper;
import com.template.common.exception.BusinessException;
import com.template.common.pagination.PageResult;
import com.template.common.response.ApiCode;
import com.template.file.dto.FileResourceListQuery;
import com.template.file.entity.FileResource;
import com.template.file.mapper.FileResourceMapper;
import com.template.file.service.FileResourceManageService;
import com.template.file.vo.FileResourceItemVo;
import com.template.social.entity.SocialMessage;
import com.template.social.mapper.SocialMessageMapper;
import com.template.system.user.entity.SysUser;
import com.template.system.user.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 文件资源管理服务实现。
 */
@Service
public class FileResourceManageServiceImpl implements FileResourceManageService {

    private static final int NOT_DELETED = 0;
    private static final long DEFAULT_CURRENT = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final FileResourceMapper fileResourceMapper;
    private final ArticleMapper articleMapper;
    private final ArticleAttachmentMapper articleAttachmentMapper;
    private final SocialMessageMapper socialMessageMapper;
    private final SysUserMapper userMapper;

    public FileResourceManageServiceImpl(
            FileResourceMapper fileResourceMapper,
            ArticleMapper articleMapper,
            ArticleAttachmentMapper articleAttachmentMapper,
            SocialMessageMapper socialMessageMapper,
            SysUserMapper userMapper
    ) {
        this.fileResourceMapper = fileResourceMapper;
        this.articleMapper = articleMapper;
        this.articleAttachmentMapper = articleAttachmentMapper;
        this.socialMessageMapper = socialMessageMapper;
        this.userMapper = userMapper;
    }

    @Override
    public PageResult<FileResourceItemVo> pageResources(FileResourceListQuery query) {
        long current = query.current() == null || query.current() < 1 ? DEFAULT_CURRENT : query.current();
        long size = query.size() == null || query.size() < 1 ? DEFAULT_SIZE : Math.min(query.size(), MAX_SIZE);
        IPage<FileResource> page = fileResourceMapper.selectPage(Page.of(current, size), buildQueryWrapper(query));
        Map<Long, String> userNameMap = loadUserNameMap(page.getRecords());
        List<FileResourceItemVo> records = page.getRecords().stream()
                .map(resource -> toVo(resource, userNameMap))
                .filter(vo -> query.referenced() == null || query.referenced().equals(vo.referenced()))
                .toList();
        return new PageResult<>(records, page.getCurrent(), page.getSize(), page.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteResource(Long id) {
        FileResource resource = getExistingResource(id);
        long referenceCount = countReferences(resource);
        if (referenceCount > 0) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "文件已被业务引用，不能删除");
        }
        fileResourceMapper.deleteById(id);
    }

    private LambdaQueryWrapper<FileResource> buildQueryWrapper(FileResourceListQuery query) {
        LocalDateTime startTime = parseDateTime(query.createTimeStart(), "创建开始时间格式不正确");
        LocalDateTime endTime = parseDateTime(query.createTimeEnd(), "创建结束时间格式不正确");
        String extension = normalize(query.extension());
        String contentType = normalize(query.contentType());
        String storageType = normalize(query.storageType());
        return new LambdaQueryWrapper<FileResource>()
                .eq(FileResource::getDeleted, NOT_DELETED)
                .and(StringUtils.hasText(query.keyword()), wrapper -> wrapper
                        .like(FileResource::getOriginalName, query.keyword())
                        .or()
                        .like(FileResource::getUrl, query.keyword())
                        .or()
                        .like(FileResource::getSha256, query.keyword()))
                .eq(StringUtils.hasText(extension), FileResource::getExtension, extension)
                .like(StringUtils.hasText(contentType), FileResource::getContentType, contentType)
                .eq(StringUtils.hasText(storageType), FileResource::getStorageType, storageType)
                .eq(query.uploaderId() != null, FileResource::getUploaderId, query.uploaderId())
                .ge(query.minSize() != null, FileResource::getSize, query.minSize())
                .le(query.maxSize() != null, FileResource::getSize, query.maxSize())
                .ge(startTime != null, FileResource::getCreateTime, startTime)
                .le(endTime != null, FileResource::getCreateTime, endTime)
                .orderByDesc(FileResource::getCreateTime);
    }

    private FileResource getExistingResource(Long id) {
        if (id == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "文件ID不能为空");
        }
        FileResource resource = fileResourceMapper.selectOne(new LambdaQueryWrapper<FileResource>()
                .eq(FileResource::getId, id)
                .eq(FileResource::getDeleted, NOT_DELETED));
        if (resource == null) {
            throw new BusinessException(ApiCode.NOT_FOUND, "文件资源不存在");
        }
        return resource;
    }

    private FileResourceItemVo toVo(FileResource resource, Map<Long, String> userNameMap) {
        long referenceCount = countReferences(resource);
        return new FileResourceItemVo(
                resource.getId(),
                resource.getOriginalName(),
                resource.getUrl(),
                resource.getContentType(),
                resource.getExtension(),
                resource.getSize(),
                resource.getSha256(),
                resource.getStorageType(),
                resource.getUploaderId(),
                userNameMap.get(resource.getUploaderId()),
                resource.getCreateBy(),
                formatDateTime(resource.getCreateTime()),
                referenceCount > 0,
                referenceCount
        );
    }

    private long countReferences(FileResource resource) {
        long count = 0;
        count += safeCount(articleAttachmentMapper.selectCount(new LambdaQueryWrapper<ArticleAttachment>()
                .eq(ArticleAttachment::getFileId, resource.getId())));
        count += safeCount(articleMapper.selectCount(new LambdaQueryWrapper<Article>()
                .eq(Article::getDeleted, NOT_DELETED)
                .eq(Article::getCoverUrl, resource.getUrl())));
        count += safeCount(articleMapper.selectCount(new LambdaQueryWrapper<Article>()
                .eq(Article::getDeleted, NOT_DELETED)
                .like(Article::getContentHtml, resource.getUrl())));
        count += safeCount(socialMessageMapper.selectCount(new LambdaQueryWrapper<SocialMessage>()
                .eq(SocialMessage::getDeleted, 0L)
                .eq(SocialMessage::getFileId, resource.getId())));
        return count;
    }

    private Map<Long, String> loadUserNameMap(List<FileResource> resources) {
        Set<Long> userIds = resources.stream()
                .map(FileResource::getUploaderId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                        .in(SysUser::getId, userIds)
                        .eq(SysUser::getDeleted, NOT_DELETED))
                .stream()
                .collect(Collectors.toMap(SysUser::getId, this::displayName, (left, right) -> left));
    }

    private String displayName(SysUser user) {
        return StringUtils.hasText(user.getNickName()) ? user.getNickName() : user.getUserName();
    }

    private long safeCount(Long count) {
        return count == null ? 0L : count;
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase() : null;
    }

    private LocalDateTime parseDateTime(String value, String errorMessage) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return LocalDateTime.parse(value.trim(), DATE_TIME_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new BusinessException(ApiCode.BAD_REQUEST, errorMessage);
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : DATE_TIME_FORMATTER.format(dateTime);
    }
}
