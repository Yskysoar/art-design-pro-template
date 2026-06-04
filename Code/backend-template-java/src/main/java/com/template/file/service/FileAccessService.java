package com.template.file.service;

import com.template.file.entity.FileResource;
import com.template.security.auth.AppUserPrincipal;

/**
 * 文件访问权限服务。
 */
public interface FileAccessService {

    /**
     * 校验当前用户是否可以读取指定文件。
     *
     * @param file      文件资源
     * @param principal 当前用户，匿名访问时为空
     */
    void requireReadable(FileResource file, AppUserPrincipal principal);
}
