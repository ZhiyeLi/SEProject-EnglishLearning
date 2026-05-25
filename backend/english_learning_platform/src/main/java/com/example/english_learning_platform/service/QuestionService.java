package com.example.english_learning_platform.service;

import com.example.english_learning_platform.entity.Course;
import com.example.english_learning_platform.entity.Question;
import com.example.english_learning_platform.entity.UserQuestionRecord;
import com.example.english_learning_platform.entity.UserVocabulary;

import java.util.List;
import java.util.Map;

public interface QuestionService {

    List<Question> getQuestions(String type, String difficulty);

    Map<String, Object> getQuestionDetail(Long id, Long userId);

    UserQuestionRecord toggleFavorite(Long userId, Long questionId);

    Map<String, Object> getStatistics(Long userId);

    List<UserQuestionRecord> getWrongQuestions(Long userId);

    List<Course> getCourses();

    List<Question> getCourseQuestions(String courseId);

    List<UserVocabulary> getUserVocabulary(Long userId);

    UserVocabulary addVocabulary(Long userId, Long wordId, String translation, String questionId);
}
