package com.example.english_learning_platform.controller;

import com.example.english_learning_platform.dto.ApiResponse;
import com.example.english_learning_platform.dto.CourseDTO;
import com.example.english_learning_platform.dto.CourseDetailDTO;
import com.example.english_learning_platform.service.CourseService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ApiResponse<Page<CourseDTO>> getCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Page<CourseDTO> courses = courseService.getCourses(keyword, tag, page, size, userId);
            return ApiResponse.success(courses);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<CourseDetailDTO> getCourseById(
            @PathVariable Long id,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            CourseDetailDTO course = courseService.getCourseById(id, userId);
            return ApiResponse.success(course);
        } catch (Exception e) {
            return ApiResponse.notFound(e.getMessage());
        }
    }

    @GetMapping("/progress")
    public ApiResponse<List<CourseDTO>> getUserProgress(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Page<CourseDTO> all = courseService.getCourses(null, null, 0, Integer.MAX_VALUE, userId);
            return ApiResponse.success(all.getContent());
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/progress")
    public ApiResponse<String> updateProgress(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            String status = body.get("status");
            courseService.updateProgress(userId, id, status);
            return ApiResponse.success("进度更新成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<String> markComplete(
            @PathVariable Long id,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            courseService.markComplete(userId, id);
            return ApiResponse.success("标记完成");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/favorite")
    public ApiResponse<Map<String, Object>> toggleFavorite(
            @PathVariable Long id,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            boolean isFav = courseService.toggleFavorite(userId, id);
            return ApiResponse.success(Map.of("favorite", isFav));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/favorites")
    public ApiResponse<List<Long>> getFavoriteIds(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            List<Long> ids = courseService.getFavoriteIds(userId);
            return ApiResponse.success(ids);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/cover")
    public ApiResponse<Map<String, String>> resolveCover(@RequestParam String bvid) {
        try {
            String cover = courseService.resolveBilibiliCover(bvid, "");
            return ApiResponse.success(Map.of("cover", cover));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
