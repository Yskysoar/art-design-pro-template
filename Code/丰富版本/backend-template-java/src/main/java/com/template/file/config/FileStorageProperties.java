package com.template.file.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件存储配置。
 *
 * @param storageRoot 本地文件存储根目录
 */
@ConfigurationProperties(prefix = "app.file")
public record FileStorageProperties(String storageRoot) {
}
