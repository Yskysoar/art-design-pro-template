package com.template.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.template.common.exception.BusinessException;
import com.template.common.response.ApiCode;
import com.template.file.config.FileStorageProperties;
import com.template.file.entity.FileResource;
import com.template.file.mapper.FileResourceMapper;
import com.template.file.service.FileStorageService;
import com.template.file.vo.UploadResponse;
import com.template.security.auth.AppUserPrincipal;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 本地文件存储服务实现。
 */
@Service
public class LocalFileStorageService implements FileStorageService {

    private static final int NOT_DELETED = 0;
    private static final long IMAGE_MAX_SIZE = 5L * 1024 * 1024;
    private static final long FILE_MAX_SIZE = 20L * 1024 * 1024;
    private static final long SOCIAL_IMAGE_MAX_SIZE = 5L * 1024 * 1024;
    private static final long SOCIAL_FILE_MAX_SIZE = 50L * 1024 * 1024;
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final Set<String> FILE_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "zip"
    );
    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final Set<String> FILE_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain",
            "application/zip",
            "application/x-zip-compressed"
    );
    private static final Set<String> OFFICE_EXTENSIONS = Set.of("doc", "docx", "xls", "xlsx", "ppt", "pptx");
    private static final Set<String> OFFICE_COMPATIBLE_CONTENT_TYPES = Set.of(
            "application/octet-stream",
            "application/zip",
            "application/x-zip-compressed"
    );

    private final FileResourceMapper fileResourceMapper;
    private final Path rootPath;

    public LocalFileStorageService(FileResourceMapper fileResourceMapper, FileStorageProperties properties) {
        this.fileResourceMapper = fileResourceMapper;
        String root = StringUtils.hasText(properties.storageRoot()) ? properties.storageRoot() : "./uploads";
        this.rootPath = Path.of(root).toAbsolutePath().normalize();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadResponse uploadImage(MultipartFile file, AppUserPrincipal principal) {
        return save(file, principal, IMAGE_EXTENSIONS, IMAGE_CONTENT_TYPES, IMAGE_MAX_SIZE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadResponse uploadFile(MultipartFile file, AppUserPrincipal principal) {
        Set<String> extensions = new HashSet<>(IMAGE_EXTENSIONS);
        extensions.addAll(FILE_EXTENSIONS);
        Set<String> contentTypes = new HashSet<>(IMAGE_CONTENT_TYPES);
        contentTypes.addAll(FILE_CONTENT_TYPES);
        return save(file, principal, extensions, contentTypes, FILE_MAX_SIZE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadResponse uploadSocialImage(MultipartFile file, AppUserPrincipal principal) {
        return save(file, principal, IMAGE_EXTENSIONS, IMAGE_CONTENT_TYPES, SOCIAL_IMAGE_MAX_SIZE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadResponse uploadSocialFile(MultipartFile file, AppUserPrincipal principal) {
        Set<String> extensions = new HashSet<>(IMAGE_EXTENSIONS);
        extensions.addAll(FILE_EXTENSIONS);
        Set<String> contentTypes = new HashSet<>(IMAGE_CONTENT_TYPES);
        contentTypes.addAll(FILE_CONTENT_TYPES);
        return save(file, principal, extensions, contentTypes, SOCIAL_FILE_MAX_SIZE);
    }

    @Override
    public FileResource getExistingFile(Long id) {
        if (id == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "文件ID不能为空");
        }
        FileResource file = fileResourceMapper.selectOne(new LambdaQueryWrapper<FileResource>()
                .eq(FileResource::getId, id)
                .eq(FileResource::getDeleted, NOT_DELETED));
        if (file == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "附件不存在：" + id);
        }
        return file;
    }

    @Override
    public Resource loadAsResource(String storagePath) {
        try {
            FileResource file = getByStoragePath(storagePath);
            Path filePath = resolveInsideRoot(file.getStoragePath());
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BusinessException(ApiCode.NOT_FOUND, "文件不存在");
            }
            return resource;
        } catch (IOException exception) {
            throw new BusinessException(ApiCode.NOT_FOUND, "文件不存在");
        }
    }

    @Override
    public FileResource getByStoragePath(String storagePath) {
        String normalizedPath = normalizeStoragePath(storagePath);
        FileResource file = fileResourceMapper.selectOne(new LambdaQueryWrapper<FileResource>()
                .eq(FileResource::getStoragePath, normalizedPath)
                .eq(FileResource::getDeleted, NOT_DELETED));
        if (file == null) {
            throw new BusinessException(ApiCode.NOT_FOUND, "文件不存在");
        }
        return file;
    }

    private UploadResponse save(
            MultipartFile file,
            AppUserPrincipal principal,
            Set<String> allowedExtensions,
            Set<String> allowedContentTypes,
            long maxSize
    ) {
        validateFile(file, allowedExtensions, allowedContentTypes, maxSize);

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename());
        String extension = extractExtension(originalName);
        LocalDate today = LocalDate.now();
        String datePath = "%04d/%02d/%02d".formatted(today.getYear(), today.getMonthValue(), today.getDayOfMonth());
        String storageName = UUID.randomUUID() + "." + extension;
        String storagePath = datePath + "/" + storageName;
        Path targetPath = resolveInsideRoot(storagePath);

        try {
            Files.createDirectories(targetPath.getParent());
            Files.copy(file.getInputStream(), targetPath);
        } catch (IOException exception) {
            throw new BusinessException(ApiCode.ERROR, "文件保存失败");
        }

        String sha256 = calculateSha256(targetPath);
        FileResource resource = new FileResource();
        resource.setOriginalName(originalName);
        resource.setStorageName(storageName);
        resource.setStoragePath(storagePath);
        resource.setUrl("/api/common/files/" + storagePath);
        resource.setContentType(file.getContentType());
        resource.setExtension(extension);
        resource.setSize(file.getSize());
        resource.setSha256(sha256);
        resource.setStorageType("LOCAL");
        resource.setUploaderId(principal.userId());
        resource.setCreateBy(principal.userName());
        resource.setDeleted(NOT_DELETED);
        fileResourceMapper.insert(resource);

        return new UploadResponse(
                resource.getId(),
                resource.getUrl(),
                resource.getOriginalName(),
                resource.getSize(),
                resource.getContentType()
        );
    }

    private void validateFile(
            MultipartFile file,
            Set<String> allowedExtensions,
            Set<String> allowedContentTypes,
            long maxSize
    ) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "上传文件不能为空");
        }
        if (file.getSize() > maxSize) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "上传文件大小超限");
        }
        String originalName = file.getOriginalFilename();
        String extension = extractExtension(originalName);
        if (!allowedExtensions.contains(extension)) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "不支持的文件后缀：" + extension);
        }
        String contentType = normalizeContentType(file.getContentType());
        if (!StringUtils.hasText(contentType) || !isAllowedContentType(extension, contentType, allowedContentTypes)) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "不支持的文件类型");
        }
    }

    private boolean isAllowedContentType(String extension, String contentType, Set<String> allowedContentTypes) {
        if (allowedContentTypes.contains(contentType)) {
            return true;
        }
        return OFFICE_EXTENSIONS.contains(extension) && OFFICE_COMPATIBLE_CONTENT_TYPES.contains(contentType);
    }

    private String normalizeContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return null;
        }
        int semicolonIndex = contentType.indexOf(';');
        String mediaType = semicolonIndex >= 0 ? contentType.substring(0, semicolonIndex) : contentType;
        return mediaType.trim().toLowerCase(Locale.ROOT);
    }

    private String extractExtension(String originalName) {
        if (!StringUtils.hasText(originalName)) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "文件名不能为空");
        }
        String cleaned = StringUtils.cleanPath(originalName);
        if (cleaned.contains("..")) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "文件名不合法");
        }
        int dotIndex = cleaned.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == cleaned.length() - 1) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "文件后缀不能为空");
        }
        return cleaned.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private Path resolveInsideRoot(String storagePath) {
        Path resolved = rootPath.resolve(normalizeStoragePath(storagePath)).normalize();
        if (!resolved.startsWith(rootPath)) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "文件路径不合法");
        }
        return resolved;
    }

    private String normalizeStoragePath(String storagePath) {
        if (!StringUtils.hasText(storagePath) || storagePath.contains("..") || storagePath.startsWith("/")) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "文件路径不合法");
        }
        return storagePath.replace('\\', '/');
    }

    private String calculateSha256(Path path) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream inputStream = Files.newInputStream(path);
                 DigestInputStream digestInputStream = new DigestInputStream(inputStream, digest)) {
                digestInputStream.transferTo(OutputStream.nullOutputStream());
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (IOException | NoSuchAlgorithmException exception) {
            throw new BusinessException(ApiCode.ERROR, "文件摘要计算失败");
        }
    }
}
