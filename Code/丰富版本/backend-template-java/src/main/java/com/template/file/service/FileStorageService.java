package com.template.file.service;

import com.template.file.entity.FileResource;
import com.template.file.vo.UploadResponse;
import com.template.security.auth.AppUserPrincipal;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务。
 */
public interface FileStorageService {

    /**
     * 上传图片。
     *
     * @param file      文件
     * @param principal 当前用户
     * @return 上传响应
     */
    UploadResponse uploadImage(MultipartFile file, AppUserPrincipal principal);

    /**
     * 上传普通附件。
     *
     * @param file      文件
     * @param principal 当前用户
     * @return 上传响应
     */
    UploadResponse uploadFile(MultipartFile file, AppUserPrincipal principal);

    /**
     * 上传社交图片。
     *
     * @param file      图片文件
     * @param principal 当前用户
     * @return 上传响应
     */
    UploadResponse uploadSocialImage(MultipartFile file, AppUserPrincipal principal);

    /**
     * 上传社交附件。
     *
     * @param file      附件文件
     * @param principal 当前用户
     * @return 上传响应
     */
    UploadResponse uploadSocialFile(MultipartFile file, AppUserPrincipal principal);

    /**
     * 获取文件资源。
     *
     * @param id 文件 ID
     * @return 文件元数据
     */
    FileResource getExistingFile(Long id);

    /**
     * 通过受控路径读取文件。
     *
     * @param storagePath 相对存储路径
     * @return 文件资源
     */
    Resource loadAsResource(String storagePath);

    /**
     * 通过受控路径读取文件元数据。
     *
     * @param storagePath 相对存储路径
     * @return 文件元数据
     */
    FileResource getByStoragePath(String storagePath);
}
