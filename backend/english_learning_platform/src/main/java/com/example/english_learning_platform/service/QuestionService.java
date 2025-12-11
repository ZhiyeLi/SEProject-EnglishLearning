package com.example.english_learning_platform.service;

import com.example.english_learning_platform.entity.*;
import com.example.english_learning_platform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class QuestionService {
    
    private final QuestionRepository questionRepository;
    private final QuestionItemRepository questionItemRepository;
    private final UserQuestionRecordRepository userQuestionRecordRepository;
    private final UserAnswerDetailRepository userAnswerDetailRepository;
    private final CourseRepository courseRepository;
    private final UserVocabularyRepository userVocabularyRepository;
    
    public QuestionService(QuestionRepository questionRepository,
                          QuestionItemRepository questionItemRepository,
                          UserQuestionRecordRepository userQuestionRecordRepository,
                          UserAnswerDetailRepository userAnswerDetailRepository,
                          CourseRepository courseRepository,
                          UserVocabularyRepository userVocabularyRepository) {
        this.questionRepository = questionRepository;
        this.questionItemRepository = questionItemRepository;
        this.userQuestionRecordRepository = userQuestionRecordRepository;
        this.userAnswerDetailRepository = userAnswerDetailRepository;
        this.courseRepository = courseRepository;
        this.userVocabularyRepository = userVocabularyRepository;
    }
    
    public List<Question> getQuestions(String type, String difficulty) {
        if (type != null && difficulty != null) {
            return questionRepository.findAll().stream()
                    .filter(q -> q.getType().equals(type) && q.getDifficulty().equals(difficulty))
                    .toList();
        } else if (type != null) {
            return questionRepository.findByType(type);
        } else if (difficulty != null) {
            return questionRepository.findByDifficulty(difficulty);
        }
        return questionRepository.findAll();
    }
    
    public Map<String, Object> getQuestionDetail(Long id, Long userId) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("题目不存在"));
        
        List<QuestionItem> items = questionItemRepository
                .findByQuestionIdOrderByOrderNum(id.toString());
        
        Optional<UserQuestionRecord> record = userQuestionRecordRepository
                .findByUserIdAndQuestionId(userId, id.toString());
        
        Map<String, Object> result = new HashMap<>();
        result.put("question", question);
        result.put("items", items);
        result.put("record", record.orElse(null));
        
        return result;
    }
    
    @Transactional
    public UserQuestionRecord toggleFavorite(Long userId, Long questionId) {
        Optional<UserQuestionRecord> existing = userQuestionRecordRepository
                .findByUserIdAndQuestionId(userId, questionId.toString());
        
        UserQuestionRecord record;
        if (existing.isPresent()) {
            record = existing.get();
            record.setIsFavorited(!record.getIsFavorited());
        } else {
            record = new UserQuestionRecord();
            record.setUserId(userId);
            record.setQuestionId(questionId.toString());
            record.setIsFavorited(true);
        }
        
        return userQuestionRecordRepository.save(record);
    }
    
    public Map<String, Object> getStatistics(Long userId) {
        Long correct = userQuestionRecordRepository.countByUserIdAndStatus(userId, "correct");
        Long wrong = userQuestionRecordRepository.countByUserIdAndStatus(userId, "wrong");
        Long total = (long) userQuestionRecordRepository.findByUserId(userId).size();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("correct", correct);
        stats.put("wrong", wrong);
        stats.put("total", total);
        
        return stats;
    }
    
    public List<UserQuestionRecord> getWrongQuestions(Long userId) {
        return userQuestionRecordRepository.findByUserIdAndStatus(userId, "wrong");
    }
    
    public List<Course> getCourses() {
        return courseRepository.findAll();
    }
    
    public List<Question> getCourseQuestions(String courseId) {
        return questionRepository.findByRelatedCourseId(courseId);
    }
    
    public List<UserVocabulary> getUserVocabulary(Long userId) {
        return userVocabularyRepository.findByUserId(userId);
    }
    
    @Transactional
    public UserVocabulary addVocabulary(Long userId, Long wordId, String translation, String questionId) {
        UserVocabulary vocabulary = new UserVocabulary();
        vocabulary.setUserId(userId);
        vocabulary.setWordId(wordId);
        vocabulary.setTranslation(translation);
        vocabulary.setSourceQuestionId(questionId);
        
        return userVocabularyRepository.save(vocabulary);
    }
}
