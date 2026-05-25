package com.example.english_learning_platform.service.impl;

import com.example.english_learning_platform.entity.Course;
import com.example.english_learning_platform.entity.Question;
import com.example.english_learning_platform.entity.QuestionItem;
import com.example.english_learning_platform.entity.UserAnswerDetail;
import com.example.english_learning_platform.entity.UserQuestionRecord;
import com.example.english_learning_platform.entity.UserVocabulary;
import com.example.english_learning_platform.repository.CourseRepository;
import com.example.english_learning_platform.repository.QuestionItemRepository;
import com.example.english_learning_platform.repository.QuestionRepository;
import com.example.english_learning_platform.repository.UserAnswerDetailRepository;
import com.example.english_learning_platform.repository.UserQuestionRecordRepository;
import com.example.english_learning_platform.repository.UserVocabularyRepository;
import com.example.english_learning_platform.service.QuestionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionItemRepository questionItemRepository;
    private final UserQuestionRecordRepository userQuestionRecordRepository;
    private final UserAnswerDetailRepository userAnswerDetailRepository;
    private final CourseRepository courseRepository;
    private final UserVocabularyRepository userVocabularyRepository;

    public QuestionServiceImpl(QuestionRepository questionRepository,
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public List<UserQuestionRecord> getWrongQuestions(Long userId) {
        return userQuestionRecordRepository.findByUserIdAndStatus(userId, "wrong");
    }

    @Override
    public List<Course> getCourses() {
        return courseRepository.findAll();
    }

    @Override
    public List<Question> getCourseQuestions(String courseId) {
        return questionRepository.findByRelatedCourseId(courseId);
    }

    @Override
    public List<UserVocabulary> getUserVocabulary(Long userId) {
        return userVocabularyRepository.findByUserId(userId);
    }

    @Override
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

