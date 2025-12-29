package com.example.english_learning_platform.repository;

import com.example.english_learning_platform.entity.UserExamRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserExamRecordRepository extends JpaRepository<UserExamRecord, Long> {
    
    // 查找用户某试卷的完成记录
    List<UserExamRecord> findByUserIdAndPaperIdAndStatus(
        Long userId, 
        Long paperId, 
        UserExamRecord.Status status
    );
    
    // 查找用户某试卷的所有记录
    List<UserExamRecord> findByUserIdAndPaperId(Long userId, Long paperId);
    
    // 查找用户正在进行的考试记录
    Optional<UserExamRecord> findByUserIdAndPaperIdAndModeAndStatusAndTargetQuestionId(
        Long userId,
        Long paperId,
        UserExamRecord.Mode mode,
        UserExamRecord.Status status,
        Long targetQuestionId
    );
    
    // 查询用户所有已完成的试卷ID
    @Query("SELECT DISTINCT uer.paperId FROM UserExamRecord uer " +
           "WHERE uer.userId = :userId AND uer.status = 'completed'")
    List<Long> findCompletedPaperIdsByUserId(@Param("userId") Long userId);
    
    // 查询用户所有已完成的题目ID（单题模式）
    @Query("SELECT DISTINCT uer.targetQuestionId FROM UserExamRecord uer " +
           "WHERE uer.userId = :userId AND uer.mode = 'single_part' AND uer.status = 'completed'")
    List<Long> findCompletedQuestionIdsByUserId(@Param("userId") Long userId);
}
