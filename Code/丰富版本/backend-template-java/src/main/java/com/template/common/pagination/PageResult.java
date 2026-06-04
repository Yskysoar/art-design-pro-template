package com.template.common.pagination;

import java.util.List;

/**
 * 前端表格分页响应结构。
 *
 * @param records 当前页数据
 * @param current 当前页码
 * @param size    每页条数
 * @param total   总条数
 * @param <T>     行数据类型
 */
public record PageResult<T>(List<T> records, long current, long size, long total) {
}
