package com.template.file.vo;

/**
 * wangEditor 上传响应。
 *
 * @param errno 错误码，0 表示成功
 * @param data  图片数据
 */
public record WangEditorUploadResponse(int errno, Data data) {

    /**
     * wangEditor 图片数据。
     *
     * @param url  图片 URL
     * @param alt  替代文本
     * @param href 点击跳转 URL
     */
    public record Data(String url, String alt, String href) {
    }
}
