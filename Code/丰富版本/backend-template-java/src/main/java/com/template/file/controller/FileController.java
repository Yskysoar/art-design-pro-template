package com.template.file.controller;

import com.template.common.response.ApiResponse;
import com.template.file.entity.FileResource;
import com.template.file.service.FileAccessService;
import com.template.file.service.FileStorageService;
import com.template.file.vo.UploadResponse;
import com.template.file.vo.WangEditorUploadResponse;
import com.template.security.auth.AppUserPrincipal;
import com.template.security.permission.PermissionService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传与访问接口。
 */
@RestController
@RequestMapping("/api/common")
public class FileController {

    private static final String ARTICLE_UPLOAD_PERMISSION = "article:upload";

    private final FileStorageService fileStorageService;
    private final FileAccessService fileAccessService;
    private final PermissionService permissionService;

    public FileController(
            FileStorageService fileStorageService,
            FileAccessService fileAccessService,
            PermissionService permissionService
    ) {
        this.fileStorageService = fileStorageService;
        this.fileAccessService = fileAccessService;
        this.permissionService = permissionService;
    }

    /**
     * 通用图片上传。
     *
     * @param file      上传文件
     * @param principal 当前用户
     * @return 上传响应
     */
    @PostMapping("/upload")
    public ApiResponse<UploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, ARTICLE_UPLOAD_PERMISSION);
        return ApiResponse.success(fileStorageService.uploadImage(file, principal));
    }

    /**
     * 图片上传。
     *
     * @param file      上传文件
     * @param principal 当前用户
     * @return 上传响应
     */
    @PostMapping("/upload/image")
    public ApiResponse<UploadResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, ARTICLE_UPLOAD_PERMISSION);
        return ApiResponse.success(fileStorageService.uploadImage(file, principal));
    }

    /**
     * 普通附件上传。
     *
     * @param file      上传文件
     * @param principal 当前用户
     * @return 上传响应
     */
    @PostMapping("/upload/file")
    public ApiResponse<UploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, ARTICLE_UPLOAD_PERMISSION);
        return ApiResponse.success(fileStorageService.uploadFile(file, principal));
    }

    /**
     * wangEditor 图片上传。
     *
     * @param file      上传文件
     * @param principal 当前用户
     * @return wangEditor 响应
     */
    @PostMapping("/upload/wangeditor")
    public WangEditorUploadResponse uploadWangEditor(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        permissionService.requirePermission(principal, ARTICLE_UPLOAD_PERMISSION);
        UploadResponse response = fileStorageService.uploadImage(file, principal);
        return new WangEditorUploadResponse(
                0,
                new WangEditorUploadResponse.Data(response.url(), response.fileName(), response.url())
        );
    }

    /**
     * 受控读取上传文件。
     *
     * @param year 年份
     * @param month 月份
     * @param day 日期
     * @param fileName 文件名
     * @return 文件内容
     */
    @GetMapping("/files/{year}/{month}/{day}/{fileName}")
    public ResponseEntity<Resource> getFile(
            @PathVariable String year,
            @PathVariable String month,
            @PathVariable String day,
            @PathVariable String fileName,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        String storagePath = "%s/%s/%s/%s".formatted(year, month, day, fileName);
        FileResource file = fileStorageService.getByStoragePath(storagePath);
        fileAccessService.requireReadable(file, principal);
        Resource resource = fileStorageService.loadAsResource(storagePath);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getOriginalName() + "\"")
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .body(resource);
    }
}
