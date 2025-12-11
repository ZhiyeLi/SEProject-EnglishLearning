package com.example.english_learning_platform.repository;

import com.example.english_learning_platform.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByType(String type);
    List<Question> findByDifficulty(String difficulty);
    List<Question> findByRelatedCourseId(String courseId);
}
