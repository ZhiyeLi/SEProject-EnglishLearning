package com.example.english_learning_platform.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 文件服务
 * 处理文件上传、存储和访问
 */
@Service
public class FileService {

    @Value("${file.upload.base-path}")
    private String basePath;

    @Value("${file.upload.audio-path}")
    private String audioPath;

    @Value("${file.upload.image-path}")
    private String imagePath;

    /**
     * 上传音频文件
     * @param file 上传的文件
     * @return 文件访问 URL
     */
    public String uploadAudio(MultipartFile file) throws IOException {
        return uploadFile(file, audioPath, "audio");
    }

    /**
     * 上传图片文件
     * @param file 上传的文件
     * @return 文件访问 URL
     */
    public String uploadImage(MultipartFile file) throws IOException {
        return uploadFile(file, imagePath, "images");
    }

    /**
     * 通用文件上传方法
     * @param file 上传的文件
     * @param relativePath 相对路径
     * @param urlPath URL 路径部分
     * @return 文件访问 URL
     */
    private String uploadFile(MultipartFile file, String relativePath, String urlPath) throws IOException {
        // 检查文件是否为空
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        // 获取文件扩展名
        String extension = getFileExtension(originalFilename);
        
        // 生成唯一文件名
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        // 构建完整的存储路径
        Path uploadPath = Paths.get(basePath, relativePath);
        
        // 如果目录不存在，创建目录
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 保存文件
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 返回访问 URL
        return "/api/files/" + urlPath + "/" + uniqueFilename;
    }

    /**
     * 获取文件扩展名
     * @param filename 文件名
     * @return 扩展名（包含点号）
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex);
        }
        return "";
    }

    /**
     * 删除文件
     * @param fileUrl 文件 URL
     * @return 是否删除成功
     */
    public boolean deleteFile(String fileUrl) {
        try {
            // 从 URL 中提取文件路径
            // 例如：/api/files/audio/test.mp3 -> audio/test.mp3
            String filePath = fileUrl.replace("/api/files/", "");
            Path fullPath = Paths.get(basePath, filePath);
            
            if (Files.exists(fullPath)) {
                Files.delete(fullPath);
                return true;
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 检查文件是否存在
     * @param fileUrl 文件 URL
     * @return 是否存在
     */
    public boolean fileExists(String fileUrl) {
        try {
            String filePath = fileUrl.replace("/api/files/", "");
            Path fullPath = Paths.get(basePath, filePath);
            return Files.exists(fullPath);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证音频文件格式
     * @param file 文件
     * @return 是否为有效的音频格式
     */
    public boolean isValidAudioFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) return false;
        
        String extension = getFileExtension(filename).toLowerCase();
        return extension.equals(".mp3") || extension.equals(".wav") || 
               extension.equals(".ogg") || extension.equals(".m4a");
    }

    /**
     * 验证图片文件格式
     * @param file 文件
     * @return 是否为有效的图片格式
     */
    public boolean isValidImageFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) return false;
        
        String extension = getFileExtension(filename).toLowerCase();
        return extension.equals(".jpg") || extension.equals(".jpeg") || 
               extension.equals(".png") || extension.equals(".gif") || 
               extension.equals(".webp");
    }
}
