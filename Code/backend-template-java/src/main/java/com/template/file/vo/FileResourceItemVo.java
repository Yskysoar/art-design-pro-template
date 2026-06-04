package com.template.file.vo;

/**
 * 文件资源管理列表行数据。
 *
 * @param id             文件 ID
 * @param originalName   原始文件名
 * @param url            受控访问 URL
 * @param contentType    MIME 类型
 * @param extension      文件后缀
 * @param size           文件大小，单位字节
 * @param sha256         SHA-256 摘要
 * @param storageType    存储类型
 * @param uploaderId     上传人 ID
 * @param uploaderName   上传人显示名
 * @param createBy       创建人账号
 * @param createTime     创建时间
 * @param referenced     是否已被业务引用
 * @param referenceCount 引用数量
 */
public record FileResourceItemVo(
        Long id,
        String originalName,
        String url,
        String contentType,
        String extension,
        Long size,
        String sha256,
        String storageType,
        Long uploaderId,
        String uploaderName,
        String createBy,
        String createTime,
        Boolean referenced,
        Long referenceCount
) {
}
