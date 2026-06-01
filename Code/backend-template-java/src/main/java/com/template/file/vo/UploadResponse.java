package com.template.file.vo;

/**
 * 上传接口响应。
 *
 * @param id          文件 ID
 * @param url         访问 URL
 * @param fileName    原始文件名
 * @param size        文件大小
 * @param contentType MIME 类型
 */
public record UploadResponse(
        Long id,
        String url,
        String fileName,
        Long size,
        String contentType
) {
}
