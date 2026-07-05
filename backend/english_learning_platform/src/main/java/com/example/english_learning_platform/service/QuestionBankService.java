package com.example.english_learning_platform.service;

import com.example.english_learning_platform.dto.questionbank.*;

import java.util.Map;

public interface QuestionBankService {

    QuestionBankListResponse<?> getList(Long userId, QuestionBankListRequest request);

    TodayStatsDTO getTodayStats(Long userId);

    Map<String, Long> getTodayStatsByType(Long userId);

    void addFavorite(Long userId, FavoriteRequest request);

    void removeFavorite(Long userId, FavoriteRequest request);

    ExamPaperDetailDTO getExamPaperDetail(Long userId, Long paperId);

    QuestionDetailDTO getQuestionDetail(Long userId, Long questionId);

    SubmitAnswerResponse submitAnswer(Long userId, SubmitAnswerRequest request);

    WrongQuestionsResponse getWrongQuestions(Long userId, String category, String sectionType, String sortBy);

    long getTotalAnsweredQuestions(Long userId);
}