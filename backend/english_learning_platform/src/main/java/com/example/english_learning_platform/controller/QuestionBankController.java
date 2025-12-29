package com.example.english_learning_platform.controller;

import com.example.english_learning_platform.dto.ApiResponse;
import com.example.english_learning_platform.dto.questionbank.*;
import com.example.english_learning_platform.service.QuestionBankService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 题库控制器
 * 提供题库相关的所有 API 接口
 */
@Slf4j
@RestController
@RequestMapping("/api/questionbank")
@RequiredArgsConstructor
public class QuestionBankController {
    
    private final QuestionBankService questionBankService;
    
    /**
     * 1.1 获取列表（考试模式或单题模式）
     * GET /api/questionbank/list
     */
    @GetMapping("/list")
    public ApiResponse<QuestionBankListResponse<?>> getList(
            HttpServletRequest request,
            @RequestParam String mode,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "year_desc") String sortBy,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "all") String category,
            @RequestParam(defaultValue = "all") String sectionType,
            @RequestParam(required = false) java.util.List<String> containsSectionType,
            @RequestParam(defaultValue = "all") String status
    ) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            
            QuestionBankListRequest listRequest = new QuestionBankListRequest();
            listRequest.setMode(mode);
            listRequest.setPage(page);
            listRequest.setPageSize(pageSize);
            listRequest.setSortBy(sortBy);
            listRequest.setKeyword(keyword);
            listRequest.setCategory(category);
            listRequest.setSectionType(sectionType);
            listRequest.setContainsSectionType(containsSectionType);
            listRequest.setStatus(status);
            
            QuestionBankListResponse<?> response = questionBankService.getList(userId, listRequest);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("Failed to get question bank list", e);
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 1.2 获取今日统计
     * GET /api/questionbank/stats/today
     */
    @GetMapping("/stats/today")
    public ApiResponse<TodayStatsDTO> getTodayStats(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            TodayStatsDTO stats = questionBankService.getTodayStats(userId);
            return ApiResponse.success(stats);
        } catch (Exception e) {
            log.error("Failed to get today stats", e);
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 1.3 添加收藏
     * POST /api/questionbank/favorite
     */
    @PostMapping("/favorite")
    public ApiResponse<String> addFavorite(
            HttpServletRequest request,
            @RequestBody FavoriteRequest favoriteRequest
    ) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            questionBankService.addFavorite(userId, favoriteRequest);
            return ApiResponse.success("收藏成功", null);
        } catch (Exception e) {
            log.error("Failed to add favorite", e);
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 1.4 取消收藏
     * DELETE /api/questionbank/favorite
     */
    @DeleteMapping("/favorite")
    public ApiResponse<String> removeFavorite(
            HttpServletRequest request,
            @RequestBody FavoriteRequest favoriteRequest
    ) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            questionBankService.removeFavorite(userId, favoriteRequest);
            return ApiResponse.success("取消收藏成功", null);
        } catch (Exception e) {
            log.error("Failed to remove favorite", e);
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 2.1 获取试卷详情（考试模式）
     * GET /api/questionbank/exam/{paperId}
     */
    @GetMapping("/exam/{paperId}")
    public ApiResponse<ExamPaperDetailDTO> getExamPaperDetail(
            HttpServletRequest request,
            @PathVariable Long paperId
    ) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            ExamPaperDetailDTO detail = questionBankService.getExamPaperDetail(userId, paperId);
            return ApiResponse.success(detail);
        } catch (Exception e) {
            log.error("Failed to get exam paper detail", e);
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 2.2 获取题目详情（单题模式）
     * GET /api/questionbank/question/{questionId}
     */
    @GetMapping("/question/{questionId}")
    public ApiResponse<QuestionDetailDTO> getQuestionDetail(
            HttpServletRequest request,
            @PathVariable Long questionId
    ) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            QuestionDetailDTO detail = questionBankService.getQuestionDetail(userId, questionId);
            return ApiResponse.success(detail);
        } catch (Exception e) {
            log.error("Failed to get question detail", e);
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 2.3 提交答案
     * POST /api/questionbank/submit
     */
    @PostMapping("/submit")
    public ApiResponse<SubmitAnswerResponse> submitAnswer(
            HttpServletRequest request,
            @RequestBody SubmitAnswerRequest submitRequest
    ) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            SubmitAnswerResponse response = questionBankService.submitAnswer(userId, submitRequest);
            return ApiResponse.success("提交成功", response);
        } catch (Exception e) {
            log.error("Failed to submit answer", e);
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 3.1 获取错题列表
     * GET /api/questionbank/wrong
     */
    @GetMapping("/wrong")
    public ApiResponse<WrongQuestionsResponse> getWrongQuestions(
            HttpServletRequest request,
            @RequestParam(defaultValue = "all") String category,
            @RequestParam(defaultValue = "all") String sectionType,
            @RequestParam(defaultValue = "recent") String sortBy
    ) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            WrongQuestionsResponse response = questionBankService.getWrongQuestions(
                userId, category, sectionType, sortBy);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("Failed to get wrong questions", e);
            return ApiResponse.error(e.getMessage());
        }
    }
}
