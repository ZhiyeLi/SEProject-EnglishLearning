package com.example.english_learning_platform.service;

import com.example.english_learning_platform.dto.CourseDTO;
import com.example.english_learning_platform.dto.CourseDetailDTO;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CourseService {

    Page<CourseDTO> getCourses(String keyword, String tag, int page, int size, Long userId);

    CourseDetailDTO getCourseById(Long courseId, Long userId);

    void updateProgress(Long userId, Long courseId, String status);

    void markComplete(Long userId, Long courseId);

    boolean toggleFavorite(Long userId, Long courseId);

    List<Long> getFavoriteIds(Long userId);

    @Transactional
    void seedCourseData();

    String resolveCoverUrl(String coverImage, String videoUrl);

    String resolveBilibiliCover(String bvid, String fallback);
}
