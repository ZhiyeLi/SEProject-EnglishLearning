package com.example.english_learning_platform.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {

    String uploadAudio(MultipartFile file) throws IOException;

    String uploadImage(MultipartFile file) throws IOException;

    boolean deleteFile(String fileUrl);

    boolean fileExists(String fileUrl);

    boolean isValidAudioFile(MultipartFile file);

    boolean isValidImageFile(MultipartFile file);
}
