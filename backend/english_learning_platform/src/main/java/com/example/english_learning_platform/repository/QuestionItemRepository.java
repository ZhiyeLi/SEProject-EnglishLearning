package com.example.english_learning_platform.repository;

import com.example.english_learning_platform.entity.QuestionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionItemRepository extends JpaRepository<QuestionItem, Long> {
    List<QuestionItem> findByQuestionIdOrderByOrderNum(String questionId);
}
