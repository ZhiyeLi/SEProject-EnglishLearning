package com.example.english_learning_platform.service;

import com.example.english_learning_platform.entity.*;
import com.example.english_learning_platform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class WordService {
    
    private final WordRepository wordRepository;
    private final WordTypeRepository wordTypeRepository;
    private final UserWordProgressRepository userWordProgressRepository;
    private final DailyStudyRecordRepository dailyStudyRecordRepository;
    private final CheckinPlanRepository checkinPlanRepository;
    private final UserSelectedTypeRepository userSelectedTypeRepository;
    
    public WordService(WordRepository wordRepository,
                      WordTypeRepository wordTypeRepository,
                      UserWordProgressRepository userWordProgressRepository,
                      DailyStudyRecordRepository dailyStudyRecordRepository,
                      CheckinPlanRepository checkinPlanRepository,
                      UserSelectedTypeRepository userSelectedTypeRepository) {
        this.wordRepository = wordRepository;
        this.wordTypeRepository = wordTypeRepository;
        this.userWordProgressRepository = userWordProgressRepository;
        this.dailyStudyRecordRepository = dailyStudyRecordRepository;
        this.checkinPlanRepository = checkinPlanRepository;
        this.userSelectedTypeRepository = userSelectedTypeRepository;
    }
    
    public List<WordType> getWordTypes() {
        return wordTypeRepository.findAll();
    }
    
    public List<Word> getWordsByType(Long typeId, Integer limit) {
        if (limit != null && limit > 0) {
            return wordRepository.findByTypeId(typeId).stream()
                    .limit(limit)
                    .toList();
        }
        return wordRepository.findByTypeId(typeId);
    }
    
    public Map<String, Object> getUserWordProgress(Long userId) {
        List<UserWordProgress> passedWords = userWordProgressRepository
                .findByUserIdAndPassedDateIsNotNull(userId);
        
        Map<String, Object> progress = new HashMap<>();
        progress.put("totalPassed", passedWords.size());
        progress.put("words", passedWords);
        
        return progress;
    }
    
    public List<UserWordProgress> getPassedWords(Long userId) {
        return userWordProgressRepository.findByUserIdAndPassedDateIsNotNull(userId);
    }
    
    @Transactional
    public UserWordProgress markWordPassed(Long userId, Long wordId, Long typeId) {
        Optional<UserWordProgress> existing = userWordProgressRepository
                .findByUserIdAndWordId(userId, wordId);
        
        UserWordProgress progress;
        if (existing.isPresent()) {
            progress = existing.get();
        } else {
            progress = new UserWordProgress();
            progress.setUserId(userId);
            progress.setWordId(wordId);
            progress.setTypeId(typeId);
        }
        
        progress.setPassedDate(LocalDate.now());
        progress.setStage(5); // 假设5表示已掌握
        
        return userWordProgressRepository.save(progress);
    }
    
    @Transactional
    public void unmarkWordPassed(Long userId, Long wordId) {
        Optional<UserWordProgress> progress = userWordProgressRepository
                .findByUserIdAndWordId(userId, wordId);
        progress.ifPresent(p -> {
            p.setPassedDate(null);
            p.setStage(0);
            userWordProgressRepository.save(p);
        });
    }
    
    public Map<String, Object> getTodayCheckInStatus(Long userId) {
        LocalDate today = LocalDate.now();
        Optional<DailyStudyRecord> record = dailyStudyRecordRepository
                .findByUserIdAndStudyDate(userId, today);
        
        Map<String, Object> status = new HashMap<>();
        status.put("hasCheckedIn", record.isPresent());
        status.put("date", today);
        if (record.isPresent()) {
            status.put("record", record.get());
        }
        
        return status;
    }
    
    public Map<String, Object> getCheckInStatistics(Long userId) {
        List<DailyStudyRecord> records = dailyStudyRecordRepository
                .findByUserIdOrderByStudyDateDesc(userId);
        
        int totalDays = records.size();
        int totalWords = records.stream()
                .mapToInt(DailyStudyRecord::getTotalWords)
                .sum();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDays", totalDays);
        stats.put("totalWords", totalWords);
        stats.put("records", records);
        
        return stats;
    }
    
    @Transactional
    public CheckinPlan createCheckInPlan(Long userId, Long typeId, Integer wordsPerDay) {
        CheckinPlan plan = new CheckinPlan();
        plan.setUserId(userId);
        plan.setTypeId(typeId);
        plan.setWordsPerDay(wordsPerDay);
        plan.setStartDate(LocalDate.now());
        plan.setStatus("active");
        
        return checkinPlanRepository.save(plan);
    }
    
    public CheckinPlan getUserCheckInPlan(Long userId) {
        return checkinPlanRepository.findByUserIdAndStatus(userId, "active")
                .orElse(null);
    }
    
    @Transactional
    public Long setSelectedWordType(Long userId, Long typeId) {
        userSelectedTypeRepository.deleteByUserId(userId);
        
        UserSelectedType selected = new UserSelectedType();
        selected.setUserId(userId);
        selected.setTypeId(typeId);
        
        userSelectedTypeRepository.save(selected);
        return typeId;
    }
    
    public Long getSelectedWordType(Long userId) {
        return userSelectedTypeRepository.findTopByUserIdOrderBySelectedDateDesc(userId)
                .map(UserSelectedType::getTypeId)
                .orElse(null);
    }
}
