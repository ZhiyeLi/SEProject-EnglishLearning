package com.example.english_learning_platform.controller;

import com.example.english_learning_platform.dto.ApiResponse;
import com.example.english_learning_platform.entity.*;
import com.example.english_learning_platform.service.QuestionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {
    
    private final QuestionService questionService;
    
    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }
    
    @GetMapping
    public ApiResponse<List<Question>> getQuestions(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String difficulty) {
        try {
            List<Question> questions = questionService.getQuestions(type, difficulty);
            return ApiResponse.success(questions);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getQuestionDetail(
            HttpServletRequest request,
            @PathVariable Long id) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Map<String, Object> detail = questionService.getQuestionDetail(id, userId);
            return ApiResponse.success(detail);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/{id}/favorite")
    public ApiResponse<UserQuestionRecord> toggleFavorite(
            HttpServletRequest request,
            @PathVariable Long id) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            UserQuestionRecord record = questionService.toggleFavorite(userId, id);
            return ApiResponse.success(record);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getStatistics(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Map<String, Object> stats = questionService.getStatistics(userId);
            return ApiResponse.success(stats);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/wrong/list")
    public ApiResponse<List<UserQuestionRecord>> getWrongQuestions(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            List<UserQuestionRecord> questions = questionService.getWrongQuestions(userId);
            return ApiResponse.success(questions);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/courses/list")
    public ApiResponse<List<Course>> getCourses() {
        try {
            List<Course> courses = questionService.getCourses();
            return ApiResponse.success(courses);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/courses/{courseId}/questions")
    public ApiResponse<List<Question>> getCourseQuestions(@PathVariable String courseId) {
        try {
            List<Question> questions = questionService.getCourseQuestions(courseId);
            return ApiResponse.success(questions);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/vocabulary/user")
    public ApiResponse<List<UserVocabulary>> getUserVocabulary(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            List<UserVocabulary> vocabulary = questionService.getUserVocabulary(userId);
            return ApiResponse.success(vocabulary);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/vocabulary/add")
    public ApiResponse<UserVocabulary> addVocabulary(
            HttpServletRequest request,
            @RequestBody Map<String, Object> data) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Long wordId = Long.parseLong(data.get("wordId").toString());
            String translation = (String) data.get("translation");
            String questionId = (String) data.get("questionId");
            
            UserVocabulary vocabulary = questionService.addVocabulary(userId, wordId, translation, questionId);
            return ApiResponse.success(vocabulary);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
