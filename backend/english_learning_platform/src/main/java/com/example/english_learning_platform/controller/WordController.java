package com.example.english_learning_platform.controller;

import com.example.english_learning_platform.dto.ApiResponse;
import com.example.english_learning_platform.entity.*;
import com.example.english_learning_platform.service.WordService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/words")
public class WordController {
    
    private final WordService wordService;
    
    public WordController(WordService wordService) {
        this.wordService = wordService;
    }
    
    @GetMapping("/types")
    public ApiResponse<List<WordType>> getWordTypes() {
        try {
            List<WordType> types = wordService.getWordTypes();
            return ApiResponse.success(types);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/list")
    public ApiResponse<List<Word>> getWordsByType(@RequestParam Long typeId, @RequestParam(required = false) Integer limit) {
        try {
            List<Word> words = wordService.getWordsByType(typeId, limit);
            return ApiResponse.success(words);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/progress")
    public ApiResponse<Map<String, Object>> getUserWordProgress(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Map<String, Object> progress = wordService.getUserWordProgress(userId);
            return ApiResponse.success(progress);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/passed")
    public ApiResponse<List<UserWordProgress>> getPassedWords(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            List<UserWordProgress> words = wordService.getPassedWords(userId);
            return ApiResponse.success(words);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/mark-passed")
    public ApiResponse<UserWordProgress> markWordPassed(HttpServletRequest request, @RequestBody Map<String, Long> data) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Long wordId = data.get("wordId");
            Long typeId = data.get("typeId");
            UserWordProgress progress = wordService.markWordPassed(userId, wordId, typeId);
            return ApiResponse.success(progress);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/unmark-passed")
    public ApiResponse<String> unmarkWordPassed(HttpServletRequest request, @RequestBody Map<String, Long> data) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Long wordId = data.get("wordId");
            wordService.unmarkWordPassed(userId, wordId);
            return ApiResponse.success("取消标记成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/today-status")
    public ApiResponse<Map<String, Object>> getTodayCheckInStatus(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Map<String, Object> status = wordService.getTodayCheckInStatus(userId);
            return ApiResponse.success(status);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getCheckInStatistics(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Map<String, Object> stats = wordService.getCheckInStatistics(userId);
            return ApiResponse.success(stats);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/plan")
    public ApiResponse<CheckinPlan> createCheckInPlan(HttpServletRequest request, @RequestBody Map<String, Object> data) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Long typeId = Long.parseLong(data.get("typeId").toString());
            Integer wordsPerDay = Integer.parseInt(data.get("wordsPerDay").toString());
            CheckinPlan plan = wordService.createCheckInPlan(userId, typeId, wordsPerDay);
            return ApiResponse.success(plan);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/plan/detail")
    public ApiResponse<CheckinPlan> getUserCheckInPlan(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            CheckinPlan plan = wordService.getUserCheckInPlan(userId);
            return ApiResponse.success(plan);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/set-selected-type")
    public ApiResponse<Long> setSelectedWordType(HttpServletRequest request, @RequestBody Map<String, Object> data) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            // 兼容前端可能传过来的不同类型 (Integer/String/Long)
            Long typeId = Long.parseLong(data.get("typeId").toString());
            Long result = wordService.setSelectedWordType(userId, typeId);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/selected-type")
    public ApiResponse<Long> getSelectedWordType(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Long typeId = wordService.getSelectedWordType(userId);
            return ApiResponse.success(typeId);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
