package com.example.english_learning_platform.service.impl;

import com.example.english_learning_platform.service.FileService;
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
 * 文件服务实现
 * 处理文件上传、存储和访问
 */
@Service
public class FileServiceImpl implements FileService {

    @Value("${file.upload.base-path}")
    private String basePath;

    @Value("${file.upload.audio-path}")
    private String audioPath;

    @Value("${file.upload.image-path}")
    private String imagePath;

    @Override
    public String uploadAudio(MultipartFile file) throws IOException {
        return uploadFile(file, audioPath, "audio");
    }

    @Override
    public String uploadImage(MultipartFile file) throws IOException {
        return uploadFile(file, imagePath, "images");
    }

    /**
     * 通用文件上传私有方法
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
     * 获取文件扩展名（私有工具）
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex);
        }
        return "";
    }

    @Override
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

    @Override
    public boolean fileExists(String fileUrl) {
        try {
            String filePath = fileUrl.replace("/api/files/", "");
            Path fullPath = Paths.get(basePath, filePath);
            return Files.exists(fullPath);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isValidAudioFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) return false;

        String extension = getFileExtension(filename).toLowerCase();
        return extension.equals(".mp3") || extension.equals(".wav") ||
                extension.equals(".ogg") || extension.equals(".m4a");
    }

    @Override
    public boolean isValidImageFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) return false;

        String extension = getFileExtension(filename).toLowerCase();
        return extension.equals(".jpg") || extension.equals(".jpeg") ||
                extension.equals(".png") || extension.equals(".gif") ||
                extension.equals(".webp");
    }
}