package com.example.english_learning_platform.repository;

import com.example.english_learning_platform.entity.ExamPaper;
import com.example.english_learning_platform.entity.QuestionBank.SectionType;
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
    Page<ExamPaper> findByContainingSectionTypes(@Param("sectionTypes") List<SectionType> sectionTypes, Pageable pageable);
    
    // 根据包含的题型和分类查询试卷
    @Query("SELECT DISTINCT ep FROM ExamPaper ep " +
           "JOIN QuestionBank qb ON qb.paperId = ep.id " +
           "WHERE ep.category = :category AND qb.sectionType IN :sectionTypes")
    Page<ExamPaper> findByCategoryAndContainingSectionTypes(
        @Param("category") String category, 
        @Param("sectionTypes") List<SectionType> sectionTypes, 
        Pageable pageable
    );
    
    // ========== 带状态筛选的查询方法 ==========
    
    // 查询已完成的试卷
    Page<ExamPaper> findByIdIn(List<Long> ids, Pageable pageable);
    
    // 按分类查询已完成的试卷
    Page<ExamPaper> findByCategoryAndIdIn(String category, List<Long> ids, Pageable pageable);
    
    // 按分类查询未完成的试卷
    Page<ExamPaper> findByCategoryAndIdNotIn(String category, List<Long> ids, Pageable pageable);
    
    // 查询未完成的试卷
    Page<ExamPaper> findByIdNotIn(List<Long> ids, Pageable pageable);
    
    // 根据包含的题型查询已完成的试卷
    @Query("SELECT DISTINCT ep FROM ExamPaper ep " +
           "JOIN QuestionBank qb ON qb.paperId = ep.id " +
           "WHERE qb.sectionType IN :sectionTypes AND ep.id IN :completedIds")
    Page<ExamPaper> findByContainingSectionTypesAndCompleted(
        @Param("sectionTypes") List<SectionType> sectionTypes, 
        @Param("completedIds") List<Long> completedIds,
        Pageable pageable
    );
    
    // 根据包含的题型查询未完成的试卷
    @Query("SELECT DISTINCT ep FROM ExamPaper ep " +
           "JOIN QuestionBank qb ON qb.paperId = ep.id " +
           "WHERE qb.sectionType IN :sectionTypes AND ((:completedIds) IS NULL OR ep.id NOT IN :completedIds)")
    Page<ExamPaper> findByContainingSectionTypesAndNotCompleted(
        @Param("sectionTypes") List<SectionType> sectionTypes, 
        @Param("completedIds") List<Long> completedIds,
        Pageable pageable
    );
    
    // 根据包含的题型和分类查询已完成的试卷
    @Query("SELECT DISTINCT ep FROM ExamPaper ep " +
           "JOIN QuestionBank qb ON qb.paperId = ep.id " +
           "WHERE ep.category = :category AND qb.sectionType IN :sectionTypes AND ep.id IN :completedIds")
    Page<ExamPaper> findByCategoryAndContainingSectionTypesAndCompleted(
        @Param("category") String category, 
        @Param("sectionTypes") List<SectionType> sectionTypes, 
        @Param("completedIds") List<Long> completedIds,
        Pageable pageable
    );
    
    // 根据包含的题型和分类查询未完成的试卷
    @Query("SELECT DISTINCT ep FROM ExamPaper ep " +
           "JOIN QuestionBank qb ON qb.paperId = ep.id " +
           "WHERE ep.category = :category AND qb.sectionType IN :sectionTypes AND ((:completedIds) IS NULL OR ep.id NOT IN :completedIds)")
    Page<ExamPaper> findByCategoryAndContainingSectionTypesAndNotCompleted(
        @Param("category") String category, 
        @Param("sectionTypes") List<SectionType> sectionTypes, 
        @Param("completedIds") List<Long> completedIds,
        Pageable pageable
    );
    
    // 根据包含的题型查询收藏的试卷
    @Query("SELECT DISTINCT ep FROM ExamPaper ep " +
           "JOIN QuestionBank qb ON qb.paperId = ep.id " +
           "WHERE qb.sectionType IN :sectionTypes AND ep.id IN :favoritedIds")
    Page<ExamPaper> findByContainingSectionTypesAndFavorited(
        @Param("sectionTypes") List<SectionType> sectionTypes, 
        @Param("favoritedIds") List<Long> favoritedIds,
        Pageable pageable
    );
    
    // 根据包含的题型和分类查询收藏的试卷
    @Query("SELECT DISTINCT ep FROM ExamPaper ep " +
           "JOIN QuestionBank qb ON qb.paperId = ep.id " +
           "WHERE ep.category = :category AND qb.sectionType IN :sectionTypes AND ep.id IN :favoritedIds")
    Page<ExamPaper> findByCategoryAndContainingSectionTypesAndFavorited(
        @Param("category") String category, 
        @Param("sectionTypes") List<SectionType> sectionTypes, 
        @Param("favoritedIds") List<Long> favoritedIds,
        Pageable pageable
    );
}
