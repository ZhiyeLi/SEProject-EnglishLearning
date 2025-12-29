package com.example.english_learning_platform.repository;

import com.example.english_learning_platform.entity.UserAnswerDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserAnswerDetailRepository extends JpaRepository<UserAnswerDetail, Long> {
    
    // 根据 record_id 查询答题详情
    List<UserAnswerDetail> findByRecordId(Long recordId);
    
    // 查询用户今日答题数量
    @Query("SELECT COUNT(uad) FROM UserAnswerDetail uad " +
           "WHERE uad.userId = :userId " +
           "AND DATE(uad.createdAt) = CURRENT_DATE")
    Long countTodayAnswers(@Param("userId") Long userId);
    
    // 查询用户今日错题数量
    @Query("SELECT COUNT(uad) FROM UserAnswerDetail uad " +
           "WHERE uad.userId = :userId " +
           "AND uad.isCorrect = 0 " +
           "AND DATE(uad.createdAt) = CURRENT_DATE")
    Long countTodayWrongAnswers(@Param("userId") Long userId);
    
    // 查询用户今日正确率
    @Query("SELECT AVG(uad.isCorrect) FROM UserAnswerDetail uad " +
           "WHERE uad.userId = :userId " +
           "AND DATE(uad.createdAt) = CURRENT_DATE")
    Double calculateTodayAccuracy(@Param("userId") Long userId);
    
    // 查询用户错题（根据用户ID和正确性）
    @Query("SELECT uad FROM UserAnswerDetail uad " +
           "WHERE uad.userId = :userId AND uad.isCorrect = 0 " +
           "ORDER BY uad.createdAt DESC")
    List<UserAnswerDetail> findWrongAnswersByUserId(@Param("userId") Long userId);
    
    // 查询用户最近7天的错题
    @Query("SELECT uad FROM UserAnswerDetail uad " +
           "WHERE uad.userId = :userId " +
           "AND uad.isCorrect = 0 " +
           "AND uad.createdAt >= :startDate " +
           "ORDER BY uad.createdAt DESC")
    List<UserAnswerDetail> findRecentWrongAnswers(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate
    );
    
    // 统计某个小题的错误次数（用户维度）
    @Query("SELECT COUNT(uad) FROM UserAnswerDetail uad " +
           "WHERE uad.userId = :userId " +
           "AND uad.subItemId = :subItemId " +
           "AND uad.isCorrect = 0")
    Long countWrongAnswersBySubItem(
        @Param("userId") Long userId,
        @Param("subItemId") Long subItemId
    );
}

