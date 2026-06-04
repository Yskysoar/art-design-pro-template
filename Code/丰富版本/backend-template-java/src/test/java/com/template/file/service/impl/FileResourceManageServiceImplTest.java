package com.template.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.template.article.entity.Article;
import com.template.article.entity.ArticleAttachment;
import com.template.article.mapper.ArticleAttachmentMapper;
import com.template.article.mapper.ArticleMapper;
import com.template.common.exception.BusinessException;
import com.template.common.pagination.PageResult;
import com.template.file.dto.FileResourceListQuery;
import com.template.file.entity.FileResource;
import com.template.file.mapper.FileResourceMapper;
import com.template.file.vo.FileResourceItemVo;
import com.template.social.entity.SocialMessage;
import com.template.social.mapper.SocialMessageMapper;
import com.template.system.user.entity.SysUser;
import com.template.system.user.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 文件资源管理服务单元测试。
 */
@ExtendWith(MockitoExtension.class)
class FileResourceManageServiceImplTest {

    @Mock
    private FileResourceMapper fileResourceMapper;
    @Mock
    private ArticleMapper articleMapper;
    @Mock
    private ArticleAttachmentMapper articleAttachmentMapper;
    @Mock
    private SocialMessageMapper socialMessageMapper;
    @Mock
    private SysUserMapper userMapper;

    private FileResourceManageServiceImpl fileResourceManageService;

    @BeforeEach
    void setUp() {
        fileResourceManageService = new FileResourceManageServiceImpl(
                fileResourceMapper,
                articleMapper,
                articleAttachmentMapper,
                socialMessageMapper,
                userMapper
        );
    }

    @Test
    @DisplayName("分页查询文件资源时应映射上传人和引用数量")
    void pageResourcesShouldMapUploaderAndReferenceCount() {
        FileResource resource = imageResource(10L);
        Page<FileResource> page = Page.of(1, 20);
        page.setRecords(List.of(resource));
        page.setTotal(1);
        SysUser uploader = new SysUser();
        uploader.setId(2L);
        uploader.setUserName("designer");
        uploader.setNickName("设计同学");

        when(fileResourceMapper.selectPage(anyPage(), anyWrapper())).thenReturn(page);
        when(userMapper.selectList(anyUserWrapper())).thenReturn(List.of(uploader));
        when(articleAttachmentMapper.selectCount(anyArticleAttachmentWrapper())).thenReturn(1L);
        when(articleMapper.selectCount(anyArticleWrapper())).thenReturn(0L, 1L);
        when(socialMessageMapper.selectCount(anySocialMessageWrapper())).thenReturn(0L);

        PageResult<FileResourceItemVo> result = fileResourceManageService.pageResources(
                new FileResourceListQuery(1L, 20L, "banner", "PNG", "image", "LOCAL", 2L, null, null, null, null, null)
        );

        assertThat(result.records()).hasSize(1);
        FileResourceItemVo item = result.records().get(0);
        assertThat(item.originalName()).isEqualTo("summer-banner.png");
        assertThat(item.uploaderName()).isEqualTo("设计同学");
        assertThat(item.referenced()).isTrue();
        assertThat(item.referenceCount()).isEqualTo(2L);
        assertThat(item.createTime()).isEqualTo("2026-06-04 10:30:00");
    }

    @Test
    @DisplayName("删除已被业务引用的文件资源时应拒绝")
    void deleteResourceShouldRejectReferencedResource() {
        FileResource resource = imageResource(10L);
        when(fileResourceMapper.selectOne(anyWrapper())).thenReturn(resource);
        when(articleAttachmentMapper.selectCount(anyArticleAttachmentWrapper())).thenReturn(1L);
        when(articleMapper.selectCount(anyArticleWrapper())).thenReturn(0L, 0L);
        when(socialMessageMapper.selectCount(anySocialMessageWrapper())).thenReturn(0L);

        assertThatThrownBy(() -> fileResourceManageService.deleteResource(10L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("文件已被业务引用，不能删除");

        verify(fileResourceMapper, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("按引用状态筛选时应返回过滤后的分页总数")
    void pageResourcesShouldReturnAccurateTotalWhenFilteringReferenceState() {
        FileResource referenced = imageResource(10L);
        FileResource unreferenced = imageResource(11L);
        unreferenced.setOriginalName("unused-banner.png");
        unreferenced.setUrl("/api/common/files/article/20260604/unused-banner.png");
        SysUser uploader = new SysUser();
        uploader.setId(2L);
        uploader.setUserName("designer");
        uploader.setNickName("设计同学");

        when(fileResourceMapper.selectList(anyWrapper())).thenReturn(List.of(referenced, unreferenced));
        when(userMapper.selectList(anyUserWrapper())).thenReturn(List.of(uploader));
        when(articleAttachmentMapper.selectCount(anyArticleAttachmentWrapper())).thenReturn(1L, 0L);
        when(articleMapper.selectCount(anyArticleWrapper())).thenReturn(0L, 0L, 0L, 0L);
        when(socialMessageMapper.selectCount(anySocialMessageWrapper())).thenReturn(0L, 0L);

        PageResult<FileResourceItemVo> result = fileResourceManageService.pageResources(
                new FileResourceListQuery(1L, 20L, null, null, null, null, null, true, null, null, null, null)
        );

        assertThat(result.total()).isEqualTo(1L);
        assertThat(result.records()).hasSize(1);
        assertThat(result.records().get(0).id()).isEqualTo(10L);
        assertThat(result.records().get(0).referenced()).isTrue();
    }

    @Test
    @DisplayName("删除未引用文件资源时应删除台账记录")
    void deleteResourceShouldDeleteUnreferencedResource() {
        FileResource resource = imageResource(10L);
        when(fileResourceMapper.selectOne(anyWrapper())).thenReturn(resource);
        when(articleAttachmentMapper.selectCount(anyArticleAttachmentWrapper())).thenReturn(0L);
        when(articleMapper.selectCount(anyArticleWrapper())).thenReturn(0L, 0L);
        when(socialMessageMapper.selectCount(anySocialMessageWrapper())).thenReturn(0L);

        fileResourceManageService.deleteResource(10L);

        verify(fileResourceMapper).deleteById(10L);
    }

    @Test
    @DisplayName("分页查询创建时间格式错误时应抛出业务异常")
    void pageResourcesShouldRejectInvalidDateTime() {
        assertThatThrownBy(() -> fileResourceManageService.pageResources(
                new FileResourceListQuery(1L, 20L, null, null, null, null, null, null, null, null, "2026/06/04", null)
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("创建开始时间格式不正确");

        verify(fileResourceMapper, never()).selectPage(anyPage(), anyWrapper());
    }

    private FileResource imageResource(Long id) {
        FileResource resource = new FileResource();
        resource.setId(id);
        resource.setOriginalName("summer-banner.png");
        resource.setUrl("/api/common/files/article/20260604/summer-banner.png");
        resource.setContentType("image/png");
        resource.setExtension("png");
        resource.setSize(2048L);
        resource.setSha256("a".repeat(64));
        resource.setStorageType("LOCAL");
        resource.setUploaderId(2L);
        resource.setCreateBy("designer");
        resource.setCreateTime(LocalDateTime.of(2026, 6, 4, 10, 30));
        resource.setDeleted(0);
        return resource;
    }

    @SuppressWarnings("unchecked")
    private Page<FileResource> anyPage() {
        return any(Page.class);
    }

    @SuppressWarnings("unchecked")
    private Wrapper<FileResource> anyWrapper() {
        return any(Wrapper.class);
    }

    @SuppressWarnings("unchecked")
    private Wrapper<ArticleAttachment> anyArticleAttachmentWrapper() {
        return any(Wrapper.class);
    }

    @SuppressWarnings("unchecked")
    private Wrapper<Article> anyArticleWrapper() {
        return any(Wrapper.class);
    }

    @SuppressWarnings("unchecked")
    private Wrapper<SocialMessage> anySocialMessageWrapper() {
        return any(Wrapper.class);
    }

    @SuppressWarnings("unchecked")
    private Wrapper<SysUser> anyUserWrapper() {
        return any(Wrapper.class);
    }
}
