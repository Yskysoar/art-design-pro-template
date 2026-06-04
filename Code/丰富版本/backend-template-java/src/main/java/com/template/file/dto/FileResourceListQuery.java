package com.template.file.dto;

/**
 * 文件资源分页查询参数。
 *
 * @param current         当前页码
 * @param size            每页条数
 * @param keyword         文件名、URL 或摘要关键字
 * @param extension       文件后缀
 * @param contentType     MIME 类型
 * @param storageType     存储类型
 * @param uploaderId      上传人 ID
 * @param referenced      是否已被业务引用
 * @param minSize         最小文件大小，单位字节
 * @param maxSize         最大文件大小，单位字节
 * @param createTimeStart 创建开始时间，格式 yyyy-MM-dd HH:mm:ss
 * @param createTimeEnd   创建结束时间，格式 yyyy-MM-dd HH:mm:ss
 */
public record FileResourceListQuery(
        Long current,
        Long size,
        String keyword,
        String extension,
        String contentType,
        String storageType,
        Long uploaderId,
        Boolean referenced,
        Long minSize,
        Long maxSize,
        String createTimeStart,
        String createTimeEnd
) {
}
