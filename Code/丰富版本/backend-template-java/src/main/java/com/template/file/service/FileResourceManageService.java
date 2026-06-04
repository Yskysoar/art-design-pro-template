package com.template.file.service;

import com.template.common.pagination.PageResult;
import com.template.file.dto.FileResourceListQuery;
import com.template.file.vo.FileResourceItemVo;

/**
 * 文件资源管理服务。
 */
public interface FileResourceManageService {

    /**
     * 分页查询文件资源台账。
     *
     * @param query 查询参数
     * @return 文件资源分页
     */
    PageResult<FileResourceItemVo> pageResources(FileResourceListQuery query);

    /**
     * 删除未被业务引用的文件资源。
     *
     * @param id 文件 ID
     */
    void deleteResource(Long id);
}
