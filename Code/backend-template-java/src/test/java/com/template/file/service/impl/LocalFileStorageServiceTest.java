package com.template.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.template.common.exception.BusinessException;
import com.template.file.config.FileStorageProperties;
import com.template.file.entity.FileResource;
import com.template.file.mapper.FileResourceMapper;
import com.template.file.vo.UploadResponse;
import com.template.security.auth.AppUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 本地文件存储服务单元测试。
 */
@ExtendWith(MockitoExtension.class)
class LocalFileStorageServiceTest {

    private static final AppUserPrincipal ADMIN = new AppUserPrincipal(1L, "admin", List.of("R_SUPER"));

    @TempDir
    private Path tempDir;

    @Mock
    private FileResourceMapper fileResourceMapper;

    private LocalFileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new LocalFileStorageService(
                fileResourceMapper,
                new FileStorageProperties(tempDir.toString())
        );
    }

    @Test
    @DisplayName("上传图片成功时应落库元数据并返回受控URL")
    void uploadImageShouldInsertMetadataAndReturnControlledUrl() {
        MockMultipartFile file = new MockMultipartFile("file", "cover.png", "image/png", "image".getBytes());

        UploadResponse response = fileStorageService.uploadImage(file, ADMIN);

        ArgumentCaptor<FileResource> captor = ArgumentCaptor.forClass(FileResource.class);
        verify(fileResourceMapper).insert(captor.capture());
        FileResource saved = captor.getValue();
        assertThat(saved.getOriginalName()).isEqualTo("cover.png");
        assertThat(saved.getExtension()).isEqualTo("png");
        assertThat(saved.getStoragePath()).doesNotContain("..");
        assertThat(saved.getUrl()).startsWith("/api/common/files/");
        assertThat(saved.getSha256()).hasSize(64);
        assertThat(response.url()).isEqualTo(saved.getUrl());
    }

    @Test
    @DisplayName("上传图片应拒绝非法后缀")
    void uploadImageShouldRejectUnsupportedExtension() {
        MockMultipartFile file = new MockMultipartFile("file", "shell.svg", "image/svg+xml", "<svg/>".getBytes());

        assertThatThrownBy(() -> fileStorageService.uploadImage(file, ADMIN))
                .isInstanceOf(BusinessException.class)
                .hasMessage("不支持的文件后缀：svg");

        verify(fileResourceMapper, never()).insert(any(FileResource.class));
    }

    @Test
    @DisplayName("上传图片应拒绝非法MIME")
    void uploadImageShouldRejectUnsupportedMime() {
        MockMultipartFile file = new MockMultipartFile("file", "cover.png", "text/plain", "image".getBytes());

        assertThatThrownBy(() -> fileStorageService.uploadImage(file, ADMIN))
                .isInstanceOf(BusinessException.class)
                .hasMessage("不支持的文件类型");

        verify(fileResourceMapper, never()).insert(any(FileResource.class));
    }

    @Test
    @DisplayName("上传文件应拒绝目录穿越文件名")
    void uploadFileShouldRejectPathTraversalFileName() {
        MockMultipartFile file = new MockMultipartFile("file", "../demo.pdf", "application/pdf", "pdf".getBytes());

        assertThatThrownBy(() -> fileStorageService.uploadFile(file, ADMIN))
                .isInstanceOf(BusinessException.class)
                .hasMessage("文件名不合法");

        verify(fileResourceMapper, never()).insert(any(FileResource.class));
    }

    @Test
    @DisplayName("读取文件应拒绝目录穿越路径")
    void getByStoragePathShouldRejectPathTraversal() {
        assertThatThrownBy(() -> fileStorageService.getByStoragePath("../secret.txt"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("文件路径不合法");
    }

    @SuppressWarnings("unchecked")
    private Wrapper<FileResource> anyWrapper() {
        return any(Wrapper.class);
    }
}
