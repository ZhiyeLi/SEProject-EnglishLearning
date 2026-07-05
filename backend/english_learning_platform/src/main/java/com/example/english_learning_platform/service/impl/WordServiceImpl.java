package com.example.english_learning_platform.service.impl;

import com.example.english_learning_platform.entity.*;
import com.example.english_learning_platform.repository.*;
import com.example.english_learning_platform.service.WordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WordServiceImpl implements WordService {

    private final WordRepository wordRepository;
    private final WordTypeRepository wordTypeRepository;
    private final UserWordProgressRepository userWordProgressRepository;
    private final DailyStudyRecordRepository dailyStudyRecordRepository;
    private final CheckinPlanRepository checkinPlanRepository;
    private final UserSelectedTypeRepository userSelectedTypeRepository;

    public WordServiceImpl(WordRepository wordRepository,
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

    @Override
    public List<WordType> getWordTypes() {
        List<WordType> types = wordTypeRepository.findAll();
        // 重新统计实机的单词个数（按单词和词性去重）
        for (WordType type : types) {
            type.setTotalWords((int) wordRepository.countUniqueWordsByTypeId(type.getTypeId()));
        }
        return types;
    }

    @Override
    public List<Word> getWordsByType(Long typeId, Integer limit) {
        List<Object[]> results = wordRepository.findMergedWordsByTypeId(typeId);
        List<Word> mergedWords = results.stream().map(row -> {
            Word w = new Word();
            w.setWordId(((Number) row[0]).longValue());
            w.setWord((String) row[1]);
            w.setPartOfSpeech((String) row[2]);
            w.setPhonetic((String) row[3]);
            w.setDefinition((String) row[4]);
            w.setExample((String) row[5]);
            w.setTypeId(((Number) row[6]).longValue());
            w.setSynonyms((String) row[7]);
            w.setAntonyms((String) row[8]);
            w.setUsageNotes((String) row[9]);
            w.setAudioUrl((String) row[10]);
            w.setImageUrl((String) row[11]);
            return w;
        }).toList();

        if (limit != null && limit > 0) {
            return mergedWords.stream().limit(limit).toList();
        }
        return mergedWords;
    }

    @Override
    public Map<String, Object> getUserWordProgress(Long userId) {
        List<WordType> types = wordTypeRepository.findAll();
        Map<String, Object> progressMap = new HashMap<>();

        long totalPassed = 0;

        for (WordType type : types) {
            long typePassed = userWordProgressRepository.countUniquePassedWordsByUserIdAndTypeId(userId, type.getTypeId());
            List<UserWordProgress> typePassedRecords = userWordProgressRepository.findByUserIdAndTypeIdAndPassedDateIsNotNull(userId, type.getTypeId());

            Map<String, Object> typeData = new HashMap<>();
            typeData.put("passedCount", typePassed);
            typeData.put("passedWords", typePassedRecords.stream().map(UserWordProgress::getWordId).toList());

            progressMap.put(type.getTypeId().toString(), typeData);
            totalPassed += typePassed;
        }

        progressMap.put("totalPassed", totalPassed);
        return progressMap;
    }

    @Override
    public List<Word> getPassedWords(Long userId) {
        List<Object[]> results = wordRepository.findMergedPassedWordsByUserId(userId);
        return results.stream().map(row -> {
            Word w = new Word();
            w.setWordId(((Number) row[0]).longValue());
            w.setWord((String) row[1]);
            w.setPartOfSpeech((String) row[2]);
            w.setPhonetic((String) row[3]);
            w.setDefinition((String) row[4]);
            w.setExample((String) row[5]);
            w.setTypeId(((Number) row[6]).longValue());
            w.setSynonyms((String) row[7]);
            w.setAntonyms((String) row[8]);
            w.setUsageNotes((String) row[9]);
            w.setAudioUrl((String) row[10]);
            w.setImageUrl((String) row[11]);
            return w;
        }).toList();
    }

    @Override
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

        UserWordProgress savedProgress = userWordProgressRepository.save(progress);

        // 更新每日学习记录
        LocalDate today = LocalDate.now();
        DailyStudyRecord record = dailyStudyRecordRepository.findByUserIdAndStudyDate(userId, today)
                .orElseGet(() -> {
                    DailyStudyRecord newRecord = new DailyStudyRecord();
                    newRecord.setUserId(userId);
                    newRecord.setStudyDate(today);
                    newRecord.setTypeId(typeId);
                    newRecord.setNewWords(0);
                    newRecord.setReviewWords(0);
                    newRecord.setTotalWords(0);
                    newRecord.setStreak(1);
                    return newRecord;
                });

        if (existing.isEmpty()) {
            record.setNewWords(record.getNewWords() + 1);
            record.setTotalWords(record.getTotalWords() + 1);
        }
        dailyStudyRecordRepository.save(record);

        return savedProgress;
    }

    @Override
    @Transactional
    public void unmarkWordPassed(Long userId, Long wordId) {
        Optional<UserWordProgress> progress = userWordProgressRepository
                .findByUserIdAndWordId(userId, wordId);
        progress.ifPresent(p -> {
            p.setPassedDate(null);
            p.setStage(0);
            userWordProgressRepository.save(p);

            // 更新每日学习记录
            LocalDate today = LocalDate.now();
            dailyStudyRecordRepository.findByUserIdAndStudyDate(userId, today).ifPresent(record -> {
                if (record.getNewWords() > 0) {
                    record.setNewWords(record.getNewWords() - 1);
                }
                if (record.getTotalWords() > 0) {
                    record.setTotalWords(record.getTotalWords() - 1);
                }
                dailyStudyRecordRepository.save(record);
            });
        });
    }

    @Override
    public Map<String, Object> getTodayCheckInStatus(Long userId) {
        LocalDate today = LocalDate.now();
        Optional<DailyStudyRecord> recordOpt = dailyStudyRecordRepository
                .findByUserIdAndStudyDate(userId, today);

        Map<String, Object> status = new HashMap<>();
        status.put("hasCheckedIn", recordOpt.isPresent());
        status.put("date", today);

        Map<String, Map<String, Integer>> typeStats = new HashMap<>();
        int totalNewWords = 0;
        int totalReviewWords = 0;

        if (recordOpt.isPresent()) {
            DailyStudyRecord record = recordOpt.get();
            String typeId = record.getTypeId().toString();
            Map<String, Integer> stats = typeStats.computeIfAbsent(typeId, k -> new HashMap<>());

            stats.put("learn", record.getNewWords() != null ? record.getNewWords() : 0);
            stats.put("review", record.getReviewWords() != null ? record.getReviewWords() : 0);

            totalNewWords += record.getNewWords() != null ? record.getNewWords() : 0;
            totalReviewWords += record.getReviewWords() != null ? record.getReviewWords() : 0;
        }

        status.put("typeStats", typeStats);
        status.put("totalNewWords", totalNewWords);
        status.put("totalReviewWords", totalReviewWords);
        status.put("records", recordOpt.isPresent() ? List.of(recordOpt.get()) : Collections.emptyList());

        return status;
    }

    @Override
    public Map<String, Object> getCheckInStatistics(Long userId) {
        List<DailyStudyRecord> records = dailyStudyRecordRepository
                .findByUserIdOrderByStudyDateDesc(userId);

        int totalDays = records.size();
        long totalWords = userWordProgressRepository.countUniquePassedWordsByUserId(userId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDays", totalDays);
        stats.put("totalWords", totalWords);
        stats.put("records", records);

        return stats;
    }

    @Override
    @Transactional
    public Map<String, Object> createCheckInPlan(Long userId, Long typeId, Integer wordsPerDay) {
        checkinPlanRepository.deleteByUserIdAndStatus(userId, "active");

        CheckinPlan plan = new CheckinPlan();
        plan.setUserId(userId);
        plan.setTypeId(typeId);
        plan.setWordsPerDay(wordsPerDay);
        plan.setStartDate(LocalDate.now());
        plan.setStatus("active");

        checkinPlanRepository.save(plan);

        return getPlanDetail(plan);
    }

    @Override
    public Map<String, Object> getUserCheckInPlan(Long userId) {
        Optional<CheckinPlan> plan = checkinPlanRepository.findByUserIdAndStatus(userId, "active");
        if (plan.isEmpty()) {
            return null;
        }
        return getPlanDetail(plan.get());
    }

    @Override
    public Map<String, Object> getUserCheckInPlanByType(Long userId, Long typeId) {
        Optional<CheckinPlan> plan = checkinPlanRepository.findByUserIdAndTypeIdAndStatus(userId, typeId, "active");
        if (plan.isEmpty()) {
            return null;
        }
        return getPlanDetail(plan.get());
    }

    /**
     * 计算并返回计划的详细信息（私有工具，不进接口）
     */
    private Map<String, Object> getPlanDetail(CheckinPlan plan) {
        long totalWords = wordRepository.countUniqueWordsByTypeId(plan.getTypeId());
        long passedCount = userWordProgressRepository.countUniquePassedWordsByUserIdAndTypeId(plan.getUserId(), plan.getTypeId());
        long remainingWords = totalWords - passedCount;
        long daysNeeded = (remainingWords + plan.getWordsPerDay() - 1) / plan.getWordsPerDay();
        if (daysNeeded < 1) {
            daysNeeded = 1;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("planId", plan.getPlanId());
        result.put("typeId", plan.getTypeId());
        result.put("wordsPerDay", plan.getWordsPerDay());
        result.put("startDate", plan.getStartDate());
        result.put("status", plan.getStatus());
        result.put("totalWords", totalWords);
        result.put("passedCount", passedCount);
        result.put("remainingWords", remainingWords);
        result.put("daysNeeded", daysNeeded);
        return result;
    }

    @Override
    @Transactional
    public Long setSelectedWordType(Long userId, Long typeId) {
        userSelectedTypeRepository.deleteByUserId(userId);

        UserSelectedType selected = new UserSelectedType();
        selected.setUserId(userId);
        selected.setTypeId(typeId);

        userSelectedTypeRepository.save(selected);
        return typeId;
    }

    @Override
    public Long getSelectedWordType(Long userId) {
        return userSelectedTypeRepository.findTopByUserIdOrderBySelectedDateDesc(userId)
                .map(UserSelectedType::getTypeId)
                .orElse(null);
    }

    @Override
    public Word getWordDetail(Long wordId) {
        Optional<Word> baseWordOpt = wordRepository.findById(wordId);
        if (baseWordOpt.isEmpty()) {
            return null;
        }
        Word baseWord = baseWordOpt.get();

        List<Word> allEntries = wordRepository.findByWordAndPartOfSpeech(baseWord.getWord(), baseWord.getPartOfSpeech());

        if (allEntries.size() <= 1) {
            return baseWord;
        }

        String mergedDefinition = allEntries.stream()
                .map(Word::getDefinition)
                .filter(d -> d != null && !d.isEmpty())
                .collect(Collectors.joining("；"));

        String mergedExample = allEntries.stream()
                .map(Word::getExample)
                .filter(e -> e != null && !e.isEmpty())
                .collect(Collectors.joining(" | "));

        baseWord.setDefinition(mergedDefinition);
        baseWord.setExample(mergedExample);
        return baseWord;
    }

    @Override
    public List<Word> getUnpassedWords(Long userId, Long typeId) {
        List<String> passedKeysList = userWordProgressRepository.findPassedWordKeysByUserIdAndTypeId(userId, typeId);
        Set<String> passedKeys = new HashSet<>(passedKeysList);
        List<Word> allMergedWords = getWordsByType(typeId, null);

        return allMergedWords.stream()
                .filter(word -> !passedKeys.contains(word.getWord() + "|" + word.getPartOfSpeech()))
                .toList();
    }

    @Override
    public int getConsecutiveCheckInDays(Long userId) {
        List<DailyStudyRecord> records = dailyStudyRecordRepository
                .findByUserIdOrderByStudyDateDesc(userId);

        if (records.isEmpty()) {
            return 0;
        }

        int consecutiveDays = 0;
        LocalDate today = LocalDate.now();

        boolean hasCheckedInToday = records.stream()
                .anyMatch(record -> record.getStudyDate().equals(today));

        LocalDate startDate;
        if (!hasCheckedInToday) {
            LocalDate yesterday = today.minusDays(1);
            boolean hasCheckedInYesterday = records.stream()
                    .anyMatch(record -> record.getStudyDate().equals(yesterday));
            if (!hasCheckedInYesterday) {
                return 0;
            }
            startDate = yesterday;
        } else {
            startDate = today;
        }

        LocalDate checkDate = startDate;
        while (true) {
            final LocalDate finalCheckDate = checkDate;
            boolean hasCheckedIn = records.stream()
                    .anyMatch(record -> record.getStudyDate().equals(finalCheckDate));

            if (hasCheckedIn) {
                consecutiveDays++;
                checkDate = checkDate.minusDays(1);
            } else {
                break;
            }
        }
        return consecutiveDays;
    }

    @Override
    public long getTotalLearnedWords(Long userId) {
        return userWordProgressRepository.countUniquePassedWordsByUserId(userId);
    }
}