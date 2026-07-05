package com.example.english_learning_platform.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {

    /**
     * 上传音频文件
     * @param file 上传的文件
     * @return 文件访问 URL
     */
    String uploadAudio(MultipartFile file) throws IOException;

    /**
     * 上传图片文件
     * @param file 上传的文件
     * @return 文件访问 URL
     */
    String uploadImage(MultipartFile file) throws IOException;

    /**
     * 删除文件
     * @param fileUrl 文件 URL
     * @return 是否删除成功
     */
    boolean deleteFile(String fileUrl);

    /**
     * 检查文件是否存在
     * @param fileUrl 文件 URL
     * @return 是否存在
     */
    boolean fileExists(String fileUrl);

    /**
     * 验证音频文件格式
     * @param file 文件
     * @return 是否为有效的音频格式
     */
    boolean isValidAudioFile(MultipartFile file);

    /**
     * 验证图片文件格式
     * @param file 文件
     * @return 是否为有效的图片格式
     */
    boolean isValidImageFile(MultipartFile file);
}