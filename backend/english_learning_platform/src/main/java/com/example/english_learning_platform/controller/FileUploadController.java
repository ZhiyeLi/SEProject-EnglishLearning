package com.example.english_learning_platform.controller;

import com.example.english_learning_platform.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器
 * 处理音频和图片文件的上传请求
 */
@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileUploadController {

    @Autowired
    private FileService fileService;

    /**
     * 上传音频文件
     * @param file 音频文件
     * @return 上传结果和文件 URL
     */
    @PostMapping("/audio")
    public ResponseEntity<Map<String, Object>> uploadAudio(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 验证文件格式
            if (!fileService.isValidAudioFile(file)) {
                response.put("success", false);
                response.put("message", "不支持的音频格式，请上传 mp3、wav、ogg 或 m4a 格式的文件");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // 验证文件大小（限制为 50MB）
            if (file.getSize() > 50 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "文件大小不能超过 50MB");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // 上传文件
            String fileUrl = fileService.uploadAudio(file);
            
            response.put("success", true);
            response.put("message", "音频上传成功");
            response.put("url", fileUrl);
            response.put("filename", file.getOriginalFilename());
            response.put("size", file.getSize());
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "文件上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 上传图片文件
     * @param file 图片文件
     * @return 上传结果和文件 URL
     */
    @PostMapping("/images")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 验证文件格式
            if (!fileService.isValidImageFile(file)) {
                response.put("success", false);
                response.put("message", "不支持的图片格式，请上传 jpg、jpeg、png、gif 或 webp 格式的文件");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // 验证文件大小（限制为 10MB）
            if (file.getSize() > 10 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "文件大小不能超过 10MB");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // 上传文件
            String fileUrl = fileService.uploadImage(file);
            
            response.put("success", true);
            response.put("message", "图片上传成功");
            response.put("url", fileUrl);
            response.put("filename", file.getOriginalFilename());
            response.put("size", file.getSize());
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "文件上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 删除文件
     * @param fileUrl 文件 URL
     * @return 删除结果
     */
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deleteFile(@RequestParam("url") String fileUrl) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean deleted = fileService.deleteFile(fileUrl);
            
            if (deleted) {
                response.put("success", true);
                response.put("message", "文件删除成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "文件不存在或删除失败");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "文件删除失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 检查文件是否存在
     * @param fileUrl 文件 URL
     * @return 文件存在状态
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkFile(@RequestParam("url") String fileUrl) {
        Map<String, Object> response = new HashMap<>();
        
        boolean exists = fileService.fileExists(fileUrl);
        response.put("success", true);
        response.put("exists", exists);
        response.put("url", fileUrl);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 批量上传音频文件
     * @param files 音频文件数组
     * @return 上传结果列表
     */
    @PostMapping("/audio/batch")
    public ResponseEntity<Map<String, Object>> uploadAudioBatch(@RequestParam("files") MultipartFile[] files) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> uploadedFiles = new HashMap<>();
        Map<String, String> failedFiles = new HashMap<>();
        
        for (MultipartFile file : files) {
            try {
                if (!fileService.isValidAudioFile(file)) {
                    failedFiles.put(file.getOriginalFilename(), "不支持的音频格式");
                    continue;
                }
                
                if (file.getSize() > 50 * 1024 * 1024) {
                    failedFiles.put(file.getOriginalFilename(), "文件大小超过 50MB");
                    continue;
                }
                
                String fileUrl = fileService.uploadAudio(file);
                uploadedFiles.put(file.getOriginalFilename(), fileUrl);
                
            } catch (Exception e) {
                failedFiles.put(file.getOriginalFilename(), e.getMessage());
            }
        }
        
        response.put("success", true);
        response.put("uploaded", uploadedFiles);
        response.put("failed", failedFiles);
        response.put("total", files.length);
        response.put("successCount", uploadedFiles.size());
        response.put("failCount", failedFiles.size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 批量上传图片文件
     * @param files 图片文件数组
     * @return 上传结果列表
     */
    @PostMapping("/images/batch")
    public ResponseEntity<Map<String, Object>> uploadImageBatch(@RequestParam("files") MultipartFile[] files) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> uploadedFiles = new HashMap<>();
        Map<String, String> failedFiles = new HashMap<>();
        
        for (MultipartFile file : files) {
            try {
                if (!fileService.isValidImageFile(file)) {
                    failedFiles.put(file.getOriginalFilename(), "不支持的图片格式");
                    continue;
                }
                
                if (file.getSize() > 10 * 1024 * 1024) {
                    failedFiles.put(file.getOriginalFilename(), "文件大小超过 10MB");
                    continue;
                }
                
                String fileUrl = fileService.uploadImage(file);
                uploadedFiles.put(file.getOriginalFilename(), fileUrl);
                
            } catch (Exception e) {
                failedFiles.put(file.getOriginalFilename(), e.getMessage());
            }
        }
        
        response.put("success", true);
        response.put("uploaded", uploadedFiles);
        response.put("failed", failedFiles);
        response.put("total", files.length);
        response.put("successCount", uploadedFiles.size());
        response.put("failCount", failedFiles.size());
        
        return ResponseEntity.ok(response);
    }
}
