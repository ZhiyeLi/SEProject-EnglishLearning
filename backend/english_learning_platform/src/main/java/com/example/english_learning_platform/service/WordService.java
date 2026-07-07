package com.example.english_learning_platform.service;

import com.example.english_learning_platform.entity.CheckinPlan;
import com.example.english_learning_platform.entity.Word;
import com.example.english_learning_platform.entity.WordType;
import com.example.english_learning_platform.entity.UserWordProgress;

import java.util.List;
import java.util.Map;

public interface WordService {

    List<WordType> getWordTypes();

    List<Word> getWordsByType(Long typeId, Integer limit);

    Map<String, Object> getUserWordProgress(Long userId);

    List<Word> getPassedWords(Long userId);

    UserWordProgress markWordPassed(Long userId, Long wordId, Long typeId);

    void unmarkWordPassed(Long userId, Long wordId);

    Map<String, Object> getTodayCheckInStatus(Long userId);

    Map<String, Object> getCheckInStatistics(Long userId);

    Map<String, Object> createCheckInPlan(Long userId, Long typeId, Integer wordsPerDay);

    Map<String, Object> getUserCheckInPlan(Long userId);

    Map<String, Object> getUserCheckInPlanByType(Long userId, Long typeId);

    Long setSelectedWordType(Long userId, Long typeId);

    Long getSelectedWordType(Long userId);

    Word getWordDetail(Long wordId);

    List<Word> getUnpassedWords(Long userId, Long typeId);

    int getConsecutiveCheckInDays(Long userId);

    long getTotalLearnedWords(Long userId);
}