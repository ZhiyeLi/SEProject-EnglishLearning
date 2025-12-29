package com.example.english_learning_platform.repository;

import com.example.english_learning_platform.entity.QuestionSubItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionSubItemRepository extends JpaRepository<QuestionSubItem, Long> {
    
    // 根据 parent_question_id 查询所有小题
    List<QuestionSubItem> findByParentQuestionIdOrderBySortOrder(Long parentQuestionId);
    
    // 根据多个 parent_question_id 查询所有小题
    List<QuestionSubItem> findByParentQuestionIdInOrderByParentQuestionIdAscSortOrderAsc(List<Long> parentQuestionIds);
}
