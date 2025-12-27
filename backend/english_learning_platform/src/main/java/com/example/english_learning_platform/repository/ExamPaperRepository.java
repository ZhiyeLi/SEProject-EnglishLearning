package com.example.english_learning_platform.repository;

import com.example.english_learning_platform.entity.ExamPaper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamPaperRepository extends JpaRepository<ExamPaper, Long> {
    
    // 按分类和关键字搜索
    Page<ExamPaper> findByCategoryAndNameContainingIgnoreCase(String category, String keyword, Pageable pageable);
    
    // 按关键字搜索（所有分类）
    Page<ExamPaper> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
    
    // 按分类查询
    Page<ExamPaper> findByCategory(String category, Pageable pageable);
    
    // 根据包含的题型查询试卷
    @Query("SELECT DISTINCT ep FROM ExamPaper ep " +
           "JOIN QuestionBank qb ON qb.paperId = ep.id " +
           "WHERE qb.sectionType IN :sectionTypes")
    Page<ExamPaper> findByContainingSectionTypes(@Param("sectionTypes") List<String> sectionTypes, Pageable pageable);
    
    // 根据包含的题型和分类查询试卷
    @Query("SELECT DISTINCT ep FROM ExamPaper ep " +
           "JOIN QuestionBank qb ON qb.paperId = ep.id " +
           "WHERE ep.category = :category AND qb.sectionType IN :sectionTypes")
    Page<ExamPaper> findByCategoryAndContainingSectionTypes(
        @Param("category") String category, 
        @Param("sectionTypes") List<String> sectionTypes, 
        Pageable pageable
    );
}
