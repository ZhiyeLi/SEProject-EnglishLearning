package com.example.english_learning_platform.repository;

import com.example.english_learning_platform.entity.QuestionBank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionBankRepository extends JpaRepository<QuestionBank, Long> {
    
    // 根据 paper_id 查询所有题目
    List<QuestionBank> findByPaperIdOrderBySortOrder(Long paperId);
    
    // 单题模式查询：根据条件分页查询
    // 支持多字段搜索：title, section_name, section_type
    @Query("SELECT q FROM QuestionBank q " +
           "JOIN ExamPaper ep ON q.paperId = ep.id " +
           "WHERE (:category = 'all' OR ep.category = :category) " +
           "AND (:sectionType = 'all' OR CAST(q.sectionType AS string) = :sectionType) " +
           "AND (:keyword IS NULL OR :keyword = '' OR " +
           "     LOWER(q.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(q.sectionName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(CAST(q.sectionType AS string)) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<QuestionBank> findByFilters(
        @Param("category") String category,
        @Param("sectionType") String sectionType,
        @Param("keyword") String keyword,
        Pageable pageable
    );
    
    // 根据 paper_id 和 section_type 查询
    List<QuestionBank> findByPaperIdAndSectionType(Long paperId, QuestionBank.SectionType sectionType);
}
