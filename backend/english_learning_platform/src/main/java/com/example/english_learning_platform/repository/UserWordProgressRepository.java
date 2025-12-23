package com.example.english_learning_platform.repository;

import com.example.english_learning_platform.entity.UserWordProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import org.springframework.data.repository.query.Param;

@Repository
public interface UserWordProgressRepository extends JpaRepository<UserWordProgress, Long> {
    List<UserWordProgress> findByUserIdAndTypeId(Long userId, Long typeId);
    Optional<UserWordProgress> findByUserIdAndWordId(Long userId, Long wordId);
    Long countByUserIdAndPassedDateIsNotNull(Long userId);
    Long countByUserIdAndTypeIdAndPassedDateIsNotNull(Long userId, Long typeId);
    List<UserWordProgress> findByUserIdAndPassedDateIsNotNull(Long userId);
    List<UserWordProgress> findByUserIdAndPassedDate(Long userId, LocalDate passedDate);

    @Query("SELECT COUNT(DISTINCT w.wordId) FROM UserWordProgress w " +
            "WHERE w.userId = :userId " +
            "AND w.lastReviewTime BETWEEN :startTime AND :endTime")
    Long countDistinctWordsByUserIdAndTimeRange(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT w.userId, COUNT(DISTINCT w.wordId) FROM UserWordProgress w " +
            "WHERE w.userId IN :userIds " +
            "AND w.lastReviewTime BETWEEN :startTime AND :endTime " +
            "GROUP BY w.userId")
    List<Object[]> countDistinctWordsByUserIdsAndTimeRange(
            @Param("userIds") List<Long> userIds,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
