package com.freecanvas.service;

import java.io.InputStream;

/**
 * 云存储服务接口
 * 用于上传 AI 生成的图片/视频到火山 TOS，或生成预览 URL
 */
public interface CloudStorageService {

    /**
     * 上传文件到云存储
     * @param objectKey 对象键（路径）
     * @param inputStream 文件流
     * @param contentType MIME 类型
     * @return 公开访问 URL
     */
    String upload(String objectKey, InputStream inputStream, String contentType);

    /**
     * 生成预签名下载 URL
     * @param objectKey 对象键
     * @return 预签名 URL（有效期由配置决定）
     */
    String getPresignedUrl(String objectKey);

    /**
     * 获取公开访问 URL
     * @param objectKey 对象键
     * @return 公开 URL
     */
    String getPublicUrl(String objectKey);

    /**
     * 删除对象
     * @param objectKey 对象键
     */
    void delete(String objectKey);
}
